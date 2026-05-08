package cn.dreamtof.common.util;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dreamtof.common.web.utils.SaltFactory;
import cn.dreamtof.core.exception.Asserts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class SaTokenMixUtil {

    private static final String RT_SERVICE = "refresh_token";

    private static final long RT_TIMEOUT = 365 * 10 * 24 * 60 * 60;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginToken {
        private String accessToken;
        private String refreshToken;
    }

    public static UUID findUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        return loginId != null ? UUID.fromString(loginId.toString()) : null;
    }

    public static LoginToken login(TokenPayload payload, String device) {
        Asserts.notNull(payload, "登录载荷不能为空");
        Asserts.notBlank(device, "设备标识不能为空");
        payload.setDeviceId(device);

        StpUtil.login(payload.getUserId(), new SaLoginModel()
                .setDevice(device)
                .setExtraData(payload.toMap()));
        String accessToken = StpUtil.getTokenValue();

        String hmacHex;
        String date;
        int version;
        try {
            version = SaltFactory.getIdentityVersion();
            date = SaltFactory.getTodayEpoch();
            String dailySalt = SaltFactory.getIdentityDailySubSalt(version, date);
            byte[] hmacBytes = computeHmac(dailySalt, device, payload.getUserId());
            hmacHex = bytesToHex(hmacBytes);
        } catch (Exception e) {
            Asserts.isTrue(false, "签发 RefreshToken 失败");
            hmacHex = "";
            date = "";
            version = 0;
        }

        String storedValue = "v" + version + ":" + date + ":" + payload.getUserId() + ":" + device + ":" + hmacHex;
        String refreshToken = SaTempUtil.createToken(RT_SERVICE, storedValue, RT_TIMEOUT);

        return new LoginToken(accessToken, refreshToken);
    }

    public static String refresh(String refreshToken, TokenPayload newPayload) {
        Asserts.notBlank(refreshToken, "Refresh Token 不能为空");
        Asserts.notNull(newPayload, "刷新载荷不能为空");

        Object val = SaTempUtil.parseToken(RT_SERVICE, refreshToken);
        Asserts.notNull(val, "登录已过期或无效，请重新登录");

        SaTempUtil.deleteToken(RT_SERVICE, refreshToken);

        String storedValue = val.toString();
        int tokenVersion = -1;
        String dateFromRt;
        String userIdFromRt;
        String deviceFromRt;
        String hmacHex;

        if (storedValue.startsWith("v")) {
            String[] parts = storedValue.split(":", 5);
            Asserts.isTrue(parts.length >= 5, "无效的令牌格式");
            tokenVersion = Integer.parseInt(parts[0].substring(1));
            dateFromRt = parts[1];
            userIdFromRt = parts[2];
            deviceFromRt = parts[3];
            hmacHex = parts[4];
        } else {
            String[] parts = storedValue.split(":", 4);
            Asserts.isTrue(parts.length >= 4, "无效的令牌格式");
            tokenVersion = 0;
            dateFromRt = parts[0];
            userIdFromRt = parts[1];
            deviceFromRt = parts[2];
            hmacHex = parts[3];
        }

        int minSupported = SaltFactory.getMinSupportedVersion();
        if (tokenVersion < minSupported) {
            log.warn("物理断点开关触发: tokenVersion={} < minSupportedVersion={}", tokenVersion, minSupported);
            Asserts.isTrue(false, "令牌版本过低，请重新登录");
        }

        Asserts.equals(userIdFromRt, newPayload.getUserId(), "身份校验失败，令牌不匹配");

        try {
            String dailySalt;
            if (tokenVersion > 0) {
                dailySalt = SaltFactory.getIdentityDailySubSalt(tokenVersion, dateFromRt);
            } else {
                dailySalt = SaltFactory.getCurrentIdentityDailySubSalt();
            }
            byte[] expectedHmac = computeHmac(dailySalt, deviceFromRt, userIdFromRt);
            String expectedHex = bytesToHex(expectedHmac);
            Asserts.equals(expectedHex, hmacHex, "令牌完整性校验失败，请重新登录");
        } catch (Exception e) {
            Asserts.isTrue(false, "令牌校验异常，请重新登录");
        }

        int currentVersion = SaltFactory.getIdentityVersion();
        boolean isLowSecurity = tokenVersion < currentVersion;

        if (isLowSecurity) {
            log.info("SECURITY_LEVEL_LOW: tokenVersion={} < currentVersion={}，执行自动洗白", tokenVersion, currentVersion);
        }

        StpUtil.login(userIdFromRt, new SaLoginModel()
                .setDevice(deviceFromRt)
                .setExtraData(newPayload.toMap()));
        String newAccessToken = StpUtil.getTokenValue();

        String newRefreshToken;
        try {
            String today = SaltFactory.getTodayEpoch();
            String dailySalt = SaltFactory.getIdentityDailySubSalt(currentVersion, today);
            byte[] newHmac = computeHmac(dailySalt, deviceFromRt, userIdFromRt);
            String newHmacHex = bytesToHex(newHmac);
            String newValue = "v" + currentVersion + ":" + today + ":" + userIdFromRt + ":" + deviceFromRt + ":" + newHmacHex;
            newRefreshToken = SaTempUtil.createToken(RT_SERVICE, newValue, RT_TIMEOUT);
        } catch (Exception e) {
            Asserts.isTrue(false, "签发新 RefreshToken 失败");
            newRefreshToken = "";
        }

        return newAccessToken + "|" + newRefreshToken + "|" + (isLowSecurity ? "LOW" : "NORMAL");
    }

    public static boolean verifyRefreshTokenIntegrity(String refreshToken, String userId, String device) {
        try {
            Object val = SaTempUtil.parseToken(RT_SERVICE, refreshToken);
            if (val == null) return false;
            String storedValue = val.toString();
            String dateFromRt;
            String userIdFromRt;
            String deviceFromRt;
            String hmacHex;
            int tokenVersion;

            if (storedValue.startsWith("v")) {
                String[] parts = storedValue.split(":", 5);
                if (parts.length < 5) return false;
                tokenVersion = Integer.parseInt(parts[0].substring(1));
                dateFromRt = parts[1];
                userIdFromRt = parts[2];
                deviceFromRt = parts[3];
                hmacHex = parts[4];
            } else {
                String[] parts = storedValue.split(":", 4);
                if (parts.length < 4) return false;
                tokenVersion = 0;
                dateFromRt = parts[0];
                userIdFromRt = parts[1];
                deviceFromRt = parts[2];
                hmacHex = parts[3];
            }

            if (!device.equals(deviceFromRt)) return false;
            if (!userId.equals(userIdFromRt)) return false;

            String dailySalt;
            if (tokenVersion > 0) {
                if (SaltFactory.getIdentitySalt(tokenVersion) == null) return false;
                dailySalt = SaltFactory.getIdentityDailySubSalt(tokenVersion, dateFromRt);
            } else {
                dailySalt = SaltFactory.getCurrentIdentityDailySubSalt();
            }
            byte[] expected = computeHmac(dailySalt, device, userId);
            return bytesToHex(expected).equals(hmacHex);
        } catch (Exception e) {
            return false;
        }
    }

    public static Object get(String key) {
        Asserts.notBlank(key, "Key 不能为空");
        return StpUtil.getExtra(key);
    }

    public static String findString(String key) {
        Object value = get(key);
        return (value == null || SaFoxUtil.isEmpty(value)) ? null : String.valueOf(value);
    }

    public static void logout() {
        StpUtil.logout();
    }

    private static byte[] computeHmac(String dailySalt, String device, String userId) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                dailySalt.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        mac.update(device.getBytes(StandardCharsets.UTF_8));
        mac.update(":".getBytes(StandardCharsets.UTF_8));
        mac.update(userId.getBytes(StandardCharsets.UTF_8));
        return mac.doFinal();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
