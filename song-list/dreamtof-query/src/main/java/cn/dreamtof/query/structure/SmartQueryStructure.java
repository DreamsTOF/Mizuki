package cn.dreamtof.query.structure;

import cn.dreamtof.query.annotation.Relation;
import cn.dreamtof.query.annotation.SmartFetch;
import cn.dreamtof.query.enums.FetchType;
import cn.dreamtof.query.enums.MatchType;
import cn.dreamtof.query.core.SmartQueryContext;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 结构构建器：负责解析 VO 并维护底层的 Join 树 (AST)
 * <p>
 * 架构定位：
 * 它是 SmartQuery 引擎的“蓝图生成器”。
 * 前端需要一个带有深层嵌套的树形 VO，但关系型数据库只能处理扁平的二维表。
 * 这个类负责在查询发轫之初，利用反射(带缓存)扫一遍 VO，把带有 @Relation 和 @SmartFetch 的字段，
 * 翻译成一颗关联树（JoinNode），供后续生成 SQL 和结果组装使用。
 * </p>
 */
public class SmartQueryStructure {

    /**
     * 关联树节点 (代表 SQL 里的一个 Table 及其别名)
     * 【设计原因】：我们需要记录主表 t0 和子表 t1, t2 的关联关系（谁通过哪个外键连着谁），以及这层关联是为了装配 VO 里的哪个属性。
     */
    public static class JoinNode {
        public String path;          // 节点层级路径，如 "dept$roles"
        public String tableAlias;    // SQL 表别名，如 "t1", "t2"
        public String linkCol;       // 关联的外键列名 (右表列)
        public String localCol;      // 本地的关联列名 (左表列)
        public TableInfo tableInfo;  // 数据库表元数据
        public Class<?> entityClass; // 对应的底层 Entity Class
        public Class<?> fieldType;   // 对应的目标 VO Class
        public boolean isCollection; // 是否是 1:N 集合 (List)
        public boolean isLeaf;       // 是否是 Relation (单字段叶子节点，不需要继续往下钻)
        public String remoteTargetCol; // Relation 模式下，具体要 SELECT 的目标列
        public String pkColName;     // 数据库表的主键列名
        public String voPkPropName;  // VO 中映射主键的属性名

        // 延迟加载/子查询专用
        public boolean isDeferred;   // 是否被推迟到后续执行 (为了防止笛卡尔积爆炸)
        public SmartFetch originalFetch; // 原始的 SmartFetch 注解信息
        public String fieldName;     // 在父 VO 中的属性名

        public Map<String, String> selectFields = new LinkedHashMap<>(); // 当前节点需要 SELECT 的普通字段
        public Map<String, JoinNode> children = new LinkedHashMap<>();   // 物理 JOIN 的子节点
        public List<JoinNode> deferredChildren = new ArrayList<>();      // 被推迟执行的逻辑子节点

        // 简单类型集合支持
        public boolean simpleType = false;     // 是否为简单类型集合（List<String>等）
        public String valueField = "";         // 简单类型要提取的字段名
        public Class<?> fieldRawType = null;   // 字段的原始类型（List.class/Set.class），用于实例化集合
    }

    /**
     * 字段过滤映射
     * 【设计原因】：记录 VO 里的属性名，最终对应 SQL 里的哪个别名的哪个字段（如 userId -> t0.user_id）。
     */
    public static class FilterMapping {
        public String field;
        public QueryColumn column;
        public MatchType type;
        public FilterMapping(String field, QueryColumn column, MatchType type) {
            this.field = field; this.column = column; this.type = type;
        }
        public QueryColumn column() { return column; }
        public MatchType type() { return type; }
    }

    @Getter
    private final JoinNode rootNode = new JoinNode();
    private int aliasCounter = 0; // 用于生成 t1, t2, t3 等别名
    public final Map<String, FilterMapping> filterRegistry = new HashMap<>();

