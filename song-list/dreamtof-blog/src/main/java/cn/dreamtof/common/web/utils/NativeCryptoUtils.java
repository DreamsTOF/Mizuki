package cn.dreamtof.common.web.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.KEM;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;

public class NativeCryptoUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String CHACHA20_ALGO = "ChaCha20-Poly1305";
    private static final String AES_GCM_ALGO = "AES/GCM/NoPadding";
    private static final int NONCE_LEN = 12;
    private static final int GCM_TAG_LEN = 128;
    private static final String KEM_ALGO = "ML-KEM-768";

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String CACHED_PHYSICAL_SALT;

    static {
        CACHED_PHYSICAL_SALT = computePhysicalSalt();
    }

    private static String computePhysicalSalt() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((byte) (Runtime.getRuntime().availableProcessors() & 0xFF));
            md.update(System.getProperty("os.arch", "").getBytes(StandardCharsets.UTF_8));
            md.update(System.getProperty("os.version", "").getBytes(StandardCharsets.UTF_8));
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                while (nets.hasMoreElements()) {
                    NetworkInterface ni = nets.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        md.update(mac);
                    }
                }
            } catch (Exception ignored) {}
            byte[] hash = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "0000000000000000000000000000000000000000000000000000000000000000";
        }
    }

    public static String getPhysicalSalt() {
        return CACHED_PHYSICAL_SALT;
    }

    public static String buildCompositeSalt(String logicalSalt, String envId) {
        String logical512 = digestSha512Hex(logicalSalt.getBytes(StandardCharsets.UTF_8));
        return logical512 + envId;
    }

    public static KeyPair generateMlKemKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEM_ALGO);
        return generator.generateKeyPair();
    }

    public static byte[] decapsulateMlKem(byte[] ciphertext, PrivateKey privateKey) throws Exception {
        KEM kem = KEM.getInstance(KEM_ALGO);
        KEM.Decapsulator decapsulator = kem.newDecapsulator(privateKey);
        SecretKey sharedSecret = decapsulator.decapsulate(ciphertext);
        return sharedSecret.getEncoded();
    }

    public static String encrypt(String plainText, String base64Key, String algorithm) throws Exception {
        if ("AES-GCM".equals(algorithm)) {
            return encryptAesGcm(plainText, base64Key);
        }
        return encryptChaCha20(plainText, base64Key);
    }

    public static String decrypt(String encryptedData, String base64Key, String algorithm) throws Exception {
        if ("AES-GCM".equals(algorithm)) {
            return decryptAesGcm(encryptedData, base64Key);
        }
        return decryptChaCha20(encryptedData, base64Key);
    }

    public static String encryptChaCha20(String plainText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] nonce = new byte[NONCE_LEN];
        RANDOM.nextBytes(nonce);
        Cipher cipher = Cipher.getInstance(CHACHA20_ALGO, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "ChaCha20"), new IvParameterSpec(nonce));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[nonce.length + encrypted.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(encrypted, 0, combined, nonce.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptChaCha20(String encryptedData, String base64Key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] nonce = new byte[NONCE_LEN];
        System.arraycopy(combined, 0, nonce, 0, nonce.length);
        byte[] cipherTextWithTag = new byte[combined.length - NONCE_LEN];
        System.arraycopy(combined, nonce.length, cipherTextWithTag, 0, cipherTextWithTag.length);
        Cipher cipher = Cipher.getInstance(CHACHA20_ALGO, "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "ChaCha20"), new IvParameterSpec(nonce));
        return new String(cipher.doFinal(cipherTextWithTag), StandardCharsets.UTF_8);
    }

    public static String encryptAesGcm(String plainText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] iv = new byte[NONCE_LEN];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGO, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LEN, iv));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptAesGcm(String encryptedData, String base64Key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] iv = new byte[NONCE_LEN];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        byte[] cipherTextWithTag = new byte[combined.length - NONCE_LEN];
        System.arraycopy(combined, iv.length, cipherTextWithTag, 0, cipherTextWithTag.length);
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGO, "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LEN, iv));
        return new String(cipher.doFinal(cipherTextWithTag), StandardCharsets.UTF_8);
    }

    public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int length) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        if (salt == null || salt.length == 0) {
            salt = new byte[32];
        }
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(ikm);
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        mac.update(info);
        mac.update((byte) 0x01);
        byte[] result = mac.doFinal();
        if (result.length > length) {
            byte[] truncated = new byte[length];
            System.arraycopy(result, 0, truncated, 0, length);
            return truncated;
        }
        return result;
    }

    public static byte[] timeLockedHkdf(byte[] ikm, byte[] salt, byte[] info, int length, int iterations) throws Exception {
        byte[] result = hkdfSha256(ikm, salt, info, length);
        for (int i = 0; i < iterations; i++) {
            byte[] iterData = ("DreamToF-TimeLock-" + i).getBytes(StandardCharsets.UTF_8);
            result = hmacSha256(result, iterData);
        }
        return result;
    }

    public static String deriveKeyId(byte[] sharedSecret) {
        byte[] hash = digestSha256(sharedSecret);
        return Base64.getEncoder().encodeToString(hash);
    }

    public static byte[] digestSha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String digestSha256Hex(byte[] data) {
        byte[] hash = digestSha256(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static byte[] digestSha512(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String digestSha512Hex(byte[] data) {
        byte[] hash = digestSha512(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /** HMAC-SHA256(base64解码后的key, context) → base64编码结果 */
    public static String hmacToBase64(String keyBase64, String context) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyBase64);
        byte[] result = hmacSha256(key, context.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(result);
    }

    public static String getAlgorithmForRequest(long requestCount) {
        return requestCount % 2 == 0 ? "ChaCha20" : "AES-GCM";
    }

    public static int hashMod(String rootSalt, String key, int modulus) throws Exception {
        byte[] hash = hmacSha256(rootSalt.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        int value = (hash[0] & 0xFF) | ((hash[1] & 0xFF) << 8);
        return Math.abs(value) % modulus;
    }

    public static long extractUuidV7Timestamp(String drUuid) {
        String clean = drUuid.replace("-", "");
        return Long.parseLong(clean.substring(0, 12), 16);
    }

    public static byte[] aesGcmEncrypt(byte[] plaintext, byte[] key) throws Exception {
        byte[] iv = new byte[NONCE_LEN];
        RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGO, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LEN, iv));
        byte[] encrypted = cipher.doFinal(plaintext);
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return combined;
    }

    public static byte[] aesGcmDecrypt(byte[] combined, byte[] key) throws Exception {
        byte[] iv = new byte[NONCE_LEN];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        byte[] cipherTextWithTag = new byte[combined.length - NONCE_LEN];
        System.arraycopy(combined, iv.length, cipherTextWithTag, 0, cipherTextWithTag.length);
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGO, "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LEN, iv));
        return cipher.doFinal(cipherTextWithTag);
    }

    public static KeyPair reconstructMlKemKeyPair(String publicKeyBase64, byte[] privateKeyBytes) throws Exception {
        byte[] pubBytes = Base64.getDecoder().decode(publicKeyBase64);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);
        KeyFactory kf = KeyFactory.getInstance(KEM_ALGO, "BC");
        PrivateKey priv = kf.generatePrivate(privSpec);
        PublicKey pub = kf.generatePublic(pubSpec);
        return new KeyPair(pub, priv);
    }

}
