package cn.dreamtof.audit.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;

/**
 * 👑 审计元数据缓存
 * <p>
 * 为什么要有这个类：
 * 1. 自动翻译：审计日志中必须记录“用户名”变更而非“userName”变更。
 * 2. 性能：解析注解涉及昂贵的反射调用，必须通过缓存消灭二次损耗。
 * 3. 兼容性：同时支持 OpenAPI 3 (@Schema) 和 Swagger 2 (@ApiModelProperty)。
 * </p>
 */
@Slf4j
public class AuditMetaCache {

    /** 存储字段/类与其展示名称的对应关系 */
    private static final Cache<AnnotatedElement, String> DISPLAY_NAME_CACHE = Caffeine.newBuilder()
            .maximumSize(5000)
            .weakKeys() // 弱引用 Key，防止类卸载后缓存不释放
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build();

    /**
     * 获取字段的中文展示名称
     */
    public static String getDisplayName(Field field) {
        if (field == null) return "未知字段";
        return DISPLAY_NAME_CACHE.get(field, AuditMetaCache::resolveDisplayNameFromAnnotations);
    }

    /**
     * 获取类的中文展示名称
     */
    public static String getDisplayName(Class<?> clazz) {
        if (clazz == null) return "未知实体";
        return DISPLAY_NAME_CACHE.get(clazz, AuditMetaCache::resolveDisplayNameFromAnnotations);
    }

    /**
     * 核心逻辑：扫描注解并提取描述
     * 采用动态反射方式读取注解属性，避免强依赖 Swagger 库导致类找不到。
     */
    @SuppressWarnings("unchecked")
    private static String resolveDisplayNameFromAnnotations(AnnotatedElement element) {
        // 1. 尝试 OpenAPI 3 (@Schema)
        try {
            Class<?> schemaClass = Class.forName("io.swagger.v3.oas.annotations.media.Schema");
            Object schema = element.getAnnotation((Class<? extends java.lang.annotation.Annotation>) schemaClass);
            if (schema != null) {
                String desc = (String) schemaClass.getMethod("description").invoke(schema);
                if (desc != null && !desc.isEmpty()) return desc;
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.trace(AUDIT,"解析 @Schema 失败: {}", e.getMessage());
        }

        // 2. 尝试 Swagger 2 (@ApiModelProperty)
        try {
            Class<?> apiPropClass = Class.forName("io.swagger.annotations.ApiModelProperty");
            Object apiProp = element.getAnnotation((Class<? extends java.lang.annotation.Annotation>) apiPropClass);
            if (apiProp != null) {
                String value = (String) apiPropClass.getMethod("value").invoke(apiProp);
                if (value != null && !value.isEmpty()) return value;
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.trace(AUDIT,"解析 @ApiModelProperty 失败: {}", e.getMessage());
        }

        // 3. 兜底策略：如果是类，返回类名；如果是字段，返回字段名
        if (element instanceof Class<?> clazz) return clazz.getSimpleName();
        if (element instanceof Field field) return field.getName();
        return "未知名称";
    }
}