    /**
     * 构造器
     * 【作用】：初始化这棵树的“树干”（即 FROM 的那张主表，永远是 t0）。
     */
    public SmartQueryStructure(Class<?> entityClass, Class<?> resultClass) {
        TableInfo info = SmartQueryContext.getTableInfo(entityClass);
        this.rootNode.tableAlias = "t0";
        this.rootNode.tableInfo = info;
        this.rootNode.entityClass = entityClass;
        this.rootNode.fieldType = resultClass;
        this.rootNode.path = "";
        this.rootNode.pkColName = info.getPrimaryKeyList().getFirst().getColumn();
    }

    /**
     * 核心递归解析方法：解析 VO 树
     * 【作用】：扫描 VO 里的每一个字段，并归类为 Relation（查单个字段）、SmartFetch（查整个对象/集合）还是 Simple Field（普通字段）。
     * 【保留原因】：引擎蓝图生成的核心驱动，它决定了最终的 SQL 到底长什么样。
     */
    public void parseVoTree(Class<?> voClass, JoinNode node, int collectionDepth) {
        // 先找到 VO 里哪个字段是当主键用的
        node.voPkPropName = findPkPropInVo(voClass, node.pkColName, node.tableInfo);

        // 利用上下文的缓存，极速获取 VO 的元数据
        for (SmartQueryContext.VoFieldMeta meta : SmartQueryContext.getVoFields(voClass)) {
            // 1. 如果是 @Relation (一般用于 Left Join 过来一个普通字段，比如查 User 顺带查 DeptName)
            if (meta.relation() != null) {
                processRelation(meta, node);
                continue;
            }
            // 2. 如果是 @SmartFetch (用于查整个复杂嵌套对象，比如 List<RoleVO>)
            if (meta.smartFetch() != null) {
                processSmartFetch(meta, node, collectionDepth);
                continue;
            }
            // 3. 如果是普通字段 (Simple Field)
            if (node.tableInfo != null) {
                String col = node.tableInfo.getColumnByProperty(meta.name());
                if (col != null) {
                    node.selectFields.put(meta.name(), col);
                    // 【防污染设计】：只有主表 (Root Node) 的字段才允许注册到全局 Filter 供前端作为查询条件，
                    // 避免子表的同名字段（比如主表有 name，子表也有 name）造成 WHERE 语句混乱。
                    if (node == this.rootNode) {
                        filterRegistry.put(meta.name(), new FilterMapping(meta.name(), new QueryColumn(node.tableAlias, col), MatchType.CUSTOM));
                    }
                }
            }
        }
    }

    /**
     * 处理 @SmartFetch 深层嵌套抓取
     * 【作用】：遇到嵌套对象/集合时，决定是将其化作一条 LEFT JOIN 语句拼在主 SQL 里，还是将其推迟 (Deferred) 作为单独的一条子查询。
     * 【保留原因】：防“笛卡尔积爆炸”的核心控制器。
     */
    private void processSmartFetch(SmartQueryContext.VoFieldMeta meta, JoinNode parentNode, int collectionDepth) {
        SmartFetch fetch = meta.smartFetch();
        Class<?> targetEntity = fetch.targetEntity();
        TableInfo targetInfo = SmartQueryContext.getTableInfo(targetEntity);
        if (targetInfo == null) return;

        JoinNode child = new JoinNode();
        child.fieldName = meta.name();
        child.tableInfo = targetInfo;
        child.entityClass = targetEntity;
        child.fieldType = meta.isCollection() ? meta.componentType() : meta.type();
        child.isCollection = meta.isCollection();
        child.originalFetch = fetch;
        if (!targetInfo.getPrimaryKeyList().isEmpty()) {
            child.pkColName = targetInfo.getPrimaryKeyList().getFirst().getColumn();
        }

        // 👇 新增：记录字段的原始类型（用于实例化集合）
        child.fieldRawType = meta.type();  // List.class 或 Set.class

        // 简单类型集合支持
        child.simpleType = fetch.simpleType();
        child.valueField = fetch.valueField();

        // 如果指定了simpleType，验证valueField
        if (child.simpleType && child.valueField.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("@SmartFetch simpleType=true时必须指定valueField。字段: %s, targetEntity: %s",
                    meta.name(), targetEntity.getName())
            );
        }

