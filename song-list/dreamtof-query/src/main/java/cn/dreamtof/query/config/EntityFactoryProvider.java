package cn.dreamtof.query.config;

import cn.dreamtof.query.core.SmartQueryAssembler;

/**
 * 🧩 实体工厂提供者 (Extension SPI)
 * <p>
 * 【架构定位】：
 * 这是 SmartQuery 引擎留给开发者的“最高权限”扩展点（逃生舱）。
 *
 * 【为什么需要它？】：
 * 默认情况下，Assembler 会通过反射 new 出 VO，然后用 FastBeanMeta 极速塞值。
 * 但在某些极端场景下，这种“默认规矩”走不通，比如：
 * 1. 数据库里存的是一段 JSON 字符串，但你的 VO 里声明的是 List<String> 或者嵌套对象。
 * 2. 你的实体类是一个“充血模型”，不允许外界直接 set 属性，必须通过复杂的业务逻辑构造。
 * 3. 枚举或状态机的极其特殊的转化逻辑。
 *
 * 【怎么用？】：
 * 开发者只需写一个类实现本接口，并标上 @Component 交给 Spring 管理。
 * 引擎在运行时发现该实体有对应的 Factory，就会放弃默认操作，直接把那行数据库数据
 * (Map<String, Object>) 全权交给你来手动组装并返回！
 * </p>
 * @param <E> 目标实体类型
 */
public interface EntityFactoryProvider<E> {

    /**
     * 声明这个工厂是用来处理哪个实体类的
     * @return 目标实体 Class
     */
    Class<E> getEntityClass();

    /**
     * 提供具体的工厂组装逻辑
     * <p>
     * 返回的 EntityFactory 函数式接口包含两个入参：
     * 1. row: 数据库查出来的单行原生 Map 数据。
     * 2. aliasPrefix: 引擎帮你计算好的列名前缀（如 "t1$"），方便你准确 get 属于该表的数据。
     * </p>
     * @return 具体的对象创建逻辑实现
     */
    SmartQueryAssembler.EntityFactory<E> getFactory();
}