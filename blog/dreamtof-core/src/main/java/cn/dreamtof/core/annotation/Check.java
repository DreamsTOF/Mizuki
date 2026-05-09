package cn.dreamtof.core.annotation;

import java.lang.annotation.*;

/**
 * 🚀 SmartCheck: 轻量级、高性能业务校验注解
 * 配合 SmartValidator 使用，支持深度判空、数值区间、正则及内置格式校验。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Check {

    /**
     * 业务错误提示消息
     * 例如："用户手机号" -> 校验失败会自动拼接为 "用户手机号不能为空" 或 "用户手机号格式错误"
     */
    String msg() default "参数";

    /**
     * 是否必填 (深度判空)
     * 1. 引用类型：不能为 null
     * 2. 字符串：不能是空串或空格
     * 3. 集合/Map/数组：调用 Asserts.isDeepEmpty 确保内部有实质内容
     */
    boolean required() default false;

    /**
     * 仅仅校验非 null
     * 区别于 required，它不关心字符串是否为空白，只关心对象是否存在
     */
    boolean notNull() default false;

    /**
     * 最小长度 (String) 或 最小值 (Number)
     * 默认 Long.MIN_VALUE 表示不限制
     */
    long min() default Long.MIN_VALUE;

    /**
     * 最大长度 (String) 或 最大值 (Number)
     * 默认 Long.MAX_VALUE 表示不限制
     */
    long max() default Long.MAX_VALUE;

    /**
     * 必须为正数 ( > 0 )
     * 仅对 Number 类型有效
     */
    boolean positive() default false;

    /**
     * 集合/数组内不能包含 null 元素
     * 仅对 Collection 或 Array 有效
     */
    boolean noNulls() default false;

    /**
     * 自定义正则表达式校验
     */
    String regex() default "";

    /**
     * 内置常用格式快捷校验
     * 取值示例: "email", "mobile", "idcard", "numeric"
     * 对应 Asserts 中预编译的 Pattern，性能极高
     */
    String type() default "";

    /**
     * 是否递归校验 (针对嵌套的 DTO 对象)
     * 如果为 true，Validator 会深度进入该字段指向的对象执行 validate()
     */
    boolean valid() default false;
}