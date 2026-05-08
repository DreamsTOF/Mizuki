package cn.dreamtof.core.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 缓存 key 生成工具类
 * 已移除 Hutool 依赖，改用 Apache Commons Codec 和 Jackson
 *
 * @author dream
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存key (JSON + MD5)
     *
     * @param obj 要生成key的对象
     * @return MD5哈希后的缓存key
     */
    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtils.md5Hex("null");
        }

        try {
            // 1. 使用 Jackson 将对象序列化为 JSON 字符串
            // 2. 使用 Apache Commons Codec 生成 MD5 哈希
            String jsonStr = JsonUtils.MAPPER.writeValueAsString(obj);
            return DigestUtils.md5Hex(jsonStr);
        } catch (Exception e) {
            // 如果 JSON 序列化失败（例如存在循环引用），则退化到使用 toString
            // 确保工具类的健壮性，不轻易抛出异常导致业务中断
            return DigestUtils.md5Hex(String.valueOf(obj));
        }
    }
}