        // 如果指定了simpleType且valueField，只SELECT该字段
        if (child.simpleType && !child.valueField.isEmpty()) {
            String col = targetInfo.getColumnByProperty(child.valueField);
            if (col != null) {
                child.selectFields.put(child.valueField, col);
            } else {
                throw new IllegalArgumentException(
                    String.format("@SmartFetch valueField指定的字段不存在。valueField: %s, targetEntity: %s",
                        child.valueField, targetEntity.getName())
                );
            }
        }

        boolean shouldDefer = false;

        // 【智能延迟加载逻辑 (N+1 防御)】：
        // 1. 开发者显式声明 LAZY 时，强制推迟。
        if (fetch.fetchType() == FetchType.LAZY) {
            shouldDefer = true;
        }  else {
            if (child.isCollection) {
                // 2. 多级 List 防御：如果上层已经是 List，这一层还是 List (如 User -> Orders -> Items)，
                // 坚决推迟！否则 Join 出来的数据量是 订单数 * 明细数，内存直接 OOM。
                if (collectionDepth > 0) shouldDefer = true;
                    // 3. 平级 List 防御：如果一个对象已经 Left Join 了一个 List (如 User -> Roles)，
                    // 现在又要 Join 第二个平级 List (如 User -> Permissions)，也必须推迟！
                    // 否则数据量会是 角色数 * 权限数。
                else if (hasCollectionSibling(parentNode)) shouldDefer = true;
            }
        }

