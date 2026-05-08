package cn.dreamtof.core.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树形结构构建器 (SmartTreeBuilder)
 * <p>
 * 基于“空间换时间”的策略，将 O(N^2) 的双重 for 循环树构建过程优化为 O(N) 时间复杂度。
 * 适用于菜单树、部门树、评论树等任何具有父子层级关系的扁平数据结构的转换。
 * </p>
 *
 * <b>使用示例：</b>
 * <pre>{@code
 * List<Menu> tree = SmartTreeBuilder.of(menuList, 0L)
 * .withId(Menu::getId)
 * .withParentId(Menu::getParentId)
 * .withChildrenSetter(Menu::setChildren)
 * .build();
 * }</pre>
 *
 * @param <T> 树节点的实体数据类型 (例如：Menu, Department)
 * @param <R> 节点唯一标识(ID)的数据类型 (例如：Long, String)
 */
public class SmartTreeBuilder<T, R> {

    /**
     * 需要被转换为树形结构的扁平数据源列表
     */
    private final List<T> flatList;

    /**
     * 根节点的父级 ID 标识值 (例如通常根节点的 parentId 为 0L 或 null)
     */
    private final R rootId;

    /**
     * 获取节点唯一标识 (ID) 的函数接口
     */
    private Function<T, R> idGetter;

    /**
     * 获取节点父级标识 (Parent ID) 的函数接口
     */
    private Function<T, R> parentIdGetter;

    /**
     * 设置节点子集列表 (Children) 的函数接口
     */
    private BiConsumer<T, List<T>> childrenSetter;

    /**
     * 构造函数
     *
     * @param flatList 扁平化的原始数据列表
     * @param rootId   定义根节点的 parentId 匹配值
     */
    public SmartTreeBuilder(List<T> flatList, R rootId) {
        this.flatList = flatList;
        this.rootId = rootId;
    }

    /**
     * 静态工厂方法，用于链式调用初始化
     *
     * @param flatList 扁平列表
     * @param rootId   根节点标识
     * @param <T>      节点类型
     * @param <R>      ID 类型
     * @return SmartTreeBuilder 实例
     */
    public static <T, R> SmartTreeBuilder<T, R> of(List<T> flatList, R rootId) {
        return new SmartTreeBuilder<>(flatList, rootId);
    }

    /**
     * 配置：如何获取当前节点的 ID
     *
     * @param idGetter 例如 {@code Entity::getId}
     * @return 当前构建器实例
     */
    public SmartTreeBuilder<T, R> withId(Function<T, R> idGetter) {
        this.idGetter = idGetter;
        return this;
    }

    /**
     * 配置：如何获取当前节点的 Parent ID
     *
     * @param parentIdGetter 例如 {@code Entity::getParentId}
     * @return 当前构建器实例
     */
    public SmartTreeBuilder<T, R> withParentId(Function<T, R> parentIdGetter) {
        this.parentIdGetter = parentIdGetter;
        return this;
    }

    /**
     * 配置：如何将子节点列表设置到父节点中
     *
     * @param childrenSetter 例如 {@code Entity::setChildren}
     * @return 当前构建器实例
     */
    public SmartTreeBuilder<T, R> withChildrenSetter(BiConsumer<T, List<T>> childrenSetter) {
        this.childrenSetter = childrenSetter;
        return this;
    }

    /**
     * 执行构建，将扁平列表组装为树形结构
     * <p>核心原理：通过一次遍历将节点按 parentId 分组，再通过一次遍历将子节点挂载到对应的父节点上。</p>
     *
     * @return 组装好的树形结构的根节点列表
     */
    public List<T> build() {
        // 如果传入的扁平列表为空，直接返回空集合以防止空指针异常
        if (CollectionUtils.isEmpty(flatList)) return Collections.emptyList();

        // 1. O(N) 构建索引 Map
        // 【注意】如果后续不需要通过 ID 快速反查节点对象，这段代码其实可以删除，目前在下方的组装逻辑中并未被实际调用，删除可节省内存。
        Map<R, T> nodeMap = flatList.stream()
                .collect(Collectors.toMap(idGetter, Function.identity(), (a, b) -> a));

        // 2. O(N) 预初始化所有节点的子列表容器
        // 以 parentId 为 Key，将所有不属于根节点的子节点提前分好组
        Map<R, List<T>> childrenGroup = flatList.stream()
                // 过滤掉根节点，因为根节点没有父节点或其父节点不在当前列表中
                .filter(node -> !Objects.equals(parentIdGetter.apply(node), rootId))
                // 按照 parentId 进行分组聚合
                .collect(Collectors.groupingBy(parentIdGetter));

        // 3. O(N) 组装：只遍历一次 flatList 建立父子引用
        List<T> roots = new ArrayList<>();
        for (T node : flatList) {
            R id = idGetter.apply(node);
            R pid = parentIdGetter.apply(node);

            // 如果当前节点的 parentId 等于配置的 rootId，说明它是顶层根节点，放入根集合
            if (Objects.equals(pid, rootId)) {
                roots.add(node);
            }

            // 从第 2 步预先分组好的 Map 中，通过当前节点的 id 取出属于它的所有子节点
            List<T> children = childrenGroup.get(id);
            // 如果存在子节点，则调用传入的 setter 方法将子节点注入到当前对象中
            if (children != null) {
                childrenSetter.accept(node, children);
            }
        }

        // 返回仅包含顶层节点的列表（由于对象是引用传递，内部的 children 已经组装完毕）
        return roots;
    }
}