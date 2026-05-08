package cn.dreamtof.query.core;

import cn.dreamtof.query.annotation.SmartFetch;
import cn.dreamtof.query.structure.SmartQueryStructure.JoinNode;
import cn.dreamtof.core.utils.FastBeanMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结果组装器：SmartQuery 的核心引擎 (Tier 0 极速版)
 * 作用：将数据库返回的平铺、带有冗余数据的 Map 列表，组装成带有层级的、去重后的树形 VO。
 * 重构说明：移除了 MapStruct 相关代码，只保留了 LambdaMetafactory 黑科技。
 */
@Slf4j
public class SmartQueryAssembler<R> {

    private final Class<R> resultClass;
    private final JoinNode rootNode;
    private final ObjectMapper objectMapper;

    // 保留 EntityFactory 作为特殊字段的极速转换兜底 (比如把 DB 的 JSON 字符串无反射地转成 List)
    private final Map<Class<?>, EntityFactory<?>> entityFactoryRegistry = new ConcurrentHashMap<>();

    // 预计算好的 Alias 映射缓存
    private final Map<JoinNode, Map<String, String>> aliasCache = new IdentityHashMap<>();

    @FunctionalInterface
    public interface EntityFactory<E> {
        E create(Map<String, Object> row, String aliasPrefix);
    }