        if (shouldDefer) {
            // 推迟执行：将其挂载到 deferredChildren 中，交由 Assembler 查完主数据后再单独发 SQL 查。
            child.isDeferred = true;
            parentNode.deferredChildren.add(child);
            parseVoTree(child.fieldType, child, 0); // 延迟加载的新一轮解析，深度重置为 0
        } else {
            // 立刻执行：分配别名 (t1, t2...)，将其连入 Join 树，一次 SQL 查出。
            child.tableAlias = "t" + (++aliasCounter);
            child.linkCol = StringUtils.isNotBlank(fetch.remoteFieldLink()) ? targetInfo.getColumnByProperty(fetch.remoteFieldLink()) : targetInfo.getPrimaryKeyList().getFirst().getColumn();
            child.localCol = fetch.localField();

            parentNode.children.put(meta.name(), child);

            int nextDepth = collectionDepth + (child.isCollection ? 1 : 0);
            parseVoTree(child.fieldType, child, nextDepth);
        }
    }

    /**
     * 处理 @Relation 扁平关系映射
     * 【作用】：生成用于 Left Join 抓取单一字段的叶子节点。
     * 【保留原因】：轻量级的数据填充机制（比如在订单列表里显示一句冗余的“商品名称”）。
     */
    private void processRelation(SmartQueryContext.VoFieldMeta meta, JoinNode parentNode) {
        Relation relation = meta.relation();
        TableInfo targetInfo = SmartQueryContext.getTableInfo(relation.targetEntity());
        if (targetInfo == null) return;

        JoinNode child = new JoinNode();
        child.isLeaf = true;
        child.tableAlias = "t" + (++aliasCounter);
        child.tableInfo = targetInfo;
        child.entityClass = relation.targetEntity();
        child.fieldType = meta.type();

        child.linkCol = StringUtils.isNotBlank(relation.remoteFieldLink()) ? targetInfo.getColumnByProperty(relation.remoteFieldLink()) : targetInfo.getPrimaryKeyList().getFirst().getColumn();
        child.localCol = relation.localField();
        child.remoteTargetCol = relation.remoteField(); // 唯一区别：它记录了要 SELECT 的具体目标列

        parentNode.children.put(meta.name(), child);
    }

    /**
     * 将解析好的结构应用到 MyBatis-Flex 的 QueryWrapper 上
     * 【作用】：把上述建好的 JoinNode 树，真正翻译成 Flex 的 `select(...)` 和 `leftJoin(...).on(...)`。
     * 【保留原因】：SQL 生成的最终执行者。
     */
    public void applyToWrapper(QueryWrapper queryWrapper, JoinNode node) {
        // 【关键保护机制：Shadow PK (影子主键)】
        // 即使前端VO里根本没有定义 `id` 这个字段（因为前端不想展示），
        // 我们的底层 Assembler 也*必须*靠数据库的主键才能去重！
        // 因此，如果发现VO没选主键，我们就在底层偷偷 `select id as Flex_Internal_PK`，保证后续能正常折叠树形结构。
        if (node.pkColName != null) {
            String pkAliasKey = node.voPkPropName != null ? node.voPkPropName : "Flex_Internal_PK";
            if (!node.selectFields.containsKey(pkAliasKey)) {
                String alias = (node.path.isEmpty() ? "" : node.path + "$") + pkAliasKey;
                queryWrapper.select(new QueryColumn(node.tableAlias, node.pkColName).as(alias));
            }
        }

        // 1. 追加所有的 SELECT 字段 (带别名，避免同名冲突)
        for (var entry : node.selectFields.entrySet()) {
            String alias = (node.path.isEmpty() ? "" : node.path + "$") + entry.getKey();
            queryWrapper.select(new QueryColumn(node.tableAlias, entry.getValue()).as(alias));
        }

        // 2. 递归追加 LEFT JOIN 语句
        for (var entry : node.children.entrySet()) {
            JoinNode child = entry.getValue();
            child.path = (node.path.isEmpty() ? "" : node.path + "$") + entry.getKey();

            String hostCol = node.tableInfo.getColumnByProperty(child.localCol);

            if (child.isLeaf) {
                // 如果是 Relation，补上要查询的具体那一列
                String targetCol = child.tableInfo.getColumnByProperty(child.remoteTargetCol);
                String alias = child.path + "$" + entry.getKey();
                queryWrapper.select(new QueryColumn(child.tableAlias, targetCol).as(alias));
            }

            // 拼装：LEFT JOIN child_table as tX ON tY.hostCol = tX.linkCol
            queryWrapper.leftJoin(child.tableInfo.getTableName()).as(child.tableAlias)
                    .on(new QueryColumn(node.tableAlias, hostCol).eq(new QueryColumn(child.tableAlias, child.linkCol)));

            applyToWrapper(queryWrapper, child);
        }
    }

    /**
     * 辅助方法：检查是否有同级别的集合关联
     * 【作用】：用于触发平级 List 的防爆保护（一旦发现兄弟节点里有人是 List，自己就必须推迟查询）。
     */
    private boolean hasCollectionSibling(JoinNode parent) {
        for (JoinNode child : parent.children.values()) {
            if (child.isCollection) return true;
        }
        return false;
    }

    /**
     * 查找 VO 中对应的主键属性名 (支持父类继承扫描)
     * 【作用】：找到 VO 里用来做身份标识的字段（比如 VO 里的 `userId` 对应数据库的 `id` 列）。
     * 【保留原因】：让 Assembler 知道用哪个字段来进行对象级去重。
     */
    private String findPkPropInVo(Class<?> voClass, String dbPkCol, TableInfo tableInfo) {
        List<SmartQueryContext.VoFieldMeta> voFields = SmartQueryContext.getVoFields(voClass);
        String pkProp = SmartQueryContext.getPropertyByColumn(tableInfo.getEntityClass(), dbPkCol);

        // 1. 如果 Entity 里的 PK 属性名在 VO 里也存在，就用它
        if (pkProp != null) {
            boolean exists = voFields.stream().anyMatch(f -> f.name().equals(pkProp));
            if (exists) return pkProp;
        }

        // 2. 兜底尝试：查找名为 "id" 的字段
        boolean hasId = voFields.stream().anyMatch(f -> f.name().equals("id"));
        if (hasId) return "id";

        // 3. 确实没有映射主键，返回 null，后续流程会使用 Flex_Internal_PK 作为暗号
        return null;
    }
}