package cn.dreamtof.query.core;

import cn.dreamtof.core.utils.FastBeanMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * ⚡ 查询执行计划 (Query Execution Plan)
 * 将 DTO 的解析过程从 "解释执行" 优化为 "预编译执行"。
 */
public class SmartQueryPlan {

    private static final Map<Class<?>, SmartQueryPlan> PLAN_CACHE = new ConcurrentHashMap<>();

    // 这是一个处理链：每个节点负责处理一个字段
    private final List<BiConsumer<FlexSmartQuery<?, ?>, Object>> steps = new ArrayList<>();

    public static SmartQueryPlan get(Class<?> dtoClass) {
        return PLAN_CACHE.computeIfAbsent(dtoClass, SmartQueryPlan::build);
    }

    public void execute(FlexSmartQuery<?, ?> queryEngine, Object dto) {
        for (var step : steps) {
            step.accept(queryEngine, dto);
        }
    }

    private static SmartQueryPlan build(Class<?> dtoClass) {
        SmartQueryPlan plan = new SmartQueryPlan();
        Map<String, SmartQueryContext.DtoFieldMeta> metas = SmartQueryContext.getDtoFields(dtoClass);
        FastBeanMeta beanMeta = FastBeanMeta.of(dtoClass);

        for (var entry : metas.entrySet()) {
            String fieldName = entry.getKey();
            SmartQueryContext.DtoFieldMeta meta = entry.getValue();
            // 获取极速 Getter (Lambda)
            var accessor = beanMeta.getAccessor(fieldName);
            if (accessor == null || accessor.getter() == null) continue;
            // 2. 预编译：普通字段逻辑
            // 我们在这里把 "if (val == null)" 的判断逻辑也封装进去
            // 这样运行时就不用再去查 Map 获取 meta 了，直接运行闭包
            plan.steps.add((engine, dto) -> {
                Object val = accessor.getter().apply(dto);
                // 快速判空 (String 特判)
                if (val == null || (val instanceof String s && s.isBlank()) || (val instanceof java.util.Collection<?> c && c.isEmpty())) {
                    return;
                }
                // 直接调用 engine 的核心构建方法，跳过反射查找
                engine.applyFieldCondition(fieldName, val, meta);
            });
        }
        return plan;
    }
}