    // 使用 System.identityHashCode 防止在装配过程中对象 HashCode 被意外修改导致乱序
    private record IdentityKey(int parentIdentityHash, JoinNode node, Object pkValue) {
        public IdentityKey(Object parent, JoinNode node, Object pkValue) {
            this(System.identityHashCode(parent), node, pkValue);
        }
    }

    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
            String.class, Long.class, Integer.class, Double.class, Boolean.class,
            Date.class, LocalDate.class, LocalDateTime.class, BigDecimal.class
    );

    /**
     * 构造器
     * 保留原因：初始化 Assembler 实例，并在初始化阶段触发 Alias 预计算。
     */
    public SmartQueryAssembler(Class<R> resultClass, JoinNode rootNode, Map<Class<?>, EntityFactory<?>> factories) {
        this.resultClass = resultClass;
        this.rootNode = rootNode;
        this.objectMapper = SmartQueryContext.getObjectMapper();

        if (MapUtils.isNotEmpty(factories)) {
            this.entityFactoryRegistry.putAll(factories);
        }

        // 初始化预先计算字段别名缓存
        preloadAlias(rootNode, "", new HashSet<>());
    }

    /**
     * 预计算别名缓存 (Preload Alias)
     * 作用：在遍历 Row 数据前，把每一个字段在 SQL 返回的 Map 里叫什么名字 (例如 t1$userId) 提前拼好。
     * 保留原因：避免在百万级数据的循环中去执行字符串拼接 (prefix + "$" + prop)，极其关键的性能优化！
     */
    private void preloadAlias(JoinNode node, String prefix, Set<JoinNode> visited) {
        if (visited.contains(node)) return;
        visited.add(node);

        Map<String, String> fieldToAlias = new HashMap<>();
        String currentPrefix = prefix.isEmpty() ? "" : prefix + "$";

        // 预计算普通字段别名
        node.selectFields.forEach((prop, col) -> fieldToAlias.put(prop, currentPrefix + prop));

        // 预计算 PK 别名
        String pkKey = node.voPkPropName != null ? node.voPkPropName : "Flex_Internal_PK";
        fieldToAlias.put("PK", currentPrefix + pkKey);

        aliasCache.put(node, fieldToAlias);

        node.children.forEach((name, child) -> preloadAlias(child, currentPrefix + name, visited));
    }

    /**
     * 核心：重建对象树 (Reconstruct)
     * 作用：引擎的灵魂方法。把平铺的笛卡尔积数据（List<Map>）压缩并还原成立体的结果树 (List<VO>)。
     * 保留原因：最核心入口，必须存在。
     */
    public List<R> reconstruct(List<Map<String, Object>> rows) {
        Map<Object, R> rootMap = new LinkedHashMap<>(rows.size());
        Map<IdentityKey, Object> contextCache = new HashMap<>(rows.size() * 2);

        String rootPkAlias = aliasCache.get(rootNode).get("PK");

        for (Map<String, Object> row : rows) {
            Object pk = row.get(rootPkAlias);
            if (pk == null) pk = row.hashCode(); // 兜底：如果没查主键，用 hash

            // 核心去重逻辑：如果该主键的对象已经存在，则只填充追加子属性，不再重复实例化！
            R rootVo = rootMap.computeIfAbsent(pk, k -> buildNodeObject(row, rootNode));
            fillRecursive(rootVo, row, rootNode, contextCache);
        }

        List<R> results = new ArrayList<>(rootMap.values());

        // 处理延迟加载的子查询 (@SmartFetch FetchType.LAZY)
        if (!results.isEmpty()) {
            processDeferredTasks(results, rootNode);
        }
        return results;
    }

    /**
     * 核心对象构建器 (直达 VO)
     * 作用：基于一行 Map 数据，实例化目标 VO。
     * 瘦身原因：它原本又长又臭，因为要判断找不找得到 MapStruct。现在直接精简为“工厂转换”或“直接反射注入 VO”。
     * 保留原因：必须需要一个创建单个对象实例的入口。
     */
    @SuppressWarnings("unchecked")
    private <T> T buildNodeObject(Map<String, Object> row, JoinNode node) {
        // 👇 新增：简单类型直接返回值，不实例化对象
        if (node.simpleType) {
            // 从selectFields取第一个字段的值
            String col = node.selectFields.values().stream()
                .findFirst()
                .orElse(null);
            if (col != null) {
                Object val = row.get(col);
                if (val != null) {
                    return (T) convertValue(val, node.fieldType);
                }
            }
            return null;
        }

        // 1. 手工实体工厂 (最高优先级，用于处理复杂的反序列化场景)
        if (node.fieldType.isAssignableFrom(node.entityClass)) {
            EntityFactory<?> factory = entityFactoryRegistry.get(node.entityClass);
            if (factory != null) {
                return (T) factory.create(row, node.path.replace("$", ".") + ".");
            }
        }

        // 2. 极速属性映射 (兜底：跳过了中间 Entity，直接通过 FastBeanMeta 将 Row 数据填入目标 VO)
        T vo = (T) BeanUtils.instantiateClass(node.fieldType);
        fillProperties(vo, row, node);
        return vo;
    }

    /**
     * 扁平属性填充
     * 作用：将当前节点所需的标量属性（如 id, name, age）通过 FastBeanMeta 填入到 VO 中。
     * 保留原因：核心的数据拷贝方法，代替了 MapStruct。
     */
    private void fillProperties(Object target, Map<String, Object> row, JoinNode node) {
        Map<String, String> aliases = aliasCache.get(node);
        // 获取极速元数据，无反射开销
        FastBeanMeta beanMeta = FastBeanMeta.of(target.getClass());

        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if ("PK".equals(entry.getKey())) continue; // 跳过主键专属别名处理
            Object val = row.get(entry.getValue());
            if (val != null) {
                setFastValue(beanMeta, target, entry.getKey(), val);
            }
        }
    }

    /**
     * 递归填充关联对象
     * 作用：处理 1:1 或 1:N 的嵌套层级。它顺藤摸瓜，把子对象创建出来并塞进父对象的属性里（如塞入 List 里）。
     * 保留原因：实现深层级结构 (如 Order -> List<Item> -> ItemDetail) 的唯一途径。
     */
    private void fillRecursive(Object currentVo, Map<String, Object> row, JoinNode currentNode, Map<IdentityKey, Object> context) {
        FastBeanMeta beanMeta = FastBeanMeta.of(currentVo.getClass());

        for (Map.Entry<String, JoinNode> entry : currentNode.children.entrySet()) {
            String fieldName = entry.getKey();
            JoinNode childNode = entry.getValue();

            // 1. 简单的表关联单字段查询 (Relation)
            if (childNode.isLeaf) {
                String fullAlias = childNode.path + "$" + fieldName;
                Object val = row.get(fullAlias);
                if (val != null) setFastValue(beanMeta, currentVo, fieldName, convertValue(val, childNode.fieldType));
                continue;
            }

            // 2. 复杂的子对象加载 (SmartFetch)
            String childPkAlias = aliasCache.get(childNode).get("PK");
            Object childDbId = row.get(childPkAlias);
            if (childDbId == null) continue; // 数据库 Left Join 未匹配上记录

            Object currentFieldVal = getFastValue(beanMeta, currentVo, fieldName);

            // 2.1 1:N 集合装配 (如 List<Role> 或 Set<Role>)
            if (childNode.isCollection) {
                // 👇 修改：使用Collection接口，支持List/Set
                Collection<Object> collection = (Collection<Object>) currentFieldVal;
                if (collection == null) {
                    // 根据字段原始类型决定实例化List还是Set
                    Class<?> fieldRawType = childNode.fieldRawType;
                    Collection<Object> newCollection;
                    if (fieldRawType != null && Set.class.isAssignableFrom(fieldRawType)) {
                        newCollection = new HashSet<>();
                    } else {
                        newCollection = new ArrayList<>();
                    }
                    setFastValue(beanMeta, currentVo, fieldName, newCollection);
                    collection = newCollection;
                }

                if (childNode.simpleType) {
                    // 简单类型集合：直接添加值
                    String col = childNode.selectFields.values().stream()
                        .findFirst()
                        .orElse(null);
                    if (col != null) {
                        Object val = row.get(col);
                        if (val != null) {
                            collection.add(convertValue(val, childNode.fieldType));
                        }
                    }
                } else {
                    // 复杂类型集合：实例化对象并去重
                    IdentityKey cacheKey = new IdentityKey(currentVo, childNode, childDbId);
                    Object childObj = context.get(cacheKey);
                    if (childObj == null) {
                        childObj = buildNodeObject(row, childNode);
                        collection.add(childObj);
                        context.put(cacheKey, childObj);
                    }
                    fillRecursive(childObj, row, childNode, context);
                }
            }
            // 2.2 1:1 对象装配 (如 Dept)
            else {
                if (currentFieldVal == null) {
                    currentFieldVal = buildNodeObject(row, childNode);
                    setFastValue(beanMeta, currentVo, fieldName, currentFieldVal);
                }
                fillRecursive(currentFieldVal, row, childNode, context);
            }
        }
    }

    // ==========================================
    // 底层：基于 Lambda 的极速设值工具
    // ==========================================

    /**
     * 极速设值 (Setter)
     * 作用：利用预先编译的 Lambda 表达式，像原生代码一样去执行 `target.setField(value)`。
     * 保留原因：如果使用传统的 field.set(obj, val) 反射，在高并发下性能极差。这是整个框架号称 Tier 0 的底气。
     */
    private void setFastValue(FastBeanMeta meta, Object target, String field, Object value) {
        try {
            var accessor = meta.getAccessor(field);
            if (accessor != null && accessor.setter() != null) {
                accessor.setter().accept(target, value);
            }
        } catch (Throwable ignore) {
            // 兜底：防止因个别非标准字段抛出异常导致整个数据流装配中断
        }
    }

    /**
     * 极速取值 (Getter)
     * 作用：原理同 Setter。
     * 保留原因：在分组聚合、延迟加载提取父级 ID 时需要用到。
     */
    private Object getFastValue(FastBeanMeta meta, Object target, String field) {
        try {
            var accessor = meta.getAccessor(field);
            return (accessor != null && accessor.getter() != null) ? accessor.getter().apply(target) : null;
        } catch (Throwable e) { return null; }
    }

    // 重载快捷方法，外部未传 meta 时自动获取
    private Object getFastValue(Object target, String field) {
        return getFastValue(FastBeanMeta.of(target.getClass()), target, field);
    }

    /**
     * 类型强转 (Type Convert)
     * 作用：弥合数据库底层 JDBC 类型与 Java 实体类类型之间的小差异 (例如 DB 返回 BigDecimal，VO 期望是 Integer)。
     * 保留原因：由于失去了 MapStruct 自带的类型转换功能，这里必须手动做一个简单的容错处理，防止 ClassCastException。
     */
    private Object convertValue(Object val, Class<?> targetType) {
        if (val == null) return null;
        if (targetType.isInstance(val)) return val;

        if (val instanceof Number n) {
            if (targetType == Integer.class || targetType == int.class) return n.intValue();
            if (targetType == Long.class || targetType == long.class) return n.longValue();
            if (targetType == Double.class || targetType == double.class) return n.doubleValue();
            if (targetType == BigDecimal.class) return new BigDecimal(n.toString());
        }
        if (PRIMITIVE_TYPES.contains(targetType)) return val;

        // 如果是复杂类型对象 (如 JSON String 转 List)，尝试交给 Jackson 兜底处理
        try { return objectMapper.readValue(val.toString(), targetType); } catch (Exception e) { return val; }
    }

    // ==========================================
    // 延迟加载子查询引擎 (Deferred Load)
    // ==========================================

    /**
     * 处理推迟的任务 (主要用于 @SmartFetch(FetchType.LAZY))
     * 作用：当主数据全部装配完成后，扫描是否还有被标记为“延迟加载”的关联节点。如果有，则收集 ID 发起 N+1 中的那一次 "1" 的批量子查询。
     * 保留原因：这是解决复杂递归树（如无限极分类树）、或者超大集合关联时，避免单次 SQL 过于臃肿的官方推荐手段。
     */
    private void processDeferredTasks(List<?> parentObjects, JoinNode parentNode) {
        if (CollectionUtils.isEmpty(parentObjects)) return;

        // 处理当前层的延迟加载任务
        if (CollectionUtils.isNotEmpty(parentNode.deferredChildren)) {
            parentNode.deferredChildren.parallelStream().forEach(deferNode -> executeDeferredFetch(parentObjects, deferNode));
        }

        // 向下遍历子节点，如果子节点还有子节点，继续收集父级进行下一轮延迟加载
        for (JoinNode childNode : parentNode.children.values()) {
            if (childNode.isLeaf) continue;
            List<Object> children = new ArrayList<>();
            for (Object parent : parentObjects) {
                Object childVal = getFastValue(parent, childNode.fieldName);
                if (childVal == null) continue;
                if (childVal instanceof Collection<?> c) children.addAll(c);
                else children.add(childVal);
            }
            if (!children.isEmpty()) processDeferredTasks(children, childNode);
        }
    }

    /**
     * 执行具体的批量子查询
     * 作用：收集父对象的所有 localKey（如 userId），去子表中执行 `WHERE user_id IN (1,2,3)`，然后按外键分组回填给每个父对象。
     * 保留原因：延迟加载的核心逻辑实现者。
     */
    @SuppressWarnings("unchecked")
    private void executeDeferredFetch(List<?> parents, JoinNode deferNode) {
        SmartFetch sf = deferNode.originalFetch;
        Set<Object> linkValues = new HashSet<>();

        FastBeanMeta parentMeta = null;
        if (!parents.isEmpty()) parentMeta = FastBeanMeta.of(parents.getFirst().getClass());

        // 收集父级主键集合
        for (Object parent : parents) {
            Object val = getFastValue(parentMeta, parent, sf.localField());
            if (val != null) linkValues.add(val);
        }
        if (linkValues.isEmpty()) return;

        String remoteCol = org.apache.commons.lang3.StringUtils.isNotBlank(sf.remoteFieldLink()) ?
                SmartQueryContext.getTableInfo(sf.targetEntity()).getColumnByProperty(sf.remoteFieldLink()) :
                SmartQueryContext.getTableInfo(sf.targetEntity()).getPrimaryKeyList().getFirst().getColumn();

        final String finalRemoteCol = remoteCol;

        // 防止 in() 参数过多导致 SQL 报错，分批次(1000条/批)查询
        final int BATCH_SIZE = 1000;
        List<Object> linkValueList = new ArrayList<>(linkValues);
        List<List<Object>> batches = new ArrayList<>();
        for (int i = 0; i < linkValueList.size(); i += BATCH_SIZE) {
            batches.add(linkValueList.subList(i, Math.min(i + BATCH_SIZE, linkValueList.size())));
        }

        // 并发执行子查询装载 (复用 FlexSmartQuery 框架本身)
        List<Object> allChildren = batches.parallelStream()
                .map(batch -> (List<Object>) FlexSmartQuery.of(sf.targetEntity())
                        .bind(deferNode.fieldType)
                        .withFactories(this.entityFactoryRegistry)  // 依然传递全局工厂配置
                        .where(new com.mybatisflex.core.query.QueryColumn("t0", finalRemoteCol).in(batch))
                        .list())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();

        // 内存中做按外键 GroupBy 分组映射
        Map<Object, Collection<Object>> groupedChildren = new HashMap<>();
        String remoteProp = SmartQueryContext.getPropertyByColumn(sf.targetEntity(), remoteCol);
        if (remoteProp == null) remoteProp = sf.remoteFieldLink();

        FastBeanMeta childMeta = allChildren.isEmpty() ? null : FastBeanMeta.of(allChildren.getFirst().getClass());

        for (Object child : allChildren) {
            Object linkVal = getFastValue(childMeta, child, remoteProp);
            if (linkVal != null) {
                // 👇 修改：根据deferNode.fieldRawType决定创建List还是Set
                Collection<Object> collection = groupedChildren.computeIfAbsent(
                    linkVal,
                    k -> {
                        Class<?> fieldRawType = deferNode.fieldRawType;
                        if (fieldRawType != null && Set.class.isAssignableFrom(fieldRawType)) {
                            return new HashSet<>();
                        } else {
                            return new ArrayList<>();
                        }
                    }
                );
                collection.add(child);
            }
        }

        // 最后回填给所有的 Parent 对象
        for (Object parent : parents) {
            Object linkVal = getFastValue(parentMeta, parent, sf.localField());
            Collection<Object> matches = groupedChildren.get(linkVal);
            if (matches != null) {
                if (deferNode.isCollection) {
                    setFastValue(parentMeta, parent, deferNode.fieldName, matches);
                } else if (!matches.isEmpty()) {
                    setFastValue(parentMeta, parent, deferNode.fieldName, matches.iterator().next());
                }
            }
        }
    }
}