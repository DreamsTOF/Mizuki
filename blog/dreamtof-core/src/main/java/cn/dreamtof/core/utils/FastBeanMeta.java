package cn.dreamtof.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ⚡ FastBeanMeta: 基于 LambdaMetafactory 的极致性能元数据访问引擎
 * <p>
 * 为什么要有这个类：
 * 1. 性能：传统反射 (Method.invoke) 每次调用都要检查权限和参数，无法被 JIT 内联。
 * LambdaMetafactory 会生成字节码，性能等同于直接调用 get/set 方法。
 * 2. 内存：相比 ThreadLocal 或简单的 Map，使用 ClassValue 避免了 ClassLoader 导致的内存泄漏。
 * </p>
 */
@Slf4j
public class FastBeanMeta {

    /** 目标类的 Class 对象 */
    private final Class<?> targetClass;

    /** 高性能实例化器：绑定无参构造函数 */
    private final Supplier<Object> instantiator;

    /** 字段访问器缓存：懒加载单个字段的访问器 */
    private final Map<String, FieldAccessor> accessorCache = new ConcurrentHashMap<>();

    /** 预计算的属性清单：仅包含具备完整读写能力的属性，用于高性能迭代（如深拷贝或全量审计） */
    private final List<FieldAccessor> writeableAccessors;

    /**
     * 全局静态缓存。
     * 为什么用 ClassValue：它专门为类关联元数据设计，比 ConcurrentHashMap 更快，
     * 且在类卸载时会自动清理，彻底杜绝 Metaspace OOM。
     */
    private static final ClassValue<FastBeanMeta> CACHE = new ClassValue<>() {
        @Override
        protected FastBeanMeta computeValue(@NonNull Class<?> type) {
            return new FastBeanMeta(type);
        }
    };

    /**
     * 获取指定类的元数据中心
     * @param clazz 目标类
     * @return FastBeanMeta 实例
     */
    public static FastBeanMeta of(Class<?> clazz) {
        return CACHE.get(clazz);
    }

    private FastBeanMeta(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.instantiator = createInstantiator();
        // 在构造阶段就解析所有写回属性，为后续的 List 遍历（深拷贝/Diff）提供 $O(1)$ 的支持
        this.writeableAccessors = resolveWriteableAccessors();
    }

    /**
     * 字段访问器封装
     * @param fieldName 原始字段名
     * @param field 对应的 Field 对象（供外部解析注解使用）
     * @param getter 高性能 Lambda Getter
     * @param setter 高性能 Lambda Setter
     * @param fieldType 字段类型
     */
    public record FieldAccessor(
            String fieldName,
            Field field,
            Function<Object, Object> getter,
            BiConsumer<Object, Object> setter,
            Class<?> fieldType
    ) {}

    /**
     * 极速实例化对象
     * 为什么：绕过 Class.newInstance() 的安全检查，直接触发生成的构造指令。
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance() {
        if (instantiator == null) {
            throw new RuntimeException("未找到可访问的无参构造函数: " + targetClass.getName());
        }
        return (T) instantiator.get();
    }

    /**
     * 获取预计算的读写属性列表
     * 为什么：在深拷贝场景下，我们通常需要遍历所有属性，直接遍历 List 比反复查找 Map 快得多。
     */
    public List<FieldAccessor> getWriteableAccessors() {
        return writeableAccessors;
    }

    /**
     * 获取指定字段的访问器
     */
    public FieldAccessor getAccessor(String fieldName) {
        return accessorCache.computeIfAbsent(fieldName, this::createAccessor);
    }

    /**
     * 解析所有具备读写能力的字段
     */
    private List<FieldAccessor> resolveWriteableAccessors() {
        List<FieldAccessor> list = new ArrayList<>();
        // 利用 FastReflectUtils 获取类及其父类的所有字段
        for (Field field : FastReflectUtils.getAllFields(targetClass)) {
            FieldAccessor accessor = getAccessor(field.getName());
            if (accessor != null && accessor.getter() != null && accessor.setter() != null) {
                list.add(accessor);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 核心实现：创建单个字段的 Lambda 绑定
     */
    private FieldAccessor createAccessor(String fieldName) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // 尝试突破模块化权限限制 (Java 9+)
            lookup = MethodHandles.privateLookupIn(targetClass, lookup);
        } catch (Exception ignored) {}

        Field field = FastReflectUtils.getField(targetClass, fieldName);
        Class<?> type = (field != null) ? field.getType() : Object.class;

        return new FieldAccessor(
                fieldName,
                field,
                createGetter(lookup, fieldName),
                createSetter(lookup, fieldName),
                type
        );
    }

    /**
     * 生成构造函数 Lambda (Supplier)
     */
    private Supplier<Object> createInstantiator() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup());
            Constructor<?> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            MethodHandle handle = lookup.unreflectConstructor(constructor);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup, "get", MethodType.methodType(Supplier.class),
                    MethodType.methodType(Object.class), handle, MethodType.methodType(targetClass)
            );
            return (Supplier<Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            log.warn("类 {} 无法绑定高性能实例化器，将降级处理", targetClass.getName());
            return null;
        }
    }

    /**
     * 生成 Getter Lambda (Function)
     * 支持 getXXX, isXXX 命名规范。
     */
    @SuppressWarnings("unchecked")
    private Function<Object, Object> createGetter(MethodHandles.Lookup lookup, String fieldName) {
        Method getterMethod = findMethod(fieldName, true);
        if (getterMethod == null) return null;
        try {
            MethodHandle handle = lookup.unreflect(getterMethod);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup, "apply", MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    handle, MethodType.methodType(getterMethod.getReturnType(), targetClass)
            );
            return (Function<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            return obj -> { try { return getterMethod.invoke(obj); } catch (Exception ex) { return null; } };
        }
    }

    /**
     * 生成 Setter Lambda (BiConsumer)
     */
    @SuppressWarnings("unchecked")
    private BiConsumer<Object, Object> createSetter(MethodHandles.Lookup lookup, String fieldName) {
        Method setterMethod = findMethod(fieldName, false);
        if (setterMethod == null) return null;
        try {
            MethodHandle handle = lookup.unreflect(setterMethod);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup, "accept", MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle, MethodType.methodType(void.class, targetClass, setterMethod.getParameterTypes()[0])
            );
            return (BiConsumer<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            return (obj, val) -> { try { setterMethod.invoke(obj, val); } catch (Exception ignored) {} };
        }
    }

    private Method findMethod(String fieldName, boolean isGetter) {
        String cap = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (isGetter) {
            Method m = findMethodRecursive(targetClass, "get" + cap, 0);
            if (m == null) m = findMethodRecursive(targetClass, "is" + cap, 0);
            return m;
        } else {
            return findMethodRecursive(targetClass, "set" + cap, 1);
        }
    }

    private Method findMethodRecursive(Class<?> clazz, String name, int paramCount) {
        Class<?> searchType = clazz;
        while (searchType != null && searchType != Object.class) {
            for (Method m : searchType.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    m.setAccessible(true);
                    return m;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }


    // 在 FastBeanMeta.java 中添加：
    public Collection<FieldAccessor> getAllAccessors() {
        // 确保所有字段都已生成 accessor 并在缓存中
        for (Field f : FastReflectUtils.getAllFields(targetClass)) {
            getAccessor(f.getName());
        }
        return accessorCache.values();
    }
}