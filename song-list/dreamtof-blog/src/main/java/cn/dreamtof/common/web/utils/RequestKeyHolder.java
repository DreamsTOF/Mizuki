package cn.dreamtof.common.web.utils;

import java.util.concurrent.atomic.AtomicReference;

public class RequestKeyHolder {

    private static final ScopedValue<AtomicReference<RequestKeyInfo>> HOLDER = ScopedValue.newInstance();

    public static ScopedValue<AtomicReference<RequestKeyInfo>> holder() {
        return HOLDER;
    }

    public static void set(String keyId, int requestCount, String algorithm, String requestKeyBase64) {
        AtomicReference<RequestKeyInfo> ref = HOLDER.get();
        if (ref != null) {
            ref.set(new RequestKeyInfo(keyId, requestCount, algorithm, requestKeyBase64));
        }
    }

    public static RequestKeyInfo get() {
        AtomicReference<RequestKeyInfo> ref = HOLDER.get();
        return ref != null ? ref.get() : null;
    }

    public record RequestKeyInfo(String keyId, int requestCount, String algorithm, String requestKeyBase64) {
        public RequestKeyInfo {
            if (algorithm == null) algorithm = "ChaCha20";
        }
    }
}
