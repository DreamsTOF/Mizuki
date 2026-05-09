package cn.dreamtof.common.web.utils;

import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class SchnorrVerifier {

    private static final String ED25519_SEED_CTX = "ED25519-SEED";

    public static byte[] deriveEd25519Seed(String requestKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(requestKeyBase64);
        return NativeCryptoUtils.hmacSha256(keyBytes, ED25519_SEED_CTX.getBytes(StandardCharsets.UTF_8));
    }

    public static Ed25519PublicKeyParameters derivePublicKey(String requestKeyBase64) throws Exception {
        byte[] seed = deriveEd25519Seed(requestKeyBase64);
        Ed25519PrivateKeyParameters privKey = new Ed25519PrivateKeyParameters(seed, 0);
        return privKey.generatePublicKey();
    }

    public static byte[] sign(byte[] seed, byte[] message) throws Exception {
        Ed25519PrivateKeyParameters privKey = new Ed25519PrivateKeyParameters(seed, 0);
        Signer signer = new Ed25519Signer();
        signer.init(true, privKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    public static boolean verify(byte[] publicKeyBytes, byte[] signature, byte[] message) throws Exception {
        Ed25519PublicKeyParameters pubKey = new Ed25519PublicKeyParameters(publicKeyBytes, 0);
        Signer verifier = new Ed25519Signer();
        verifier.init(false, pubKey);
        verifier.update(message, 0, message.length);
        return verifier.verifySignature(signature);
    }

    public static byte[] buildMessage(long count, String keyId, String requestKeyBase64) throws Exception {
        byte[] countBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            countBytes[i] = (byte) (count & 0xFF);
            count >>= 8;
        }

        byte[] keyIdBytes = keyId.getBytes(StandardCharsets.UTF_8);

        byte[] requestKeyHash = Arrays.copyOf(
                NativeCryptoUtils.digestSha256(Base64.getDecoder().decode(requestKeyBase64)), 8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(countBytes);
        baos.write(keyIdBytes);
        baos.write(requestKeyHash);
        return baos.toByteArray();
    }

    public static byte[] uuidToBytes(String uuid) {
        String hex = uuid.replace("-", "");
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static String bytesToUuidFragment(byte[] bytes16) {
        StringBuilder sb = new StringBuilder(36);
        for (int i = 0; i < 16; i++) {
            sb.append(String.format("%02x", bytes16[i] & 0xFF));
            if (i == 3 || i == 5 || i == 7 || i == 9) sb.append('-');
        }
        return sb.toString();
    }

    public static byte[][] splitProof(byte[] publicKey, byte[] signature) {
        byte[][] fragments = new byte[6][16];
        System.arraycopy(publicKey, 0, fragments[0], 0, 16);
        System.arraycopy(publicKey, 16, fragments[1], 0, 16);
        for (int i = 0; i < 4; i++) {
            System.arraycopy(signature, i * 16, fragments[2 + i], 0, 16);
        }
        return fragments;
    }

    public static ProofReassembly reassembleProof(byte[][] fragments) {
        byte[] publicKey = new byte[32];
        System.arraycopy(fragments[0], 0, publicKey, 0, 16);
        System.arraycopy(fragments[1], 0, publicKey, 16, 16);

        byte[] signature = new byte[64];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(fragments[2 + i], 0, signature, i * 16, 16);
        }

        return new ProofReassembly(publicKey, signature);
    }

    public static class ProofReassembly {
        public final byte[] publicKey;
        public final byte[] signature;

        public ProofReassembly(byte[] publicKey, byte[] signature) {
            this.publicKey = publicKey;
            this.signature = signature;
        }
    }
}
