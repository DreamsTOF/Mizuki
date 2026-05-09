package cn.dreamtof.common.web;

import cn.dreamtof.common.web.utils.AesKeyManager;
import cn.dreamtof.common.web.utils.SaltFactory;
import cn.dreamtof.core.base.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Tag(name = "认证模块/秘钥握手", description = "后量子密钥握手")
@RestController
@RequestMapping("/auth/crypto")
public class CryptoController {

    private static final int EXCHANGE_RATE_LIMIT_PER_IP = 10;
    private static final long RATE_LIMIT_WINDOW_MS = 60_000;
    private static final long RATE_LIMIT_CLEANUP_INTERVAL_MS = 300_000;
    private static volatile long lastCleanupTime = System.currentTimeMillis();
    private static final Map<String, RateLimitEntry> EXCHANGE_RATE_LIMITS = new ConcurrentHashMap<>();

    @GetMapping("/kem-public")
    public BaseResponse<String> getKemPublicKey() {
        String publicKey = AesKeyManager.getKemPublicKeyBase64();
        return new BaseResponse<>(200, publicKey, "获取 ML-KEM 公钥成功", true);
    }

    @PostMapping("/exchange")
    public BaseResponse<Map<String, String>> exchangeKey(@RequestBody ExchangeRequest request,
                                                          @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        try {
            if (deviceId == null || deviceId.isBlank()) {
                return new BaseResponse<>(400, null, "缺少设备标识", false);
            }
            if (!checkExchangeRateLimit(deviceId)) {
                return new BaseResponse<>(429, null, "请求过于频繁，请稍后再试", false);
            }

            if (request.getCiphertext() == null) {
                return new BaseResponse<>(400, null, "参数错误", false);
            }
            String keyId = AesKeyManager.registerAesKey(request.getCiphertext());
            String compositeSalt = SaltFactory.buildCurrentCompositeSalt();
            Map<String, String> result = Map.of("keyId", keyId, "compositeSalt", compositeSalt);
            return new BaseResponse<>(200, result, "秘钥交换成功", true);
        } catch (Exception e) {
            return new BaseResponse<>(500, null, "秘钥交换失败: " + e.getMessage(), false);
        }
    }

    private boolean checkExchangeRateLimit(String clientId) {
        long now = System.currentTimeMillis();
        maybeCleanupRateLimits(now);
        RateLimitEntry entry = EXCHANGE_RATE_LIMITS.compute(clientId, (k, v) -> {
            if (v == null || now - v.windowStart > RATE_LIMIT_WINDOW_MS) {
                return new RateLimitEntry(now, new AtomicInteger(1));
            }
            v.count.incrementAndGet();
            return v;
        });
        return entry.count.get() <= EXCHANGE_RATE_LIMIT_PER_IP;
    }

    private synchronized void maybeCleanupRateLimits(long now) {
        if (now - lastCleanupTime < RATE_LIMIT_CLEANUP_INTERVAL_MS) return;
        lastCleanupTime = now;
        EXCHANGE_RATE_LIMITS.entrySet().removeIf(e ->
                now - e.getValue().windowStart > RATE_LIMIT_WINDOW_MS);
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count;
        RateLimitEntry(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    @Data
    public static class ExchangeRequest {
        private String ciphertext;
    }
}
