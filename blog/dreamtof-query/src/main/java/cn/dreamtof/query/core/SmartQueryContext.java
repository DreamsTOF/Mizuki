package cn.dreamtof.query.core;

import cn.dreamtof.query.annotation.QueryMapping;
import cn.dreamtof.query.annotation.Relation;
import cn.dreamtof.query.annotation.SmartFetch;
import cn.dreamtof.core.annotation.Check;
import cn.dreamtof.core.utils.FastReflectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🚀 SmartQuery 全局上下文中心 (元数据情报库)
 */
@Slf4j
public class SmartQueryContext {

    @Getter
    private static volatile ObjectMapper objectMapper;
    private static final Map<Class<?>, TableInfo> TABLE_CACHE = new ConcurrentHashMap<>();

    public record VoFieldMeta(String name, Class<?> type, boolean isCollection, Class<?> componentType, SmartFetch smartFetch, Relation relation) {}
    public record DtoFieldMeta(String name, Class<?> type, QueryMapping mapping, Check check) {}

    private static final Cache<Class<?>, List<VoFieldMeta>> VO_META_CACHE = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofHours(24)).build();
    private static final Cache<Class<?>, Map<String, DtoFieldMeta>> DTO_META_CACHE = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofHours(24)).build();
    private static final Cache<Class<?>, Map<String, String>> COL_TO_PROP_CACHE = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofHours(24)).build();

    public static void init(ObjectMapper om) { objectMapper = om; }
    public static TableInfo getTableInfo(Class<?> entityClass) { return TABLE_CACHE.computeIfAbsent(entityClass, TableInfoFactory::ofEntityClass); }

    public static Map<String, DtoFieldMeta> getDtoFields(Class<?> clazz) {
        return DTO_META_CACHE.get(clazz, c -> {
            Map<String, DtoFieldMeta> map = new LinkedHashMap<>();
            // 【改造点】：直接使用 Core 模块的极速反射工具
            for (Field f : FastReflectUtils.getAllFields(c)) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                map.putIfAbsent(f.getName(), new DtoFieldMeta(f.getName(), f.getType(), f.getAnnotation(QueryMapping.class), f.getAnnotation(Check.class)));
            }
            return map;
        });
    }

    public static List<VoFieldMeta> getVoFields(Class<?> clazz) {
        return VO_META_CACHE.get(clazz, c -> {
            List<VoFieldMeta> metas = new ArrayList<>();
            // 【改造点】：直接使用 Core 模块的极速反射工具
            for (Field f : FastReflectUtils.getAllFields(c)) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                boolean isCol = Collection.class.isAssignableFrom(f.getType());
                Class<?> compType = f.getType();
                if (isCol) {
                    Type gt = f.getGenericType();
                    if (gt instanceof ParameterizedType pt) compType = (Class<?>) pt.getActualTypeArguments()[0];
                }
                metas.add(new VoFieldMeta(f.getName(), f.getType(), isCol, compType, f.getAnnotation(SmartFetch.class), f.getAnnotation(Relation.class)));
            }
            return metas;
        });
    }

    public static String getPropertyByColumn(Class<?> entityClass, String colName) {
        if (colName == null) return null;
        return COL_TO_PROP_CACHE.get(entityClass, c -> {
            Map<String, String> map = new HashMap<>();
            TableInfo info = getTableInfo(c);
            if (info != null) {
                for (Field f : FastReflectUtils.getAllFields(c)) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    String col = info.getColumnByProperty(f.getName());
                    if (col != null) map.put(col, f.getName());
                }
            }
            return map;
        }).get(colName);
    }

    @Configuration
    public static class SpringAutoConfig {
        public SpringAutoConfig(ObjectMapper objectMapper) { SmartQueryContext.init(objectMapper); }
    }
}