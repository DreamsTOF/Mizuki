package cn.dreamtof.common.web.utils;

import cn.dreamtof.auth.domain.exception.AuthErrorCode;
import cn.dreamtof.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AesKeyManager {

    private static final long KEY_TTL_MS = 3_600_000;

    private static final String EVOLVE_CT = "DreamToF-Evolution-Next";

    private static final int HEADER_COUNT = 16;

    private static final long KEM_ROTATION_INTERVAL_MS = 3_600_000;
    private static final long KEM_PREVIOUS_TTL_MS = 300_000;

    private static final double EVOLUTION_PROBABILITY = 0.05;

    private static final int EVOLVE_WINDOW_MIN = 30;
    private static final int EVOLVE_WINDOW_MAX = 200;

    static KeyStatePersistence persistence;

    private static final Map<String, KeyState> KEY_STATES = new ConcurrentHashMap<>();

    private static volatile KeyPair serverMlKemKeyPair;
    private static volatile KeyPair previousMlKemKeyPair;
    private static volatile long lastKemRotationTime = System.currentTimeMillis();
    private static volatile long previousKemKeyExpiryTime = 0;

    private static volatile byte[] evolutionEncryptionKey;

    private static final Map<String, KeyPair> EVOLUTION_KEY_PAIRS = new ConcurrentHashMap<>();

    static {
        try {
            serverMlKemKeyPair = NativeCryptoUtils.generateMlKemKeyPair();
            lastKemRotationTime = System.currentTimeMillis();
            evolutionEncryptionKey = deriveEvolutionEncryptionKey(serverMlKemKeyPair);
        } catch (Exception e) {
            log.error("初始化 ML-KEM 密钥对失败", e);
        }

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "key-state-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(AesKeyManager::evictExpiredKeyStates, KEY_TTL_MS, KEY_TTL_MS, TimeUnit.MILLISECONDS);
    }

    private static void evictExpiredKeyStates() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, KeyState>> it = KEY_STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, KeyState> entry = it.next();
            if (now - entry.getValue().lastAccessTime > KEY_TTL_MS) {
                it.remove();
                EVOLUTION_KEY_PAIRS.remove(entry.getKey());
            }
        }
    }

    @Autowired
    public void setPersistence(KeyStatePersistence p) {
        persistence = p;
    }

    public static String getKemPublicKeyBase64() {
        maybeRotateKemKeyPair();
        return Base64.getEncoder().encodeToString(serverMlKemKeyPair.getPublic().getEncoded());
    }

    private static void maybeClearPreviousKemKey() {
        if (previousMlKemKeyPair != null && System.currentTimeMillis() > previousKemKeyExpiryTime) {
            previousMlKemKeyPair = null;
        }
    }

    private static void maybeRotateKemKeyPair() {
        long now = System.currentTimeMillis();
        if (now - lastKemRotationTime < KEM_ROTATION_INTERVAL_MS) return;
        synchronized (AesKeyManager.class) {
            if (now - lastKemRotationTime < KEM_ROTATION_INTERVAL_MS) return;
            try {
                previousMlKemKeyPair = serverMlKemKeyPair;
                serverMlKemKeyPair = NativeCryptoUtils.generateMlKemKeyPair();
                lastKemRotationTime = now;
                previousKemKeyExpiryTime = now + KEM_PREVIOUS_TTL_MS;
                evolutionEncryptionKey = deriveEvolutionEncryptionKey(serverMlKemKeyPair);
                log.info("ML-KEM 密钥对已轮换");
            } catch (Exception e) {
                log.error("ML-KEM 密钥对轮换失败", e);
            }
        }
    }

    public static String registerAesKey(String ciphertextBase64) throws Exception {
        maybeClearPreviousKemKey();
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);
        byte[] sharedSecret = null;
        try {
            sharedSecret = NativeCryptoUtils.decapsulateMlKem(ciphertext, serverMlKemKeyPair.getPrivate());
        } catch (Exception e) {
            if (previousMlKemKeyPair != null) {
                sharedSecret = NativeCryptoUtils.decapsulateMlKem(ciphertext, previousMlKemKeyPair.getPrivate());
            } else {
                throw e;
            }
        }
        String keyId = NativeCryptoUtils.deriveKeyId(sharedSecret);
        String compositeSalt = SaltFactory.buildCurrentCompositeSalt();
        String mskBase64 = deriveMsk(sharedSecret, compositeSalt);
        KEY_STATES.put(keyId, new KeyState(mskBase64));
        if (persistence != null) {
            persistence.save(keyId, KEY_STATES.get(keyId));
            persistence.saveFingerprintIndex(keyId);
        }
        return keyId;
    }

    public static String deriveRequestKeyFromMsk(String mskBase64, int requestCount) throws Exception {
        String evolveKey = mskBase64;
        for (int i = 0; i <= requestCount; i++) {
            evolveKey = evolveOnce(evolveKey);
        }
        return evolveKey;
    }

    public static String evolveOnce(String currentKeyBase64) throws Exception {
        byte[] key = Base64.getDecoder().decode(currentKeyBase64);
        byte[] result = NativeCryptoUtils.hmacSha256(key, EVOLVE_CT.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(result);
    }

    private static KeyState getStateOrRestore(String keyId) throws Exception {
        KeyState state = KEY_STATES.get(keyId);
        if (state != null) {
            return state;
        }
        if (persistence != null) {
            state = persistence.load(keyId);
            if (state != null) {
                KEY_STATES.put(keyId, state);
                return state;
            }
        }
        throw new BusinessException(AuthErrorCode.CRYPTO_KEY_EXPIRED);
    }

    private static void evictIfExpired(KeyState state, String keyId) {
        long idle = System.currentTimeMillis() - state.lastAccessTime;
        if (idle > KEY_TTL_MS) {
            KEY_STATES.remove(keyId);
            throw new BusinessException(AuthErrorCode.CRYPTO_KEY_EXPIRED);
        }
    }

    private static String deriveMsk(byte[] sharedSecret, String compositeSalt) throws Exception {
        byte[] saltBytes = compositeSalt.getBytes(StandardCharsets.UTF_8);
        byte[] info = "DreamToF-ChaCha20-Session".getBytes(StandardCharsets.UTF_8);
        byte[] msk = NativeCryptoUtils.hkdfSha256(sharedSecret, saltBytes, info, 32);
        return Base64.getEncoder().encodeToString(msk);
    }

    public static Map<String, KeyState> getKeyStates() {
        return KEY_STATES;
    }

    // ========================================================================
    // --- 16-Header Position Computation ---
    // ========================================================================

    private static int computeKeyIdPos(String keyId, String label, int... avoid) throws Exception {
        int basePos = Math.abs(NativeCryptoUtils.hashMod(keyId, label, HEADER_COUNT));
        Set<Integer> used = new HashSet<>();
        for (int a : avoid) used.add(a);
        int pos = basePos;
        while (used.contains(pos)) {
            pos = (pos + 1) % HEADER_COUNT;
        }
        return pos;
    }

    private static int[] computeProofPositions(String keyId, int... avoid) throws Exception {
        Set<Integer> used = new HashSet<>();
        for (int a : avoid) used.add(a);
        int basePos = Math.abs(NativeCryptoUtils.hashMod(keyId, "PROOF_BASE_POS", HEADER_COUNT));
        int[] positions = new int[6];
        int idx = 0;
        int pos = basePos;
        while (idx < 6) {
            if (!used.contains(pos)) {
                positions[idx++] = pos;
                used.add(pos);
            }
            pos = (pos + 1) % HEADER_COUNT;
        }
        return positions;
    }

    // ========================================================================
    // --- 16-Header Key Identification (tryChain) ---
    // ========================================================================

    public static TryChainResult tryChainIdentifyAndConsume(String[] headers) throws Exception {
        if (headers == null || headers.length < HEADER_COUNT) {
            throw new BusinessException(AuthErrorCode.CRYPTO_VERSION_MISMATCH, "需要 16-Header，当前只有 " + (headers != null ? headers.length : 0));
        }

        Set<String> candidateKeyIds = new HashSet<>();
        if (persistence != null) {
            for (String headerUuid : headers) {
                if (headerUuid == null || headerUuid.isEmpty()) continue;
                String fp = extractFingerprintFromUuid(headerUuid);
                if (fp == null) continue;
                Set<String> ids = persistence.getKeyIdsByFingerprint(fp);
                if (ids != null) candidateKeyIds.addAll(ids);
            }
        }
        if (candidateKeyIds.isEmpty()) {
            candidateKeyIds = new HashSet<>(KEY_STATES.keySet());
        }

        for (String keyId : candidateKeyIds) {
            TryChainResult result = tryChainForCandidateAtomic(headers, keyId);
            if (result != null) {
                return result;
            }
        }

        throw new BusinessException(AuthErrorCode.CRYPTO_KEY_EXPIRED, "无法识别任何有效 keyId");
    }

    private static TryChainResult tryChainForCandidateAtomic(String[] headers, String keyId) throws Exception {
        int expectedPos = computeKeyIdPos(keyId, "EXPECTED_POS");
        int drPos = computeKeyIdPos(keyId, "DATE_REF_POS", expectedPos);
        int countPosVal = computeKeyIdPos(keyId, "COUNT_POS", expectedPos, drPos);
        int fpPos = computeKeyIdPos(keyId, "FP_POS", expectedPos, drPos, countPosVal);
        int chalPos = computeKeyIdPos(keyId, "CHAL_POS", expectedPos, drPos, countPosVal, fpPos);
        int evPos = computeKeyIdPos(keyId, "EV_SUBMIT", expectedPos, drPos, countPosVal, fpPos, chalPos);

        String drUuid = headers[drPos];
        String today = extractDateFromDrUuid(drUuid);

        String expectedMarker = computeMarker(keyId, today);
        String actualMarker = headers[expectedPos];
        if (actualMarker == null || !expectedMarker.equals(actualMarker)) {
            return null;
        }

        int requestCount = extractRequestCount(headers[countPosVal]);
        if (requestCount < 0) {
            return null;
        }

        int[] proofPositions = computeProofPositions(keyId, expectedPos, drPos, countPosVal, fpPos, chalPos, evPos);

        KeyState state = KEY_STATES.get(keyId);
        if (state == null && persistence != null) {
            state = persistence.load(keyId);
            if (state != null) KEY_STATES.put(keyId, state);
        }
        if (state == null) return null;

        CandidateSnapshot snapshot;
        synchronized (state) {
            evictIfExpired(state, keyId);

            if (requestCount <= state.consumedCounter) {
                return null;
            }

            int steps = requestCount - (int) state.consumedCounter;
            String evolveKey = state.currentEvolveKeyBase64;
            for (int i = 0; i < steps; i++) {
                evolveKey = evolveOnce(evolveKey);
            }

            snapshot = new CandidateSnapshot(
                    evolveKey, state.mskBase64, state.consumedCounter,
                    state.preEvolveMskBase64, state.currentEvolveKeyBase64);
        }

        boolean schnorrOk = verifySchnorrProof(headers, proofPositions, requestCount, keyId, snapshot.evolveKey);

        String fpValue = headers[fpPos];
        boolean fpOk = false;
        if (fpValue != null) {
            String expectedFp = KeyStatePersistence.computeFingerprint(keyId, today);
            String actualFp = extractFingerprintFromUuid(fpValue);
            fpOk = expectedFp.equals(actualFp);
        }

        String preEvolveKey = null;
        boolean preSchnorrOk = false;
        if (snapshot.preEvolveMskBase64 != null) {
            preEvolveKey = snapshot.preEvolveMskBase64;
            for (int i = 0; i <= requestCount; i++) {
                preEvolveKey = evolveOnce(preEvolveKey);
            }
            preSchnorrOk = verifySchnorrProof(headers, proofPositions, requestCount, keyId, preEvolveKey);
        }

        String verifiedEvolveKey = null;
        if (schnorrOk) {
            verifiedEvolveKey = snapshot.evolveKey;
        } else if (fpOk) {
            verifiedEvolveKey = snapshot.evolveKey;
        } else if (preSchnorrOk) {
            log.info("preEvolve fallback 命中 (atomic): keyId={}, count={}", keyId, requestCount);
            verifiedEvolveKey = preEvolveKey;
        }

        if (verifiedEvolveKey == null) {
            return null;
        }

        if (chalPos >= 0 && chalPos < HEADER_COUNT && headers[chalPos] != null) {
            try {
                boolean challengeOk = ChallengeManager.verifyChallengeResponse(
                        keyId, requestCount, verifiedEvolveKey, headers[chalPos]);
                if (!challengeOk) {
                    throw new BusinessException(AuthErrorCode.CHALLENGE_MISMATCH, "时间锁挑战验证失败");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("挑战验证异常: keyId={}", keyId, e);
            }
        }

        return confirmAndSetRequestKey(state, keyId, requestCount, verifiedEvolveKey, fpPos, chalPos, evPos);
    }

    private static TryChainResult confirmAndSetRequestKey(KeyState state, String keyId, int requestCount,
                                                           String requestKeyBase64, int fpPos, int chalPos, int evPos) throws Exception {
        synchronized (state) {
            if (requestCount <= state.consumedCounter) {
                return null;
            }

            String currentEvolveKey = state.currentEvolveKeyBase64;
            int steps = requestCount - (int) state.consumedCounter;
            for (int i = 0; i < steps; i++) {
                currentEvolveKey = evolveOnce(currentEvolveKey);
            }

            if (!currentEvolveKey.equals(requestKeyBase64)) {
                log.warn("confirmAndSetRequestKey: 进化在验证窗口期发生，拒绝本次请求 keyId={} count={}", keyId, requestCount);
                return null;
            }

            state.currentEvolveKeyBase64 = currentEvolveKey;
            state.consumedCounter = requestCount;
            state.lastAccessTime = System.currentTimeMillis();

            if (state.preEvolveMskBase64 != null && requestCount > state.preEvolveConsumedCounter + EVOLVE_WINDOW_MAX) {
                state.preEvolveMskBase64 = null;
                state.preEvolveEvolveKeyBase64 = null;
                state.preEvolveConsumedCounter = -1;
            }

            if (persistence != null) {
                persistence.advanceState(keyId, requestCount, currentEvolveKey, false);
            }
        }

        String algorithm = NativeCryptoUtils.getAlgorithmForRequest(requestCount);
        RequestKeyHolder.set(keyId, requestCount, algorithm, requestKeyBase64);
        return new TryChainResult(keyId, requestCount, requestKeyBase64, fpPos, chalPos, evPos);
    }

    private static class CandidateSnapshot {
        final String evolveKey;
        final String mskBase64;
        final long consumedCounter;
        final String preEvolveMskBase64;
        final String currentEvolveKeyBase64;

        CandidateSnapshot(String evolveKey, String mskBase64, long consumedCounter,
                          String preEvolveMskBase64, String currentEvolveKeyBase64) {
            this.evolveKey = evolveKey;
            this.mskBase64 = mskBase64;
            this.consumedCounter = consumedCounter;
            this.preEvolveMskBase64 = preEvolveMskBase64;
            this.currentEvolveKeyBase64 = currentEvolveKeyBase64;
        }
    }

    private static String computeMarker(String keyId, String today) throws Exception {
        byte[] seed = NativeCryptoUtils.hmacSha256(
                keyId.getBytes(StandardCharsets.UTF_8),
                ("MARKER-" + today).getBytes(StandardCharsets.UTF_8));
        byte[] fragment = Arrays.copyOf(seed, 16);
        return SchnorrVerifier.bytesToUuidFragment(fragment);
    }

    private static int extractRequestCount(String countUuid) {
        if (countUuid == null) return -1;
        try {
            byte[] bytes = SchnorrVerifier.uuidToBytes(countUuid);
            long count = 0;
            for (int i = 0; i < 8; i++) {
                count = (count << 8) | (bytes[i] & 0xFF);
            }
            if (count > Integer.MAX_VALUE) {
                log.warn("requestCount 超过 Integer.MAX_VALUE: {}", count);
                return -1;
            }
            return (int) count;
        } catch (Exception e) {
            return -1;
        }
    }

    private static boolean verifySchnorrProof(String[] headers, int[] proofPositions,
                                               int count, String keyId, String requestKeyBase64) {
        try {
            byte[][] fragments = new byte[6][16];
            for (int i = 0; i < 6; i++) {
                String uuid = headers[proofPositions[i]];
                if (uuid == null || uuid.length() < 32) return false;
                fragments[i] = SchnorrVerifier.uuidToBytes(uuid);
            }

            SchnorrVerifier.ProofReassembly proof = SchnorrVerifier.reassembleProof(fragments);

            byte[] expectedPubKey = SchnorrVerifier.derivePublicKey(requestKeyBase64).getEncoded();

            if (!Arrays.equals(proof.publicKey, expectedPubKey)) {
                return false;
            }

            byte[] message = SchnorrVerifier.buildMessage(count, keyId, requestKeyBase64);

            return SchnorrVerifier.verify(proof.publicKey, proof.signature, message);
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractDateFromDrUuid(String drUuid) {
        long ts = NativeCryptoUtils.extractUuidV7Timestamp(drUuid);
        if (ts > 0) {
            java.time.Instant instant = java.time.Instant.ofEpochMilli(ts);
            java.time.LocalDate date = instant.atZone(java.time.ZoneOffset.UTC).toLocalDate();
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String extractFingerprintFromUuid(String uuid) {
        if (uuid == null) return null;
        byte[] bytes = SchnorrVerifier.uuidToBytes(uuid);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(String.format("%02x", bytes[i] & 0xFF));
        return sb.toString();
    }

    // ========================================================================
    // --- Evolution Methods ---
    // ========================================================================

    public static boolean shouldTriggerEvolution(String keyId) {
        KeyState state = KEY_STATES.get(keyId);
        if (state == null) return false;
        if (state.signalSentAtCounter >= 0 || state.pendingMskBase64 != null || state.preEvolveMskBase64 != null) {
            if (state.signalSentAtCounter >= 0 && state.evolutionDeadline >= 0
                    && state.consumedCounter > state.evolutionDeadline) {
                synchronized (state) {
                    if (state.signalSentAtCounter >= 0 && state.consumedCounter > state.evolutionDeadline) {
                        log.info("进化 deadline 已过期，回滚进化状态: keyId={}, deadline={}, current={}",
                                keyId, state.evolutionDeadline, state.consumedCounter);
                        state.signalSentAtCounter = -1;
                        state.evolutionDeadline = -1;
                        state.evolutionKeyPairPublic = null;
                        EVOLUTION_KEY_PAIRS.remove(keyId);
                        if (persistence != null) {
                            persistence.save(keyId, state);
                        }
                    }
                }
            }
            return false;
        }
        if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() >= EVOLUTION_PROBABILITY) return false;
        synchronized (state) {
            if (state.signalSentAtCounter >= 0 || state.pendingMskBase64 != null || state.preEvolveMskBase64 != null) {
                return false;
            }
            return true;
        }
    }

    public static String triggerEvolution(String keyId, long currentCount) throws Exception {
        KeyState state = getStateOrRestore(keyId);
        synchronized (state) {
            if (state.signalSentAtCounter >= 0) return null;
            if (state.pendingMskBase64 != null) return null;

            KeyPair evolutionKp = NativeCryptoUtils.generateMlKemKeyPair();
            String publicKeyBase64 = Base64.getEncoder().encodeToString(evolutionKp.getPublic().getEncoded());

            EVOLUTION_KEY_PAIRS.put(keyId, evolutionKp);
            state.evolutionKeyPairPublic = publicKeyBase64;
            int window = EVOLVE_WINDOW_MIN + new java.security.SecureRandom().nextInt(EVOLVE_WINDOW_MAX - EVOLVE_WINDOW_MIN + 1);
            state.signalSentAtCounter = currentCount;
            state.evolutionDeadline = currentCount + window;

            if (persistence != null) {
                byte[] privKeyBytes = evolutionKp.getPrivate().getEncoded();
                byte[] encryptedPrivKey = NativeCryptoUtils.aesGcmEncrypt(
                        privKeyBytes, getEvolutionEncryptionKey());
                persistence.saveEvolutionPrivateKey(keyId, encryptedPrivKey);
                persistence.save(keyId, state);
                persistence.markEvolutionSignal(keyId, currentCount);
            }

            log.info("进化信号已下发: keyId={}, signalAtCounter={}", keyId, currentCount);
            return publicKeyBase64;
        }
    }

    public static void processEvolutionCt(String keyId, String ctBase64) throws Exception {
        KeyState state = getStateOrRestore(keyId);
        synchronized (state) {
            if (state.preEvolveMskBase64 != null && EVOLUTION_KEY_PAIRS.get(keyId) == null) {
                log.info("进化已完成（幂等跳过）: keyId={}", keyId);
                return;
            }

            KeyPair evKp = EVOLUTION_KEY_PAIRS.get(keyId);
            if (evKp == null && persistence != null) {
                byte[] encryptedPrivKey = persistence.loadEvolutionPrivateKey(keyId);
                if (encryptedPrivKey != null) {
                    byte[] privKeyBytes = NativeCryptoUtils.aesGcmDecrypt(encryptedPrivKey, getEvolutionEncryptionKey());
                    evKp = NativeCryptoUtils.reconstructMlKemKeyPair(
                            state.evolutionKeyPairPublic, privKeyBytes);
                    EVOLUTION_KEY_PAIRS.put(keyId, evKp);
                }
            }
            if (evKp == null) {
                log.warn("进化私钥不在内存中且无法从 Redis 恢复: keyId={}", keyId);
                return;
            }

            byte[] ct = Base64.getDecoder().decode(ctBase64);
            byte[] sharedSecret = NativeCryptoUtils.decapsulateMlKem(ct, evKp.getPrivate());

            byte[] currentMskBytes = Base64.getDecoder().decode(state.mskBase64);
            byte[] newMskBytes = NativeCryptoUtils.hkdfSha256(
                    sharedSecret, currentMskBytes,
                    "DreamToF-Evolution".getBytes(StandardCharsets.UTF_8), 32);
            String newMskBase64 = Base64.getEncoder().encodeToString(newMskBytes);

            state.preEvolveMskBase64 = state.mskBase64;
            state.preEvolveEvolveKeyBase64 = state.currentEvolveKeyBase64;
            state.preEvolveConsumedCounter = state.consumedCounter;

            state.mskBase64 = newMskBase64;
            state.currentEvolveKeyBase64 = newMskBase64;
            state.consumedCounter = -1;

            state.signalSentAtCounter = -1;
            state.evolutionDeadline = -1;
            state.evolutionKeyPairPublic = null;

            if (persistence != null) {
                persistence.save(keyId, state);
                persistence.deleteEvolutionPrivateKey(keyId);
            }

            EVOLUTION_KEY_PAIRS.remove(keyId);

            log.info("密钥进化完成: keyId={}", keyId);
        }
    }

    public static String getPkFieldName(String requestKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(requestKeyBase64);
        StringBuilder sb = new StringBuilder("_pk_");
        for (int i = 0; i < 4; i++) sb.append(String.format("%02x", keyBytes[i] & 0xFF));
        return sb.toString();
    }

    public static long getEvolutionDeadline(String keyId) {
        KeyState state = KEY_STATES.get(keyId);
        return state != null ? state.evolutionDeadline : -1;
    }

    private static byte[] getEvolutionEncryptionKey() {
        return evolutionEncryptionKey;
    }

    private static byte[] deriveEvolutionEncryptionKey(KeyPair kemKeyPair) throws Exception {
        byte[] salt = "DreamToF-EvPrivKey-Encryption".getBytes(StandardCharsets.UTF_8);
        return NativeCryptoUtils.hkdfSha256(
                kemKeyPair.getPrivate().getEncoded(),
                salt, "EV-KEY-ENCRYPTION".getBytes(StandardCharsets.UTF_8), 32);
    }

    // ========================================================================
    // --- TryChain Result ---
    // ========================================================================

    public static class TryChainResult {
        public final String keyId;
        public final int requestCount;
        public final String requestKeyBase64;
        public final int fpPos;
        public final int chalPos;
        public final int evPos;

        public TryChainResult(String keyId, int requestCount, String requestKeyBase64, int fpPos, int chalPos, int evPos) {
            this.keyId = keyId;
            this.requestCount = requestCount;
            this.requestKeyBase64 = requestKeyBase64;
            this.fpPos = fpPos;
            this.chalPos = chalPos;
            this.evPos = evPos;
        }
    }

    // ========================================================================
    // --- KeyState ---
    // ========================================================================

    public static class KeyState {
        public volatile String mskBase64;
        public volatile String currentEvolveKeyBase64;
        public volatile long consumedCounter = -1;
        public volatile long lastAccessTime;

        public volatile String preEvolveMskBase64;
        public volatile String preEvolveEvolveKeyBase64;
        public volatile long preEvolveConsumedCounter = -1;

        public volatile String pendingMskBase64;
        public volatile String pendingEvolveKeyBase64;
        public volatile long pendingConsumedCounter = -1;
        public volatile int pendingConfirmCount = 0;
        public volatile int pendingConfirmThreshold = 5;
        public volatile String pendingSsHash;
        public volatile long pendingCreatedAt = -1;

        public volatile long signalSentAtCounter = -1;
        public volatile long evolutionDeadline = -1;
        public volatile long lastEvolveAt = 0;

        public volatile int fingerprintPos = 0;
        public volatile int dateRefPos = 0;
        public volatile String evolutionKeyPairPublic;

        public volatile String pendingChallengeNonce;
        public volatile Long pendingChallengeCount;

        public KeyState(String mskBase64) {
            this.mskBase64 = mskBase64;
            this.currentEvolveKeyBase64 = mskBase64;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
