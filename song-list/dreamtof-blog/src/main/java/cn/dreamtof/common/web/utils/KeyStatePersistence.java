package cn.dreamtof.common.web.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KeyStatePersistence {

    private static final String KEY_PREFIX = "crypto:keystate:";
    private static final String FINGERPRINT_PREFIX = "crypto:fingerprint:";
    private static final String HISTORY_PREFIX = "crypto:history:";
    private static final long KEY_TTL_SECONDS = Duration.ofHours(2).toSeconds();
    private static final int HISTORY_MAX_SIZE = 30;
    private static final int MAX_EVOLVE_STEPS_LUA = 100;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // ========== Lua 脚本常量 ==========

    private static final String LUA_ADVANCE_STATE =
            "local k = KEYS[1]\n" +
            "local newC = tonumber(ARGV[1]); local evolveKey = ARGV[2]\n" +
            "local now = tonumber(ARGV[3]); local maxSteps = tonumber(ARGV[4]); local ttl = tonumber(ARGV[5])\n" +
            "local chainType = tonumber(ARGV[6])\n" +
            "if chainType == 0 then\n" +
            "  local ccRaw = redis.call('HGET', k, 'consumedCounter')\n" +
            "  if not ccRaw then return {0,'NOT_FOUND',0} end\n" +
            "  local cc = tonumber(ccRaw)\n" +
            "  if newC <= cc then return {0,'STALE',0} end\n" +
            "  if newC - cc > maxSteps then return {0,'TOO_LARGE',0} end\n" +
            "  redis.call('HSET', k, 'currentEvolveKeyBase64', evolveKey, 'consumedCounter', tostring(newC), 'lastAccessTime', tostring(now))\n" +
            "else\n" +
            "  local ccRaw = redis.call('HGET', k, 'pendingConsumedCounter')\n" +
            "  local cc = ccRaw and tonumber(ccRaw) or -1\n" +
            "  if newC <= cc then return {0,'STALE',1} end\n" +
            "  if cc >= 0 and newC - cc > maxSteps then return {0,'TOO_LARGE',1} end\n" +
            "  redis.call('HSET', k, 'pendingEvolveKeyBase64', evolveKey, 'pendingConsumedCounter', tostring(newC), 'lastAccessTime', tostring(now))\n" +
            "  local cnt = tonumber(redis.call('HGET', k, 'pendingConfirmCount') or '0') + 1\n" +
            "  local thr = tonumber(redis.call('HGET', k, 'pendingConfirmThreshold') or '5')\n" +
            "  redis.call('HSET', k, 'pendingConfirmCount', tostring(cnt))\n" +
            "  if cnt >= thr then\n" +
            "    local oldMsk = redis.call('HGET', k, 'mskBase64')\n" +
            "    local oldEv = redis.call('HGET', k, 'currentEvolveKeyBase64')\n" +
            "    local oldCc = redis.call('HGET', k, 'consumedCounter')\n" +
            "    redis.call('HSET', k, 'preEvolveMskBase64', oldMsk, 'preEvolveEvolveKeyBase64', oldEv, 'preEvolveConsumedCounter', oldCc)\n" +
            "    local newMsk = redis.call('HGET', k, 'pendingMskBase64')\n" +
            "    local newEv = redis.call('HGET', k, 'pendingEvolveKeyBase64')\n" +
            "    local newCc = redis.call('HGET', k, 'pendingConsumedCounter')\n" +
            "    redis.call('HSET', k, 'mskBase64', newMsk, 'currentEvolveKeyBase64', newEv, 'consumedCounter', newCc)\n" +
            "    redis.call('HSET', k, 'lastEvolveAt', tostring(newC))\n" +
            "    redis.call('HDEL', k, 'pendingMskBase64', 'pendingEvolveKeyBase64', 'pendingConsumedCounter', 'pendingConfirmCount', 'pendingConfirmThreshold', 'pendingSsHash', 'ev_priv_enc')\n" +
            "    redis.call('HSET', k, 'signalSentAtCounter', '-1', 'evolutionDeadline', '-1')\n" +
            "    redis.call('EXPIRE', k, ttl)\n" +
            "    return {1,'PROMOTED',1}\n" +
            "  end\n" +
            "end\n" +
            "redis.call('EXPIRE', k, ttl)\n" +
            "return {1,'OK',0}";

    private static final String LUA_MARK_EVOLUTION_SIGNAL =
            "local k = KEYS[1]\n" +
            "local sentAt = ARGV[1]; local deadline = ARGV[2]; local ttl = tonumber(ARGV[3])\n" +
            "local raw = redis.call('HGET', k, 'signalSentAtCounter')\n" +
            "if raw and tonumber(raw) >= 0 then return {0,'ALREADY_SENT'} end\n" +
            "redis.call('HSET', k, 'signalSentAtCounter', sentAt, 'evolutionDeadline', deadline)\n" +
            "redis.call('EXPIRE', k, ttl)\n" +
            "return {1,'OK'}";

    @PostConstruct
    public void initEarly() {
        INSTANCE = this;
        log.info("KeyStatePersistence 初始化完成 (prefix={}, ttl={}s, historyMax={})",
                KEY_PREFIX, KEY_TTL_SECONDS, HISTORY_MAX_SIZE);
    }

    // ========== KeyState Hash 操作 ==========

    public void save(String keyId, AesKeyManager.KeyState state) {
        try {
            redisTemplate.opsForHash().putAll(redisKey(keyId), stateToMap(state));
            redisTemplate.expire(redisKey(keyId), KEY_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("KeyState 持久化失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public void delete(String keyId) {
        try {
            redisTemplate.delete(redisKey(keyId));
            redisTemplate.delete(fingerprintKey(computeFingerprint(keyId)));
        } catch (Exception e) {
            log.warn("KeyState 删除失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public AesKeyManager.KeyState load(String keyId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey(keyId));
            if (entries == null || entries.isEmpty()) return null;
            return mapToState(entries);
        } catch (Exception e) {
            log.warn("KeyState 恢复失败 keyId={}: {}", keyId, e.getMessage());
            return null;
        }
    }

    public void updateTtl(String keyId) {
        redisTemplate.expire(redisKey(keyId), KEY_TTL_SECONDS, TimeUnit.SECONDS);
    }

    // ========== 指纹索引操作 ==========

    public static String computeFingerprint(String keyId) {
        return computeFingerprint(keyId, LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public static String computeFingerprint(String keyId, String date) {
        try {
            byte[] hash = NativeCryptoUtils.hmacSha256(
                    keyId.getBytes(StandardCharsets.UTF_8),
                    date.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) sb.append(String.format("%02x", hash[i] & 0xFF));
            return sb.toString();
        } catch (Exception e) {
            return "00000000";
        }
    }

    public void saveFingerprintIndex(String keyId) {
        try {
            String fingerprint = computeFingerprint(keyId);
            redisTemplate.opsForSet().add(fingerprintKey(fingerprint), keyId);
            redisTemplate.expire(fingerprintKey(fingerprint), KEY_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("指纹索引写入失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public void removeFingerprintIndex(String keyId) {
        try {
            String fingerprint = computeFingerprint(keyId);
            redisTemplate.opsForSet().remove(fingerprintKey(fingerprint), keyId);
        } catch (Exception e) {
            log.warn("指纹索引删除失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public Set<String> getKeyIdsByFingerprint(String fingerprint) {
        try {
            Set<String> ids = redisTemplate.opsForSet().members(fingerprintKey(fingerprint));
            return ids != null ? ids : Collections.emptySet();
        } catch (Exception e) {
            log.warn("指纹查询失败 fingerprint={}: {}", fingerprint, e.getMessage());
            return Collections.emptySet();
        }
    }

    // ========== 历史缓冲区操作 ==========

    public void saveToHistory(String keyId, String keyBase64, long counter, String markerBase64) {
        try {
            String hKey = historyKey(keyId);
            String entry = counter + ":" + keyBase64 + ":" + markerBase64;
            redisTemplate.opsForZSet().add(hKey, entry, (double) counter);
            Long size = redisTemplate.opsForZSet().size(hKey);
            if (size != null && size > HISTORY_MAX_SIZE) {
                Set<String> old = redisTemplate.opsForZSet().range(hKey, 0, size - HISTORY_MAX_SIZE - 1);
                if (old != null && !old.isEmpty())
                    redisTemplate.opsForZSet().remove(hKey, old.toArray(new String[0]));
            }
            redisTemplate.expire(hKey, Duration.ofMinutes(10).toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("历史缓冲区写入失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public static class HistoryEntry {
        public final long counter;
        public final String keyBase64;
        public final String markerBase64;
        public HistoryEntry(long counter, String keyBase64, String markerBase64) {
            this.counter = counter;
            this.keyBase64 = keyBase64;
            this.markerBase64 = markerBase64;
        }
    }

    public List<HistoryEntry> loadHistory(String keyId) {
        try {
            Set<String> entries = redisTemplate.opsForZSet().reverseRange(historyKey(keyId), 0, HISTORY_MAX_SIZE - 1);
            if (entries == null || entries.isEmpty()) return Collections.emptyList();
            List<HistoryEntry> result = new ArrayList<>();
            for (String entry : entries) {
                String[] parts = entry.split(":", 3);
                if (parts.length >= 2) {
                    long counter = Long.parseLong(parts[0]);
                    String keyBase64 = parts[1];
                    String markerBase64 = parts.length >= 3 ? parts[2] : "";
                    result.add(new HistoryEntry(counter, keyBase64, markerBase64));
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("历史缓冲区加载失败 keyId={}: {}", keyId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ========== Lua 脚本：统一 advanceState ==========

    public LuaResult advanceState(String keyId, long newCounter, String newEvolveKeyBase64, boolean isPendingChain) {
        try {
            String key = redisKey(keyId);
            String ttlStr = String.valueOf(KEY_TTL_SECONDS);
            List<Object> result = evalLua(LUA_ADVANCE_STATE, key,
                    String.valueOf(newCounter), newEvolveKeyBase64,
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(MAX_EVOLVE_STEPS_LUA), ttlStr,
                    isPendingChain ? "1" : "0");
            if (result != null && result.size() >= 3) {
                boolean success = longVal(result.get(0)) == 1;
                String message = strVal(result.get(1));
                boolean promoted = longVal(result.get(2)) == 1;
                return new LuaResult(success, message, promoted);
            }
            return new LuaResult(false, "UNKNOWN_ERROR", false);
        } catch (Exception e) {
            log.warn("Lua advanceState 失败 keyId={}: {}", keyId, e.getMessage());
            return new LuaResult(false, "REDIS_ERROR", false);
        }
    }

    // ========== Lua 脚本：原子标记进化信号 ==========

    public LuaResult markEvolutionSignal(String keyId, long sentAtCounter) {
        try {
            long window = EVOLVE_WINDOW_MIN + RANDOM.nextInt(EVOLVE_WINDOW_MAX - EVOLVE_WINDOW_MIN + 1);
            long deadline = sentAtCounter + window;
            String ttlStr = String.valueOf(KEY_TTL_SECONDS);
            List<Object> result = evalLua(LUA_MARK_EVOLUTION_SIGNAL, keyId,
                    String.valueOf(sentAtCounter), String.valueOf(deadline), ttlStr);
            if (result != null && result.size() >= 2) {
                boolean success = longVal(result.get(0)) == 1;
                String message = strVal(result.get(1));
                return new LuaResult(success, message, false);
            }
            return new LuaResult(false, "UNKNOWN_ERROR", false);
        } catch (Exception e) {
            log.warn("Lua markEvolutionSignal 失败 keyId={}: {}", keyId, e.getMessage());
            return new LuaResult(false, "REDIS_ERROR", false);
        }
    }

    // ========== 进化私钥加密存储 ==========

    public void saveEvolutionPrivateKey(String keyId, byte[] encryptedPrivKey) {
        try {
            redisTemplate.opsForHash().put(redisKey(keyId), "ev_priv_enc",
                    Base64.getEncoder().encodeToString(encryptedPrivKey));
            redisTemplate.expire(redisKey(keyId), KEY_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("进化私钥存储失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    public byte[] loadEvolutionPrivateKey(String keyId) {
        try {
            Object raw = redisTemplate.opsForHash().get(redisKey(keyId), "ev_priv_enc");
            if (raw == null) return null;
            return Base64.getDecoder().decode((String) raw);
        } catch (Exception e) {
            log.warn("进化私钥恢复失败 keyId={}: {}", keyId, e.getMessage());
            return null;
        }
    }

    public void deleteEvolutionPrivateKey(String keyId) {
        try {
            redisTemplate.opsForHash().delete(redisKey(keyId), "ev_priv_enc");
        } catch (Exception e) {
            log.warn("进化私钥删除失败 keyId={}: {}", keyId, e.getMessage());
        }
    }

    // ========== Lua 执行引擎 ==========

    private List<Object> evalLua(String script, String key, String... args) {
        return redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection conn) -> {
            byte[][] keysArr = { key.getBytes(StandardCharsets.UTF_8) };
            byte[][] argsArr = new byte[args.length][];
            for (int i = 0; i < args.length; i++)
                argsArr[i] = args[i].getBytes(StandardCharsets.UTF_8);
            byte[][] all = new byte[1 + argsArr.length][];
            all[0] = keysArr[0];
            System.arraycopy(argsArr, 0, all, 1, argsArr.length);
            return conn.eval(script.getBytes(StandardCharsets.UTF_8), ReturnType.MULTI, 1, all);
        }, true);
    }

    public static List<Object> evalLuaStatic(String script, String[] keys, String[] args) {
        return INSTANCE.redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection conn) -> {
            byte[][] keysArr = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++)
                keysArr[i] = keys[i].getBytes(StandardCharsets.UTF_8);
            byte[][] argsArr = new byte[args.length][];
            for (int i = 0; i < args.length; i++)
                argsArr[i] = args[i].getBytes(StandardCharsets.UTF_8);
            byte[][] all = new byte[keysArr.length + argsArr.length][];
            System.arraycopy(keysArr, 0, all, 0, keysArr.length);
            System.arraycopy(argsArr, 0, all, keysArr.length, argsArr.length);
            return conn.eval(script.getBytes(StandardCharsets.UTF_8), ReturnType.MULTI, keys.length, all);
        }, true);
    }

    private static KeyStatePersistence INSTANCE;

    private static long longVal(Object o) {
        if (o instanceof Long) return (Long) o;
        if (o instanceof byte[]) {
            try { return Long.parseLong(new String((byte[]) o, StandardCharsets.UTF_8)); } catch (Exception ignored) {}
        }
        return 0;
    }

    private static String strVal(Object o) {
        if (o instanceof byte[]) return new String((byte[]) o, StandardCharsets.UTF_8);
        return String.valueOf(o);
    }

    // ========== 常量 ==========

    private static final int EVOLVE_WINDOW_MIN = 30;
    private static final int EVOLVE_WINDOW_MAX = 200;

    // ========== 结果对象 ==========

    public static class LuaResult {
        public final boolean success;
        public final String message;
        public final boolean promoted;
        public LuaResult(boolean success, String message, boolean promoted) {
            this.success = success; this.message = message; this.promoted = promoted;
        }
    }

    // ========== KeyState ⇔ Hash Map 转换 ==========

    public static Map<String, String> stateToMap(AesKeyManager.KeyState state) {
        Map<String, String> map = new LinkedHashMap<>();
        put(map, "mskBase64", state.mskBase64);
        put(map, "currentEvolveKeyBase64", state.currentEvolveKeyBase64);
        map.put("consumedCounter", String.valueOf(state.consumedCounter));
        map.put("lastAccessTime", String.valueOf(state.lastAccessTime));
        put(map, "preEvolveMskBase64", state.preEvolveMskBase64);
        put(map, "preEvolveEvolveKeyBase64", state.preEvolveEvolveKeyBase64);
        map.put("preEvolveConsumedCounter", String.valueOf(state.preEvolveConsumedCounter));
        put(map, "pendingMskBase64", state.pendingMskBase64);
        put(map, "pendingEvolveKeyBase64", state.pendingEvolveKeyBase64);
        map.put("pendingConsumedCounter", String.valueOf(state.pendingConsumedCounter));
        map.put("pendingConfirmCount", String.valueOf(state.pendingConfirmCount));
        map.put("pendingConfirmThreshold", String.valueOf(state.pendingConfirmThreshold));
        put(map, "pendingSsHash", state.pendingSsHash);
        map.put("pendingCreatedAt", String.valueOf(state.pendingCreatedAt));
        map.put("signalSentAtCounter", String.valueOf(state.signalSentAtCounter));
        map.put("evolutionDeadline", String.valueOf(state.evolutionDeadline));
        map.put("lastEvolveAt", String.valueOf(state.lastEvolveAt));
        map.put("fingerprintPos", String.valueOf(state.fingerprintPos));
        map.put("dateRefPos", String.valueOf(state.dateRefPos));
        put(map, "evolutionKeyPairPublic", state.evolutionKeyPairPublic);
        put(map, "pendingChallengeNonce", state.pendingChallengeNonce);
        map.put("pendingChallengeCount", state.pendingChallengeCount != null ? String.valueOf(state.pendingChallengeCount) : "");
        return map;
    }

    public static AesKeyManager.KeyState mapToState(Map<Object, Object> map) {
        String mskBase64 = getStr(map, "mskBase64");
        if (mskBase64 == null) return null;
        AesKeyManager.KeyState state = new AesKeyManager.KeyState(mskBase64);
        state.currentEvolveKeyBase64 = getStr(map, "currentEvolveKeyBase64", mskBase64);
        state.consumedCounter = getLong(map, "consumedCounter", -1);
        state.lastAccessTime = getLong(map, "lastAccessTime", System.currentTimeMillis());
        state.preEvolveMskBase64 = getStr(map, "preEvolveMskBase64");
        state.preEvolveEvolveKeyBase64 = getStr(map, "preEvolveEvolveKeyBase64");
        state.preEvolveConsumedCounter = getLong(map, "preEvolveConsumedCounter", -1);
        state.pendingMskBase64 = getStr(map, "pendingMskBase64");
        state.pendingEvolveKeyBase64 = getStr(map, "pendingEvolveKeyBase64");
        state.pendingConsumedCounter = getLong(map, "pendingConsumedCounter", -1);
        state.pendingConfirmCount = (int) getLong(map, "pendingConfirmCount", 0);
        state.pendingConfirmThreshold = (int) getLong(map, "pendingConfirmThreshold", 5);
        state.pendingSsHash = getStr(map, "pendingSsHash");
        state.pendingCreatedAt = getLong(map, "pendingCreatedAt", -1);
        state.signalSentAtCounter = getLong(map, "signalSentAtCounter", -1);
        state.evolutionDeadline = getLong(map, "evolutionDeadline", -1);
        state.lastEvolveAt = getLong(map, "lastEvolveAt", 0);
        state.fingerprintPos = (int) getLong(map, "fingerprintPos", 0);
        state.dateRefPos = (int) getLong(map, "dateRefPos", 0);
        state.evolutionKeyPairPublic = getStr(map, "evolutionKeyPairPublic");
        state.pendingChallengeNonce = getStr(map, "pendingChallengeNonce");
        String chalCountStr = getStr(map, "pendingChallengeCount");
        state.pendingChallengeCount = (chalCountStr != null && !chalCountStr.isEmpty()) ? Long.parseLong(chalCountStr) : null;
        return state;
    }

    private static void put(Map<String, String> m, String k, String v) { if (v != null) m.put(k, v); }
    private static String getStr(Map<Object, Object> m, String k) { Object v = m.get(k); return v instanceof String ? (String) v : null; }
    private static String getStr(Map<Object, Object> m, String k, String d) { String v = getStr(m, k); return v != null ? v : d; }
    private static long getLong(Map<Object, Object> m, String k, long d) {
        Object v = m.get(k);
        if (v instanceof String) { try { return Long.parseLong((String) v); } catch (NumberFormatException ignored) {} }
        return d;
    }

    // ========== Key 生成 ==========

    static String redisKey(String keyId) { return KEY_PREFIX + keyId; }
    static String fingerprintKey(String fp) { return FINGERPRINT_PREFIX + fp; }
    static String historyKey(String keyId) { return HISTORY_PREFIX + keyId; }
}
