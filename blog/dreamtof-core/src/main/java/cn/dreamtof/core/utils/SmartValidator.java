package cn.dreamtof.core.utils;

import cn.dreamtof.core.annotation.Check;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.exception.CommonErrorCode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 🚀 SmartValidator: 极致性能的通用校验引擎
 * 特点：
 * 1. 零反射：底层通过 FastBeanMeta 使用 LambdaMetafactory 调用 getter。
 * 2. 深度防御：集成 Asserts.isDeepEmpty 递归检查容器内容。
 * 3. 语义化：支持快捷格式校验（手机号、邮箱等）与正数检查。
 * 4. 易扩展：校验逻辑集中在 performCheck 方法中。
 */
@Component
public class SmartValidator {

    /** 元数据缓存：类 -> 带有 @Check 注解的字段访问器列表 */
    private static final Cache<Class<?>, List<CheckField>> CHECK_CACHE = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build();

    private record CheckField(FastBeanMeta.FieldAccessor accessor, Check check) {}

    /**
     * 校验对象入口
     * @param dto 待校验的 DTO/POJO 对象
     */
    public static void validate(Object dto) {
        if (dto == null) return;
        Class<?> clazz = dto.getClass();

        // 1. 获取（或解析）该类的校验元数据
        List<CheckField> checkFields = CHECK_CACHE.get(clazz, c -> 
            FastBeanMeta.of(c).getAllAccessors().stream()
                .filter(acc -> acc.field().isAnnotationPresent(Check.class))
                .map(acc -> new CheckField(acc, acc.field().getAnnotation(Check.class)))
                .collect(Collectors.toList())
        );

        // 2. 执行字段级校验
        for (CheckField cf : checkFields) {
            Object value = cf.accessor().getter().apply(dto);
            performCheck(value, cf.check());

            // 3. 处理递归校验逻辑 (valid = true)
            if (cf.check().valid() && value != null) {
                handleRecursive(value);
            }
        }
    }

    /**
     * 核心规则断言
     */
    private static void performCheck(Object value, Check check) {
        String label = check.msg();

        // [规则 1]: 深度必填校验 (利用 Asserts.isDeepEmpty)
        if (check.required() && Asserts.isDeepEmpty(value)) {
            Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "不能为空");
        }

        // [规则 2]: 基础非空校验
        if (check.notNull() && Asserts.isNull(value)) {
            Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "不能为 NULL");
        }

        // 如果允许为空且当前值为空，则跳过后续校验
        if (value == null) return;

        // [规则 3]: 根据类型执行具体逻辑
        switch (value) {
            case String s -> {
                // 长度校验
                int len = s.length();
                if (check.min() != Long.MIN_VALUE && len < check.min())
                    Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "长度过短");
                if (check.max() != Long.MAX_VALUE && len > check.max())
                    Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "长度过长");

                // 快捷格式校验 (调用 Asserts 预编译正则)
                if (!check.type().isEmpty()) {
                    switch (check.type()) {
                        case "mobile" -> Asserts.isMobile(s, label + "格式错误");
                        case "email" -> Asserts.isEmail(s, label + "格式错误");
                        case "idcard" -> Asserts.isIdCard(s, label + "格式错误");
                        case "numeric" -> { if (!Asserts.isNumeric(s)) Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "必须为数字"); }
                        default -> {}
                    }
                }

                // 自定义正则匹配
                if (!check.regex().isEmpty()) {
                    Asserts.match(s, check.regex(), label + "格式非法");
                }
            }
            case Number n -> {
                // 正数检查
                if (check.positive() && n.doubleValue() <= 0)
                    Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "必须为正数");

                // 数值范围校验
                long l = n.longValue();
                if (check.min() != Long.MIN_VALUE && l < check.min())
                    Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "数值过小");
                if (check.max() != Long.MAX_VALUE && l > check.max())
                    Asserts.fail(CommonErrorCode.PARAM_ERROR, label + "数值过大");
            }
            case Collection<?> coll -> {
                // 集合内元素非空校验
                if (check.noNulls()) {
                    Asserts.noNullElements(coll, label + "包含空元素");
                }
            }
            default -> {}
        }
    }

    /**
     * 处理嵌套对象的递归校验
     */
    private static void handleRecursive(Object value) {
        if (value instanceof Collection<?> coll) {
            for (Object item : coll) validate(item);
        } else if (value instanceof Object[] arr) {
            for (Object item : arr) validate(item);
        } else {
            validate(value);
        }
    }
}