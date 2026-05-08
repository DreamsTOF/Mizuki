package cn.dreamtof.common.web.utils;

import cn.dreamtof.auth.domain.exception.AuthErrorCode;
import cn.dreamtof.core.exception.Asserts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ChallengeManager {

    private static final String CHALLENGE_PREFIX = "crypto:challenge:";
    private static final long CHALLENGE_TTL_SECONDS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static RedisTemplate<String, String> redisTemplate;
    private static int challengeProbability;
    private static int timeLockChallengeIterations;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        ChallengeManager.redisTemplate = redisTemplate;
    }

    @Value("${security.crypto.zkp.challenge-probability:5}")
    public void setChallengeProbability(int prob) {
        ChallengeManager.challengeProbability = prob;
    }

    @Value("${security.crypto.zkp.time-lock-challenge-iterations:${TIME_LOCK_ITERATIONS:200}}")
    public void setTimeLockChallengeIterations(int iterations) {
        ChallengeManager.timeLockChallengeIterations = iterations;
    }

    @PostConstruct
    public void init() {
        log.info("ChallengeManager 初始化完成 (probability={}%, iterations={})", challengeProbability, timeLockChallengeIterations);
    }

    public static boolean shouldChallenge() {
        if (challengeProbability <= 0) return false;
        if (challengeProbability >= 100) return true;
        return RANDOM.nextInt(100) < challengeProbability;
    }

    public static int getTimeLockChallengeIterations() {
        return timeLockChallengeIterations;
    }

    private static final String LUA_ISSUE_CHALLENGE =
            "local k = KEYS[1]\n" +
            "local chalKey = KEYS[2]\n" +
            "local challengeBase64 = ARGV[1]\n" +
            "local count = ARGV[2]\n" +
            "local ttl = tonumber(ARGV[3])\n" +
            "redis.call('SET', chalKey, challengeBase64, 'EX', tostring(ttl))\n" +
            "redis.call('HSET', k, 'pendingChallengeNonce', challengeBase64, 'pendingChallengeCount', count)\n" +
            "redis.call('EXPIRE', k, ttl)\n" +
            "return {1,'OK'}";

    private static final String LUA_VERIFY_CHALLENGE =
            "local k = KEYS[1]\n" +
            "local chalKey = KEYS[2]\n" +
            "local count = tonumber(ARGV[1])\n" +
            "local ttl = tonumber(ARGV[2])\n" +
            "local pendingNonce = redis.call('HGET', k, 'pendingChallengeNonce')\n" +
            "local pendingCount = redis.call('HGET', k, 'pendingChallengeCount')\n" +
            "local challengeBase64 = nil\n" +
            "if pendingNonce and pendingCount and tonumber(pendingCount) + 1 == count then\n" +
            "  challengeBase64 = pendingNonce\n" +
            "else\n" +
            "  challengeBase64 = redis.call('GET', chalKey)\n" +
            "  if challengeBase64 then redis.call('DEL', chalKey) end\n" +
            "end\n" +
            "if not challengeBase64 then return {0,'NO_CHALLENGE'} end\n" +
            "return {1, challengeBase64}";

    private static final String LUA_CONFIRM_CHALLENGE =
            "local k = KEYS[1]\n" +
            "local ttl = tonumber(ARGV[1])\n" +
            "redis.call('HDEL', k, 'pendingChallengeNonce', 'pendingChallengeCount')\n" +
            "redis.call('EXPIRE', k, ttl)\n" +
            "return {1,'OK'}";

    public static String issueChallenge(String keyId, long count) {
        byte[] r = new byte[32];
        RANDOM.nextBytes(r);
        String challengeBase64 = Base64.getEncoder().encodeToString(r);
        String redisKey = CHALLENGE_PREFIX + keyId + ":" + count;
        String stateKey = "crypto:keystate:" + keyId;

        try {
            List<Object> result = KeyStatePersistence.evalLuaStatic(
                    LUA_ISSUE_CHALLENGE,
                    new String[]{stateKey, redisKey},
                    new String[]{challengeBase64, String.valueOf(count), String.valueOf(CHALLENGE_TTL_SECONDS)});
            if (result != null && result.size() >= 2) {
                log.debug("挑战下发成功 (Lua): keyId={} count={}", keyId, count);
            }
        } catch (Exception e) {
            log.warn("挑战下发 Lua 失败，降级到非原子操作: keyId={}", keyId, e);
            redisTemplate.opsForValue().set(redisKey, challengeBase64, CHALLENGE_TTL_SECONDS, TimeUnit.SECONDS);
            AesKeyManager.KeyState state = AesKeyManager.persistence.load(keyId);
            if (state != null) {
                state.pendingChallengeNonce = challengeBase64;
                state.pendingChallengeCount = count;
                AesKeyManager.persistence.save(keyId, state);
            }
        }

        return challengeBase64;
    }

    public static boolean verifyChallengeResponse(String keyId, long count, String requestKeyBase64,
                                                   String chalPosValue) throws Exception {
        String stateKey = "crypto:keystate:" + keyId;
        String chalRedisKey = CHALLENGE_PREFIX + keyId + ":" + (count - 1);
        String challengeBase64 = null;

        try {
            List<Object> result = KeyStatePersistence.evalLuaStatic(
                    LUA_VERIFY_CHALLENGE,
                    new String[]{stateKey, chalRedisKey},
                    new String[]{String.valueOf(count), String.valueOf(KEY_STATE_TTL_SECONDS)});
            if (result != null && result.size() >= 2 && longVal(result.get(0)) == 1) {
                challengeBase64 = strVal(result.get(1));
            }
        } catch (Exception e) {
            log.warn("挑战验证 Lua 失败，降级到非原子操作: keyId={}", keyId, e);
            AesKeyManager.KeyState state = AesKeyManager.persistence.load(keyId);
            if (state != null && state.pendingChallengeNonce != null && state.pendingChallengeCount != null) {
                if (state.pendingChallengeCount + 1 == count) {
                    challengeBase64 = state.pendingChallengeNonce;
                }
            }
            if (challengeBase64 == null) {
                challengeBase64 = redisTemplate.opsForValue().getAndDelete(chalRedisKey);
            }
        }

        if (challengeBase64 == null) return true;

        byte[] R = Base64.getDecoder().decode(challengeBase64);
        byte[] chalKey = NativeCryptoUtils.hmacSha256(
                Base64.getDecoder().decode(requestKeyBase64), R);

        byte[] countBytes = new byte[8];
        long c = count;
        for (int i = 7; i >= 0; i--) {
            countBytes[i] = (byte) (c & 0xFF);
            c >>= 8;
        }

        byte[] expectedProof = NativeCryptoUtils.timeLockedHkdf(
                chalKey, countBytes,
                "SCHNORR-TL-PROOF".getBytes(StandardCharsets.UTF_8),
                32, timeLockChallengeIterations);

        byte[] expectedPrefix = Arrays.copyOf(expectedProof, 16);
        String expectedUuid = SchnorrVerifier.bytesToUuidFragment(expectedPrefix);

        boolean verified = expectedUuid.equals(chalPosValue);

        if (verified) {
            try {
                KeyStatePersistence.evalLuaStatic(
                        LUA_CONFIRM_CHALLENGE,
                        new String[]{stateKey},
                        new String[]{String.valueOf(KEY_STATE_TTL_SECONDS)});
            } catch (Exception e) {
                log.warn("挑战确认 Lua 失败: keyId={}", keyId, e);
            }
        }

        return verified;
    }

    private static final long KEY_STATE_TTL_SECONDS = Duration.ofHours(2).toSeconds();

    private static long longVal(Object o) {
        if (o instanceof Long) return (Long) o;
        if (o instanceof byte[]) {
            try { return Long.parseLong(new String((byte[]) o, StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        }
        if (o instanceof Integer) return ((Integer) o).longValue();
        return 0;
    }

    private static String strVal(Object o) {
        if (o instanceof byte[]) return new String((byte[]) o, StandardCharsets.UTF_8);
        return String.valueOf(o);
    }
}
