package cn.dreamtof.common.web.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String ROOT_SECRET = "DreamToF_Default_Key_2026"; // 根密钥
    private static final long TIME_STEP = 5 * 60 * 1000; // 5分钟步长
    private static final int TAG_LENGTH = 128;
    private static final int IV_LEN = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 根据时间戳生成 MD5 动态密钥 (16字节/128位)
     */
    public static SecretKeySpec getDynamicKey(long timestamp) throws Exception {
        long timeBucket = timestamp / TIME_STEP;
        String factor = ROOT_SECRET + timeBucket;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] keyBytes = md.digest(factor.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String data, long ts) throws Exception {
        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getDynamicKey(ts), new GCMParameterSpec(TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedData, long ts) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(decoded, 0, iv, 0, IV_LEN);
        byte[] cipherText = new byte[decoded.length - IV_LEN];
        System.arraycopy(decoded, IV_LEN, cipherText, 0, cipherText.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getDynamicKey(ts), new GCMParameterSpec(TAG_LENGTH, iv));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }
}