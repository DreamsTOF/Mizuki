package cn.dreamtof.query.annotation;

import cn.dreamtof.query.enums.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【对象级联】
 * 声明一个嵌套的 VO 或 VO 集合 (支持 1:1, 1:N)。
 * 引擎会递归解析该 VO 的字段并进行 Join 查询。
 */
/**
 * 【对象级联】
 * 声明一个嵌套的 VO 或 VO 集合 (支持 1:1, 1:N)。
 * 引擎会递归解析该 VO 的字段并进行 Join 查询。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SmartFetch {
    Class<?> targetEntity();

    /** 本地关联键 (Entity属性名) */
    String localField();

    /** 远程关联键 (留空默认为目标表主键) */
    String remoteFieldLink() default "";

    /**
     * 抓取策略
     * 默认为 AUTO：智能识别嵌套深度，自动拆分查询以防止笛卡尔积爆炸。
     */
    FetchType fetchType() default FetchType.AUTO;

    /**
     * 是否为简单类型集合
     * true: 字段是 List&lt;基本类型/String&gt;，直接从 ResultSet 取值，不实例化对象
     * false: 字段是 List&lt;VO/Entity&gt;，需要实例化并填充属性（默认）
     *
     * 当 simpleType=true 时，必须同时指定 valueField 属性
     */
    boolean simpleType() default false;

    /**
     * 简单类型集合要提取的字段名
     * 当 simpleType=true 时必填，指定 targetEntity 的哪个字段作为集合元素值
     * 例如：Tag 实体有 tagName 字段，这里填 "tagName"
     *
     * 不填则默认使用 targetEntity 的主键字段
     */
    String valueField() default "";
}
