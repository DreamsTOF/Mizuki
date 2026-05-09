package cn.dreamtof.common.web.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SaltFactory {

    private static final String DECREMENT = "DECREMENT";
    private static final int IDENTITY_LOOKBACK = 120;
    private static final int COMM_LOOKBACK = 120;

    private static final Map<Integer, String> IDENTITY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, String> COMM_CACHE = new ConcurrentHashMap<>();

    private static String identityMasterSalt;
    private static String commMasterSalt;

    private static volatile int identityVersion;
    private static volatile int commVersion;
    private static volatile int minSupportedVersion;

    private static volatile String cachedIdentityDailySubSalt;
    private static volatile String cachedIdentityDailySubSaltDate;
    private static volatile String cachedCommDailySubSalt;
    private static volatile String cachedCommDailySubSaltDate;

    @Value("${security.crypto.root-salt}")
    public void setIdentityMasterSalt(String salt) {
        identityMasterSalt = salt;
    }

    @Value("${security.crypto.comm-salt}")
    public void setCommMasterSalt(String salt) {
        commMasterSalt = salt;
    }

    @Autowired
    private SaltChainConfig saltChainConfig;

    @PostConstruct
    public void init() {
        computeVersionsFromGoLiveDate();
        precomputeChains();
        log.info("SaltFactory 初始化完成: identityVersion={}, commVersion={}, minSupportedVersion={}",
                identityVersion, commVersion, minSupportedVersion);
    }

    public void reload() {
        computeVersionsFromGoLiveDate();
        IDENTITY_CACHE.clear();
        COMM_CACHE.clear();
        cachedIdentityDailySubSalt = null;
        cachedCommDailySubSalt = null;
        precomputeChains();
        log.info("SaltFactory 热重载完成: identityVersion={}, commVersion={}, minSupportedVersion={}",
                identityVersion, commVersion, minSupportedVersion);
    }

    private void computeVersionsFromGoLiveDate() {
        minSupportedVersion = saltChainConfig.getMinSupportedVersion();
        LocalDate goLive = LocalDate.parse(saltChainConfig.getGoLiveDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate today = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(goLive, today);
        long monthsDiff = ChronoUnit.MONTHS.between(goLive, today);
        commVersion = Math.max(1, (int) daysDiff + 1);
        identityVersion = Math.max(1, (int) monthsDiff + 1);
    }

    private void precomputeChains() {
        deriveLegacySalts(IDENTITY_CACHE, identityMasterSalt, identityVersion, IDENTITY_LOOKBACK);
        deriveLegacySalts(COMM_CACHE, commMasterSalt, commVersion, COMM_LOOKBACK);
        log.info("预计算盐链完成: identityCacheSize={}, commCacheSize={}",
                IDENTITY_CACHE.size(), COMM_CACHE.size());
    }

    private void deriveLegacySalts(Map<Integer, String> cache, String currentSalt, int currentVer, int lookback) {
        cache.put(currentVer, currentSalt);
        String salt = currentSalt;
        for (int v = currentVer - 1; v >= currentVer - lookback && v >= minSupportedVersion; v--) {
            try {
                salt = computeHmacHex(salt, DECREMENT);
                cache.put(v, salt);
            } catch (Exception e) {
                log.error("派生盐链版本 {} 失败", v, e);
                break;
            }
        }
    }

    public static int getIdentityVersion() {
        return identityVersion;
    }

    public static int getCommVersion() {
        return commVersion;
    }

    public static int getMinSupportedVersion() {
        return minSupportedVersion;
    }

    public static boolean isVersionSupported(int version) {
        return version >= minSupportedVersion;
    }

    public static String getIdentitySalt(int version) {
        return IDENTITY_CACHE.get(version);
    }

    public static String getCommSalt(int version) {
        return COMM_CACHE.get(version);
    }

    public static String getCurrentIdentityDailySubSalt() throws Exception {
        String today = getTodayEpoch();
        if (cachedIdentityDailySubSalt != null && today.equals(cachedIdentityDailySubSaltDate)) {
            return cachedIdentityDailySubSalt;
        }
        String salt = IDENTITY_CACHE.get(identityVersion);
        if (salt == null) throw new IllegalStateException("当前身份盐未缓存");
        String subSalt = computeHmacHex(salt, today);
        cachedIdentityDailySubSalt = subSalt;
        cachedIdentityDailySubSaltDate = today;
        return subSalt;
    }

    public static String getCurrentCommDailySubSalt() throws Exception {
        String today = getTodayEpoch();
        if (cachedCommDailySubSalt != null && today.equals(cachedCommDailySubSaltDate)) {
            return cachedCommDailySubSalt;
        }
        String salt = COMM_CACHE.get(commVersion);
        if (salt == null) throw new IllegalStateException("当前通信盐未缓存");
        String subSalt = computeHmacHex(salt, today);
        cachedCommDailySubSalt = subSalt;
        cachedCommDailySubSaltDate = today;
        return subSalt;
    }

    public static String getIdentityDailySubSalt(int version, String date) throws Exception {
        String salt = IDENTITY_CACHE.get(version);
        if (salt == null) throw new IllegalArgumentException("身份盐版本 " + version + " 不在缓存中");
        return computeHmacHex(salt, date);
    }

    public static String getCommDailySubSalt(int version, String date) throws Exception {
        String salt = COMM_CACHE.get(version);
        if (salt == null) throw new IllegalArgumentException("通信盐版本 " + version + " 不在缓存中");
        return computeHmacHex(salt, date);
    }

    public static String buildCompositeSalt(int version, String date) throws Exception {
        return getCommDailySubSalt(version, date);
    }

    public static String buildCurrentCompositeSalt() throws Exception {
        return getCurrentCommDailySubSalt();
    }

    public static String getTodayEpoch() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private static String computeHmacHex(String salt, String data) throws Exception {
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] hash = NativeCryptoUtils.hmacSha256(saltBytes, dataBytes);
        return NativeCryptoUtils.bytesToHex(hash);
    }
}
