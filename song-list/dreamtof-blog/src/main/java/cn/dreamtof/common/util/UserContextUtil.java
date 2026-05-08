package cn.dreamtof.common.util;

import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.context.Operator;
import cn.dreamtof.core.exception.Asserts;
import com.github.f4b6a3.uuid.UuidCreator;

import java.io.Serializable;
import java.util.UUID;

/**
 * 用户上下文工具类
 * <p>
 * 提供便捷的当前登录用户ID获取方法，封装 OperationContext 的访问逻辑。
 * 适用于需要获取当前登录用户信息的业务场景。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 获取当前用户ID（未登录会抛异常）
 * UUID userId = UserContextUtil.getCurrentUserId();
 *
 * // 获取当前用户ID（未登录返回null）
 * UUID userId = UserContextUtil.getCurrentUserIdOrNull();
 *
 * // 检查用户是否已登录
 * if (UserContextUtil.isLoggedIn()) {
 *     // 已登录逻辑
 * }
 * </pre>
 *
 * @author dreamtof
 * @see OperationContext
 */
public final class UserContextUtil {

    private UserContextUtil() {
        // 工具类禁止实例化
    }
    private static final UUID DEFAULT_USER_ID = new UUID(0L, 0L);

    /**
     * 获取当前登录用户ID
     * <p>
     * 如果用户未登录，会抛出业务异常。
     * 适用于必须登录才能访问的业务场景。
     * </p>
     *
     * @return 当前登录用户ID（非空）
     */
    public static UUID getCurrentUserId() {
        Operator operator = OperationContext.get().getOperator();
        Asserts.notNull(operator, "用户未登录");
        Serializable id = operator.getId();
        Asserts.notNull(id, "用户ID不能为空");
        return UUID.fromString(id.toString());
    }

    /**
     * 获取当前登录用户ID（可为空）
     * <p>
     * 如果用户未登录，返回 null。
     * 适用于可选登录的业务场景（如记录点击日志）。
     * </p>
     *
     * @return 当前登录用户ID，未登录返回 null
     */
    public static UUID getCurrentUserIdOrNull() {
        Operator operator = OperationContext.get().getOperator();
        if (operator == null || operator.getId() == null) {
            return null;
        }
        try {
            return UUID.fromString(operator.getId().toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getCurrentUserIdOrDef() {
        Operator operator = OperationContext.get().getOperator();
        if (operator == null || operator.getId() == null) {
            return DEFAULT_USER_ID;
        }
        try {
            return UUID.fromString(operator.getId().toString());
        } catch (Exception e) {
            return DEFAULT_USER_ID;
        }
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true - 已登录，false - 未登录
     */
    public static boolean isLoggedIn() {
        Operator operator = OperationContext.get().getOperator();
        return operator != null && operator.getId() != null;
    }

    /**
     * 获取当前登录用户名
     * <p>
     * 如果用户未登录，返回 "Guest"。
     * </p>
     *
     * @return 当前登录用户名，未登录返回 "Guest"
     */
    public static String getCurrentUsername() {
        return OperationContext.get().getOperatorName();
    }

    /**
     * 获取当前登录用户ID（字符串形式）
     * <p>
     * 如果用户未登录，返回 null。
     * </p>
     *
     * @return 当前登录用户ID字符串，未登录返回 null
     */
    public static String getCurrentUserIdString() {
        Operator operator = OperationContext.get().getOperator();
        if (operator == null || operator.getId() == null) {
            return null;
        }
        return operator.getId().toString();
    }

    // ========================================================================
    // 终端信息获取
    // ========================================================================

    /**
     * 获取客户端IP地址
     * <p>
     * 如果上下文未绑定，返回默认地址 "127.0.0.1"。
     * </p>
     *
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        return OperationContext.get().getClientIp();
    }

    /**
     * 获取用户代理（User-Agent）
     * <p>
     * 如果上下文未绑定或不存在，返回 null。
     * </p>
     *
     * @return User-Agent字符串，不存在返回 null
     */
    public static String getUserAgent() {
        return OperationContext.get().getUserAgent();
    }

    /**
     * 获取设备ID
     * <p>
     * 如果上下文未绑定，返回 "UNKNOWN-DEVICE"。
     * </p>
     *
     * @return 设备ID
     */
    public static String getDeviceId() {
        return OperationContext.get().getDeviceId();
    }

    // ========================================================================
    // 扩展字段获取
    // ========================================================================

    /**
     * 获取扩展字段值
     * <p>
     * 从 OperationContext 的 extensions 中获取指定 key 的值，
     * 返回 Object 类型，由使用方自行处理类型转换。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * // 获取字符串类型的扩展字段
     * String value = (String) UserContextUtil.getExtension("customKey");
     *
     * // 获取整数类型的扩展字段
     * Integer count = (Integer) UserContextUtil.getExtension("visitCount");
     *
     * // 安全获取并进行类型判断
     * Object value = UserContextUtil.getExtension("someKey");
     * if (value instanceof String) {
     *     String strValue = (String) value;
     * }
     * </pre>
     *
     * @param key 扩展字段的键
     * @return 扩展字段的值，不存在返回 null
     */
    public static Object getExtension(String key) {
        if (key == null) {
            return null;
        }
        OperationContext.OperationInfo info = OperationContext.get();
        if (info.getExtensions() == null) {
            return null;
        }
        return info.getExtensions().get(key);
    }

    /**
     * 获取扩展字段值（带默认值）
     * <p>
     * 如果扩展字段不存在，返回指定的默认值。
     * </p>
     *
     * @param key 扩展字段的键
     * @param defaultValue 默认值
     * @return 扩展字段的值，不存在返回 defaultValue
     */
    public static Object getExtensionOrDefault(String key, Object defaultValue) {
        Object value = getExtension(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 检查扩展字段是否存在
     *
     * @param key 扩展字段的键
     * @return true - 存在，false - 不存在
     */
    public static boolean hasExtension(String key) {
        if (key == null) {
            return false;
        }
        OperationContext.OperationInfo info = OperationContext.get();
        return info.getExtensions() != null && info.getExtensions().containsKey(key);
    }
}
