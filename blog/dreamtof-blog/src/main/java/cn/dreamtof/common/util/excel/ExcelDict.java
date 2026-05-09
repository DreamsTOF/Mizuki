package cn.dreamtof.common.util.excel;

import java.lang.annotation.*;

/**
 * 导出字典映射注解
 * 用于将数字/编码隐式转换为文字
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelDict {
    /**
     * 映射规则，格式为 "k:v,k:v"
     * 例如: "1:文本,2:图片,3:文件,4:链接"
     */
    String readConverterExp() default "";

    /**
     * 日期格式化，如 "yyyy-MM-dd HH:mm:ss"
     */
    String format() default "";
}
