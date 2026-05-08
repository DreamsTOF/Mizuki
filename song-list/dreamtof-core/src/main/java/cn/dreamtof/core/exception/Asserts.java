package cn.dreamtof.core.exception;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Asserts - 工业级防御性编程与领域校验工具类
 * <p>
 * 1. 布尔判断模式：返回 true/false，用于业务逻辑分支路由。
 * 2. 异常断言模式：强制要求传入 message 或 errorCode，校验失败抛出 BusinessException。
 */
public class Asserts {

    /** 默认错误码：参数错误 */
    private static final IErrorCode DEFAULT_ERROR = CommonErrorCode.PARAM_ERROR;

    /** 递归深度阈值，防止深度校验时发生栈溢出 */
    private static final int MAX_DEPTH = 8;

    // 常用正则表达式预编译
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]*$");

    private Asserts() {}

    // ========================================================================
    // 1. 布尔判断模式 (Boolean Mode - 单一参数，用于业务分支判断)
    // ========================================================================

    public static boolean isNull(Object value) { return value == null; }
    public static boolean isNotNull(Object value) { return value != null; }
    public static boolean isEmpty(Object value) { return ObjectUtils.isEmpty(value); }
    public static boolean isNotEmpty(Object value) { return ObjectUtils.isNotEmpty(value); }
    public static boolean isBlank(CharSequence value) { return StringUtils.isBlank(value); }
    public static boolean isNotBlank(CharSequence value) { return StringUtils.isNotBlank(value); }

    public static boolean isDeepEmpty(Object value) { return isDeepEmpty(value, 0); }
    public static boolean isDeepNotEmpty(Object value) { return !isDeepEmpty(value); }

    private static boolean isDeepEmpty(Object value, int depth) {
        if (depth >= MAX_DEPTH || ObjectUtils.isEmpty(value)) return true;
        return switch (value) {
            case CharSequence cs -> StringUtils.isBlank(cs);
            case Collection<?> coll -> {
                for (Object item : coll) {
                    if (!isDeepEmpty(item, depth + 1)) yield false;
                }
                yield true;
            }
            case Map<?, ?> map -> {
                for (Object val : map.values()) {
                    if (!isDeepEmpty(val, depth + 1)) yield false;
                }
                yield true;
            }
            case Object[] arr -> {
                for (Object item : arr) {
                    if (!isDeepEmpty(item, depth + 1)) yield false;
                }
                yield true;
            }
            default -> false;
        };
    }

    public static <T extends Comparable<T>> boolean isInRange(T value, T min, T max) {
        if (value == null) return false;
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public static boolean isEmail(String value) {
        return StringUtils.isNotEmpty(value) && EMAIL_PATTERN.matcher(value).matches();
    }

    public static boolean isMobile(String value) {
        return StringUtils.isNotEmpty(value) && MOBILE_PATTERN.matcher(value).matches();
    }

    public static boolean isIdCard(String value) {
        return StringUtils.isNotEmpty(value) && ID_CARD_PATTERN.matcher(value).matches();
    }

    public static boolean isNumeric(String value) {
        return StringUtils.isNotEmpty(value) && NUMERIC_PATTERN.matcher(value).matches();
    }

    // ========================================================================
    // 2. 异常断言模式 (Assertion Mode - 强制要求上下文信息)
    // ========================================================================

    // --- 深度非空断言 ---
    public static void deepNotEmpty(Object value, String message) { deepNotEmpty(value, DEFAULT_ERROR, message); }
    public static void deepNotEmpty(Object value, IErrorCode errorCode) { deepNotEmpty(value, errorCode, null); }
    public static void deepNotEmpty(Object value, IErrorCode errorCode, String message) {
        if (isDeepEmpty(value)) fail(errorCode, message);
    }

    // --- 基础非空断言 ---
    public static void notNull(Object value, String message) { notNull(value, DEFAULT_ERROR, message); }
    public static void notNull(Object value, IErrorCode errorCode) { notNull(value, errorCode, null); }
    public static void notNull(Object value, IErrorCode errorCode, String message) {
        if (isNull(value)) fail(errorCode, message);
    }

    public static void notEmpty(Object value, String message) { notEmpty(value, DEFAULT_ERROR, message); }
    public static void notEmpty(Object value, IErrorCode errorCode) { notEmpty(value, errorCode, null); }
    public static void notEmpty(Object value, IErrorCode errorCode, String message) {
        if (isEmpty(value)) fail(errorCode, message);
    }

    public static void notBlank(CharSequence value, String message) { notBlank(value, DEFAULT_ERROR, message); }
    public static void notBlank(CharSequence value, IErrorCode errorCode) { notBlank(value, errorCode, null); }
    public static void notBlank(CharSequence value, IErrorCode errorCode, String message) {
        if (isBlank(value)) fail(errorCode, message);
    }

    // --- 逻辑与状态断言 ---
    public static void isTrue(boolean expression, String message) { isTrue(expression, DEFAULT_ERROR, message); }
    public static void isTrue(boolean expression, IErrorCode errorCode) { isTrue(expression, errorCode, null); }
    public static void isTrue(boolean expression, IErrorCode errorCode, String message) {
        if (!expression) fail(errorCode, message);
    }

    public static void isFalse(boolean expression, String message) { isFalse(expression, DEFAULT_ERROR, message); }
    public static void isFalse(boolean expression, IErrorCode errorCode) { isFalse(expression, errorCode, null); }
    public static void isFalse(boolean expression, IErrorCode errorCode, String message) {
        if (expression) fail(errorCode, message);
    }

    public static void equals(Object actual, Object expected, String message) { equals(actual, expected, DEFAULT_ERROR, message); }
    public static void equals(Object actual, Object expected, IErrorCode errorCode) { equals(actual, expected, errorCode, null); }
    public static void equals(Object actual, Object expected, IErrorCode errorCode, String message) {
        if (!Objects.equals(actual, expected)) fail(errorCode, message);
    }

    public static <T> void mustIn(T value, Collection<T> candidates, String message) { mustIn(value, candidates, DEFAULT_ERROR, message); }
    public static <T> void mustIn(T value, Collection<T> candidates, IErrorCode errorCode) { mustIn(value, candidates, errorCode, null); }
    public static <T> void mustIn(T value, Collection<T> candidates, IErrorCode errorCode, String message) {
        if (value == null || !candidates.contains(value)) fail(errorCode, message);
    }

    // --- 数值与并发控制 ---
    public static void isOne(int count, String message) { isOne(count, DEFAULT_ERROR, message); }
    public static void isOne(int count, IErrorCode errorCode) { isOne(count, errorCode, null); }
    public static void isOne(int count, IErrorCode errorCode, String message) {
        if (count != 1) fail(errorCode, message);
    }

    public static void isPositive(Number number, String message) { isPositive(number, DEFAULT_ERROR, message); }
    public static void isPositive(Number number, IErrorCode errorCode) { isPositive(number, errorCode, null); }
    public static void isPositive(Number number, IErrorCode errorCode, String message) {
        if (number == null || number.doubleValue() <= 0) fail(errorCode, message);
    }

    public static <T extends Comparable<T>> void range(T value, T min, T max, String message) { range(value, min, max, DEFAULT_ERROR, message); }
    public static <T extends Comparable<T>> void range(T value, T min, T max, IErrorCode errorCode) { range(value, min, max, errorCode, null); }
    public static <T extends Comparable<T>> void range(T value, T min, T max, IErrorCode errorCode, String message) {
        if (!isInRange(value, min, max)) fail(errorCode, message);
    }

    // --- 集合一致性 ---
    public static <T> void allMatch(Collection<T> collection, Predicate<T> predicate, String message) { allMatch(collection, predicate, DEFAULT_ERROR, message); }
    public static <T> void allMatch(Collection<T> collection, Predicate<T> predicate, IErrorCode errorCode) { allMatch(collection, predicate, errorCode, null); }
    public static <T> void allMatch(Collection<T> collection, Predicate<T> predicate, IErrorCode errorCode, String message) {
        if (collection == null || !collection.stream().allMatch(predicate)) fail(errorCode, message);
    }

    public static void noNullElements(Collection<?> collection, String message) { noNullElements(collection, DEFAULT_ERROR, message); }
    public static void noNullElements(Collection<?> collection, IErrorCode errorCode) { noNullElements(collection, errorCode, null); }
    public static void noNullElements(Collection<?> collection, IErrorCode errorCode, String message) {
        if (collection != null) {
            for (Object element : collection) {
                if (element == null) fail(errorCode, message);
            }
        }
    }

    // --- 格式校验 ---
    public static void isEmail(String value, String message) { isEmail(value, DEFAULT_ERROR, message); }
    public static void isEmail(String value, IErrorCode errorCode) { isEmail(value, errorCode, null); }
    public static void isEmail(String value, IErrorCode errorCode, String message) {
        if (!isEmail(value)) fail(errorCode, message);
    }

    public static void isMobile(String value, String message) { isMobile(value, DEFAULT_ERROR, message); }
    public static void isMobile(String value, IErrorCode errorCode) { isMobile(value, errorCode, null); }
    public static void isMobile(String value, IErrorCode errorCode, String message) {
        if (!isMobile(value)) fail(errorCode, message);
    }

    public static void isIdCard(String value, String message) { isIdCard(value, DEFAULT_ERROR, message); }
    public static void isIdCard(String value, IErrorCode errorCode) { isIdCard(value, errorCode, null); }
    public static void isIdCard(String value, IErrorCode errorCode, String message) {
        if (!isIdCard(value)) fail(errorCode, message);
    }

    public static void isNumeric(String value, String message) { isNumeric(value, DEFAULT_ERROR, message); }
    public static void isNumeric(String value, IErrorCode errorCode) { isNumeric(value, errorCode, null); }
    public static void isNumeric(String value, IErrorCode errorCode, String message) {
        if (!isNumeric(value)) fail(errorCode, message);
    }

    public static void match(String value, String regex, String message) { match(value, regex, DEFAULT_ERROR, message); }
    public static void match(String value, String regex, IErrorCode errorCode) { match(value, regex, errorCode, null); }
    public static void match(String value, String regex, IErrorCode errorCode, String message) {
        if (StringUtils.isNotEmpty(value) && !Pattern.matches(regex, value)) fail(errorCode, message);
    }

    // ========================================================================
    // 3. 核心抛错辅助 (Internal Helper)
    // ========================================================================

    public static void fail(IErrorCode errorCode, String message) {
        String finalMessage = StringUtils.isNotBlank(message) ? message : errorCode.getMessage();
        throw new BusinessException(errorCode, finalMessage);
    }

    public static void fail(IErrorCode errorCode) {
        throw new BusinessException(errorCode);
    }

    public static void fail(IErrorCode errorCode, String message, Throwable cause) {
        String finalMessage = StringUtils.isNotBlank(message) ? message : errorCode.getMessage();
        throw new BusinessException(errorCode, finalMessage, cause);
    }

    public static void fail(IErrorCode errorCode, Throwable cause) {
        throw new BusinessException(errorCode, cause);
    }

    public static void must(boolean expression, IErrorCode errorCode) {
        if (!expression) fail(errorCode);
    }

    public static void must(boolean expression, IErrorCode errorCode, String message) {
        if (!expression) fail(errorCode, message);
    }
}