package cn.dreamtof.core.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文注册表
 * <p>
 * 用于显式注册和管理 {@link ThreadContextCopier} 和 {@link CurrentOperatorSupplier}。
 * 支持通过 Class 类型进行注册和卸载，编译期安全。
 * </p>
 */
public class ContextRegistry {

    private static final Map<Class<? extends ThreadContextCopier>, ThreadContextCopier> COPIER_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Class<? extends CurrentOperatorSupplier>, CurrentOperatorSupplier> SUPPLIER_REGISTRY = new ConcurrentHashMap<>();

    private static volatile boolean registryModeEnabled = false;

    public static void enableRegistryMode(boolean enabled) {
        registryModeEnabled = enabled;
    }

    public static boolean isRegistryModeEnabled() {
        return registryModeEnabled;
    }

    /**
     * 注册 Copier（自动使用 Class 作为 key）
     */
    public static void registerCopier(ThreadContextCopier copier) {
        if (copier == null) {
            throw new IllegalArgumentException("Copier cannot be null");
        }
        COPIER_REGISTRY.put(copier.getClass(), copier);
    }

    /**
     * 通过 Class 卸载 Copier
     */
    public static void unregisterCopier(Class<? extends ThreadContextCopier> copierClass) {
        COPIER_REGISTRY.remove(copierClass);
    }

    /**
     * 通过 Class 获取 Copier
     */
    @SuppressWarnings("unchecked")
    public static <T extends ThreadContextCopier> T getCopier(Class<T> copierClass) {
        return (T) COPIER_REGISTRY.get(copierClass);
    }

    /**
     * 获取所有已注册的 Copier
     */
    public static List<ThreadContextCopier> getAllCopiers() {
        return List.copyOf(COPIER_REGISTRY.values());
    }

    /**
     * 注册 OperatorSupplier（自动使用 Class 作为 key）
     */
    public static void registerOperatorSupplier(CurrentOperatorSupplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        SUPPLIER_REGISTRY.put(supplier.getClass(), supplier);
    }

    /**
     * 通过 Class 卸载 OperatorSupplier
     */
    public static void unregisterOperatorSupplier(Class<? extends CurrentOperatorSupplier> supplierClass) {
        SUPPLIER_REGISTRY.remove(supplierClass);
    }

    /**
     * 通过 Class 获取 OperatorSupplier
     */
    @SuppressWarnings("unchecked")
    public static <T extends CurrentOperatorSupplier> T getOperatorSupplier(Class<T> supplierClass) {
        return (T) SUPPLIER_REGISTRY.get(supplierClass);
    }

    /**
     * 获取第一个 OperatorSupplier
     */
    public static CurrentOperatorSupplier getFirstOperatorSupplier() {
        return SUPPLIER_REGISTRY.values().stream().findFirst().orElse(null);
    }

    /**
     * 清空所有注册
     */
    public static void clear() {
        COPIER_REGISTRY.clear();
        SUPPLIER_REGISTRY.clear();
    }
}
