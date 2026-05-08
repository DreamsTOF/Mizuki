package cn.dreamtof.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * 全局统一 JSON 工具类 (Jackson 封装)
 * <p>
 * 1. 统一时区、时间格式处理
 * 2. 内部处理 Checked Exception，转换为 RuntimeException
 * 3. 支持深度泛型解析（List, Map, Result<T> 等）
 */
@Slf4j
public class JsonUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // --- 1. 基础时间配置 ---
        // 显式设置时区为东八区
        MAPPER.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        // 注册 Java8 时间模块 (LocalDate, LocalDateTime)
        MAPPER.registerModule(new JavaTimeModule());
        // 禁用：将日期序列化为时间戳（默认开启，关闭后将变为标准的 ISO-8601）
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // --- 2. 兼容性配置 ---
        // 序列化：允许序列化空的 Bean (不抛出异常)
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化：忽略 JSON 中存在但 Java 对象中不存在的属性
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 反序列化：将整数强制转为 Long，防止 int 溢出
        MAPPER.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
    }

    /**
     * 将对象转换为 JSON 字符串
     * <p>扩展用法：常用于日志记录、Redis 存储或 HTTP 请求体构建</p>
     * @param obj 任意 Java 对象
     * @return JSON 字符串，若对象为 null 则返回 null
     */
    public static String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String str) return str;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /**
     * 将对象转换为 格式化后的 JSON 字符串 (美化输出)
     * <p>扩展用法：用于控制台打印、生成配置文件或导出 JSON 文件阅读</p>
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON pretty serialization error", e);
        }
    }

    /**
     * 将 JSON 字符串解析为指定的 Java 对象
     * <p>扩展用法：JsonUtils.parseObject(json, User.class)</p>
     * @param json  JSON 字符串
     * @param clazz 目标类的 Class
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON parsing error", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 List 集合
     * <p>扩展用法：JsonUtils.parseList(json, User.class) -> 返回 List&lt;User&gt;</p>
     * <p>注意：Jackson 直接 parse 会变成 List&lt;Map&gt;，此方法解决了泛型擦除问题</p>
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("JSON parsing to List error", e);
        }
    }

    /**
     * 万能泛型解析 (支持多层嵌套泛型)
     * <p>扩展用法：JsonUtils.parseReference(json, new TypeReference&lt;Result&lt;List&lt;User&gt;&gt;&gt;(){})</p>
     */
    public static <T> T parseReference(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("JSON parsing reference error", e);
        }
    }

    /**
     * 将 JSON 解析为树状结构模型
     * <p>扩展用法：当你只想取 JSON 里的某一个深层字段，而不想写 POJO 时</p>
     * <pre>
     * JsonNode node = JsonUtils.parseTree(json);
     * String name = node.path("user").path("profile").get("name").asText();
     * </pre>
     */
    public static JsonNode parseTree(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("JSON parse to tree error", e);
        }
    }

    /**
     * 创建一个空的 ObjectNode (用于动态组装 JSON)
     * <pre>
     * ObjectNode node = JsonUtils.createObjectNode();
     * node.put("id", 1);
     * node.put("name", "gemini");
     * </pre>
     */
    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * 创建一个空的 ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }

    /**
     * 对象转换 (深拷贝、Map 转 Bean)
     * <p>扩展用法：将一个 Map 映射到一个具体的 DTO 对象，或者在两个结构相似的 Bean 间转换</p>
     * <pre>
     * User user = JsonUtils.convert(map, User.class);
     * </pre>
     */
    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        if (fromValue == null) return null;
        return MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * 对象局部更新
     * <p>扩展用法：将 JSON 里的值覆盖到已有的对象实例上（常用于 PATCH 请求）</p>
     * @param json 新的数据源 JSON
     * @param targetObject 需要被更新的对象
     */
    public static void update(String json, Object targetObject) {
        try {
            MAPPER.readerForUpdating(targetObject).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException("JSON update error", e);
        }
    }
}