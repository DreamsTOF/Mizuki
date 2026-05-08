package cn.dreamtof.core.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
public class TraceIdHandler {

    /**
     * 获取或生成 UUIDv7
     */
    public static UUID fetchTraceId(String header) {

        // 1. 如果为空，直接调用你选中的方法生成 UUIDv7
        if (header == null || header.isBlank()) {
            return UuidCreator.getTimeOrderedEpoch();
        }
        try {
            // 2. 外部传了 ID，必须是标准 UUID 格式（v4 或 v7 均可）
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            // 3. 非标 ID 直接拒绝，维护日志库的“血统纯正”
            log.warn("非法 TraceId 尝试入侵: [{}]", header);
            throw new IllegalArgumentException("Invalid X-Trace-Id. Standard UUID format is required.");
        }
    }

    public static UUID createUUID() {
        log.warn("获取或生成 UUIDv7");
        return UuidCreator.getTimeOrderedEpoch();
    }
}