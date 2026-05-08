package cn.dreamtof.query.config;


import cn.dreamtof.query.core.FlexSmartQuery;
import cn.dreamtof.query.core.SmartQueryAssembler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ⚙️ SmartQuery 全局配置中心 (Spring 胶水层)
 * <p>
 * 【架构定位】：
 * 连接 Spring IOC 容器和 SmartQuery 静态核心引擎的桥梁。
 *
 * 【为什么这么设计？】：
 * FlexSmartQuery 的入口是静态方法 `FlexSmartQuery.of(...)`，它的实例是不受 Spring 容器管理的
 * （以此避免了繁重的 Bean 生命周期开销，保证每次 new 一个 Query Engine 都轻如鸿毛）。
 * * 但是，开发者写的自定义装配工厂 (EntityFactoryProvider) 通常是需要注入 Dao/Redis 的 Spring Bean。
 * 为了让不受 Spring 管理的查询引擎能够“零配置”地用到这些受管工厂，我们需要这个配置类。
 * 它在 Spring 启动时，把所有的 Factory Bean 收集起来，塞进一个全局的静态常量 (FACTORIES) 里。
 * 这样引擎在组装数据时，直接查这个静态 Map 就能极速拿到所有定制化组装规则。
 * </p>
 */
@Configuration
public class SmartQueryConfig {

    /**
     * 全局实体工厂注册表 (线程安全，允许静态直接访问)
     * FlexSmartQuery 在实例化时会默认把这里的工厂加载到自己的局部上下文中。
     */
    public static final Map<Class<?>, SmartQueryAssembler.EntityFactory<?>> FACTORIES = new ConcurrentHashMap<>();

    /**
     * 依赖注入钩子 (Spring Boot 启动时自动触发)
     * 【作用】：自动扫描上下文中所有实现了 EntityFactoryProvider 的 Bean，注册进全局静态池。
     * * @param providers Spring 收集到的所有工厂提供者
     * @return 注册完毕的工厂 Map
     */
    @Bean
    public Map<Class<?>, SmartQueryAssembler.EntityFactory<?>> smartQueryFactories(List<EntityFactoryProvider<?>> providers) {
        if (providers != null) {
            for (EntityFactoryProvider<?> provider : providers) {
                FACTORIES.put(provider.getEntityClass(), provider.getFactory());
            }
        }
        return FACTORIES;
    }

    /**
     * 🚀 SmartQueryEngine: 非静态引擎入口 (Mixed-Mode Proxy)
     * <p>
     * 【设计哲学】：
     * 采用“零成本包装”模式。本类作为 Spring 管理的 Bean 存在，但核心逻辑完全复用静态工厂。
     *
     * 【性能保障】：
     * 1. 零调用损耗：JVM JIT 会对 from() 调用的 of() 进行方法内联，机器码层面无额外开销。
     * 2. 零内存碎片：不额外持有元数据副本，仅作为 Spring 生态的接入点。
     *
     * 【使用说明】：
     * @Autowired
     * private SmartQueryEngine queryEngine;
     * </p>
     */
    @Component
    public static class SmartQueryEngine {

        /**
         * 非静态入口：开启一个智能查询流
         * 语义上使用 'from' 与静态的 'of' 形成区分。
         *
         * @param entityClass 主表实体类
         * @return FlexSmartQuery 查询实例
         */
        public <E> FlexSmartQuery<E, E> from(Class<E> entityClass) {
            // 直接复用 Tier 0 静态工厂，保持全局逻辑唯一，减少维护成本并享受 JIT 内联优化
            return FlexSmartQuery.of(entityClass);
        }

        /**
         * 辅助方法：获取当前引擎加载的所有工厂数量
         */
        public int getFactoryCount() {
            return FACTORIES.size();
        }
    }
}