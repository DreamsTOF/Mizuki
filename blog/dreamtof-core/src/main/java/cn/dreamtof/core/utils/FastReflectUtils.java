package cn.dreamtof.core.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * ⚡ 全局高性能反射与克隆工具类 (增强版)
 * 整合了 LambdaMetafactory 访问引擎，提供接近原生代码的属性读写与对象拷贝性能。
 */
public class FastReflectUtils {

    /** 类的所有字段列表缓存 */
    private static final Cache<Class<?>, List<Field>> FIELD_CACHE = Caffeine.newBuilder()
            .maximumSize(5000)
            .weakKeys()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build();

    /** 类的字段名映射缓存 */
    private static final Cache<Class<?>, Map<String, Field>> FIELD_MAP_CACHE = Caffeine.newBuilder()
            .maximumSize(5000)
            .weakKeys()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build();

    // --- 1. 基础高性能反射方法 ---

    /**
     * 高性能创建实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        return FastBeanMeta.of(clazz).newInstance();
    }

    /**
     * 【增强】高性能获取属性值 (String版本 - 自动类型转换)
     * @param target 目标对象
     * @param fieldName 字段名
     * @param clazz 期望的返回类型类
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPropertyValue(Object target, String fieldName, Class<T> clazz) {
        Object value = getPropertyValue(target, fieldName);
        return value == null ? null : (T) value;
    }

    /**
     * 高性能获取属性值 (String版本 - 返回 Object)
     */
    public static Object getPropertyValue(Object target, String fieldName) {
        if (target == null) return null;
        var accessor = FastBeanMeta.of(target.getClass()).getAccessor(fieldName);
        return (accessor != null && accessor.getter() != null)
                ? accessor.getter().apply(target)
                : null;
    }

    /**
     * 【推荐】使用 Getter 方法引用获取属性值 (自动推断返回类型)
     */
    public static <T, R> R getPropertyValue(T target, Function<T, R> getter) {
        if (target == null || getter == null) return null;
        return getter.apply(target);
    }

    /**
     * 【增强】高性能设置属性值 (String版本)
     */
    public static void setPropertyValue(Object target, String fieldName, Object value) {
        if (target == null) return;
        var accessor = FastBeanMeta.of(target.getClass()).getAccessor(fieldName);
        if (accessor != null && accessor.setter() != null) {
            accessor.setter().accept(target, value);
        }
    }

    /**
     * 【推荐】使用 Setter 方法引用设置属性值
     */
    public static <T, V> void setPropertyValue(T target, BiConsumer<T, V> setter, V value) {
        if (target == null || setter == null) return;
        setter.accept(target, value);
    }


    // --- 2. 进阶：流式 (Fluent) 操作 API ---

    /**
     * 开启流式反射操作
     * @param target 目标对象
     */
    public static <T> FastReflectWrapper<T> on(T target) {
        return new FastReflectWrapper<>(target);
    }

    /**
     * 流式包装类，提供强类型检查的读写体验
     */
    public static class FastReflectWrapper<T> {
        private final T target;

        private FastReflectWrapper(T target) {
            this.target = target;
        }

        /** 获取值：根据 Getter 自动推断返回类型 */
        public <R> R get(Function<T, R> getter) {
            return target == null ? null : getter.apply(target);
        }

        /** 设置值：支持链式调用 */
        public <V> FastReflectWrapper<T> set(BiConsumer<T, V> setter, V value) {
            if (target != null && setter != null) {
                setter.accept(target, value);
            }
            return this;
        }

        /** 获取原始对象 */
        public T get() {
            return target;
        }
    }

    // --- 3. 核心拷贝方法 (保留原逻辑) ---

    /** 极速浅拷贝 */
    @SuppressWarnings("unchecked")
    public static <T> T shallowCopy(T source) {
        if (source == null) return null;
        Class<T> clazz = (Class<T>) source.getClass();
        T target = newInstance(clazz);
        var accessors = FastBeanMeta.of(clazz).getWriteableAccessors();
        for (var accessor : accessors) {
            accessor.setter().accept(target, accessor.getter().apply(source));
        }
        return target;
    }

    /** 高性能深拷贝 */
    public static <T> T deepCopy(T source) {
        return deepCopy(source, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static <T> T deepCopy(T source, Map<Object, Object> visited) {
        if (source == null) return null;
        Class<?> clazz = source.getClass();
        if (isSimpleType(clazz)) return source;
        if (visited.containsKey(source)) return (T) visited.get(source);

        if (source instanceof Collection<?>) {
            Collection<Object> target = (source instanceof List) ? new ArrayList<>() : new HashSet<>();
            visited.put(source, target);
            for (Object item : (Collection<?>) source) {
                target.add(deepCopy(item, visited));
            }
            return (T) target;
        }

        if (source instanceof Map<?, ?>) {
            Map<Object, Object> target = new HashMap<>();
            visited.put(source, target);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
                target.put(deepCopy(entry.getKey(), visited), deepCopy(entry.getValue(), visited));
            }
            return (T) target;
        }

        try {
            T target = (T) newInstance(clazz);
            visited.put(source, target);
            var accessors = FastBeanMeta.of(clazz).getWriteableAccessors();
            for (var accessor : accessors) {
                Object val = accessor.getter().apply(source);
                accessor.setter().accept(target, deepCopy(val, visited));
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed: " + clazz.getName(), e);
        }
    }

    // --- 4. 辅助内部方法 ---

    public static List<Field> getAllFields(Class<?> clazz) {
        return FIELD_CACHE.get(clazz, FastReflectUtils::resolveAllFields);
    }

    /**
     * 根据字段名快速获取对应的 Field 对象 (从缓存获取)
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return FIELD_MAP_CACHE.get(clazz, c -> {
            Map<String, Field> map = new ConcurrentHashMap<>();
            for (Field f : getAllFields(c)) {
                map.putIfAbsent(f.getName(), f);
            }
            return map;
        }).get(fieldName);
    }


    private static List<Field> resolveAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> temp = clazz;
        while (temp != null && temp != Object.class) {
            for (Field field : temp.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    fields.add(field);
                } catch (Exception ignored) {}
            }
            temp = temp.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
    }

    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz == Character.class;
    }
}