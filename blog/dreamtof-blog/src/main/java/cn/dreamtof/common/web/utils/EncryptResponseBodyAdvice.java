package cn.dreamtof.common.web.utils;

import cn.dreamtof.core.base.BaseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@ControllerAdvice
@Slf4j
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.crypto.enabled:true}")
    private boolean cryptoEnabled;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!cryptoEnabled) {
            return false;
        }
        return BaseResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        boolean has16Header = request.getHeaders().getFirst("X-S-0") != null;

        if (!has16Header || !(body instanceof BaseResponse<?> baseRes)) {
            return body;
        }

        RequestKeyHolder.RequestKeyInfo info = RequestKeyHolder.get();
        if (info == null) {
            log.warn("RequestKeyHolder 为空，跳过响应加密");
            return body;
        }

        try {
            if (baseRes.getData() != null && !(baseRes.getData() instanceof String)) {
                Object dataToEncrypt = baseRes.getData();

                if (AesKeyManager.shouldTriggerEvolution(info.keyId())) {
                    try {
                        String evPubKeyBase64 = AesKeyManager.triggerEvolution(info.keyId(), info.requestCount());
                        if (evPubKeyBase64 != null) {
                            String pkField = AesKeyManager.getPkFieldName(info.requestKeyBase64());
                            ObjectNode wrapper = objectMapper.createObjectNode();
                            wrapper.set("d", objectMapper.valueToTree(dataToEncrypt));
                            wrapper.put(pkField, evPubKeyBase64);
                            dataToEncrypt = wrapper;

                            int evSignalIdx = Math.abs(NativeCryptoUtils.hashMod(
                                    info.requestKeyBase64(), "EV_SIGNAL_IDX", 1000));
                            response.getHeaders().set("x-s-evolve-" + evSignalIdx, "1");
                            response.getHeaders().set("x-evolve-deadline",
                                    String.valueOf(AesKeyManager.getEvolutionDeadline(info.keyId())));

                            if (log.isDebugEnabled()) {
                                log.debug("[EncryptResponse] 下发进化信号: keyId={} count={} pkField={}",
                                        info.keyId(), info.requestCount(), pkField);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("进化信号下发失败，跳过: keyId={}", info.keyId(), e);
                    }
                }

                String jsonStr = objectMapper.writeValueAsString(dataToEncrypt);
                String encryptedStr = NativeCryptoUtils.encrypt(jsonStr, info.requestKeyBase64(), info.algorithm());
                ((BaseResponse<Object>) baseRes).setData(encryptedStr);

                String respHeaderName = computeResponseHeaderName(info.requestKeyBase64());
                String respHeaderValue = computeResponseHeaderValue(info.requestKeyBase64());
                response.getHeaders().set(respHeaderName, respHeaderValue);

                if (ChallengeManager.shouldChallenge()) {
                    String challenge = ChallengeManager.issueChallenge(info.keyId(), info.requestCount());
                    int chalIdx = Math.abs(NativeCryptoUtils.hashMod(
                            info.requestKeyBase64(), "CHAL_IDX", 1000));
                    response.getHeaders().set("x-s-chal-" + chalIdx, challenge);
                    response.getHeaders().set("x-chal-count", String.valueOf(info.requestCount()));
                    response.getHeaders().set("x-chal-iter", String.valueOf(ChallengeManager.getTimeLockChallengeIterations()));
                    if (log.isDebugEnabled()) {
                        log.debug("[EncryptResponse] 下发挑战: keyId={} count={} iter={}",
                                info.keyId(), info.requestCount(), ChallengeManager.getTimeLockChallengeIterations());
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("[EncryptResponseBodyAdvice] count={} algo={} 加密响应成功", info.requestCount(), info.algorithm());
                }
            }
        } catch (Exception e) {
            log.error("响应加密失败", e);
        }
        return body;
    }

    private static String computeResponseHeaderName(String requestKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(requestKeyBase64);
        byte[] hash = NativeCryptoUtils.hmacSha256(keyBytes, "RSP-NAME".getBytes(StandardCharsets.UTF_8));
        byte[] fragment = Arrays.copyOf(hash, 16);
         return "x-r-" + SchnorrVerifier.bytesToUuidFragment(fragment);
     }
 
     private static String computeResponseHeaderValue(String requestKeyBase64) throws Exception {
         byte[] keyBytes = Base64.getDecoder().decode(requestKeyBase64);
         byte[] hash = NativeCryptoUtils.hmacSha256(keyBytes, "RSP-TAG".getBytes(StandardCharsets.UTF_8));
         byte[] fragment = Arrays.copyOf(hash, 16);
        return SchnorrVerifier.bytesToUuidFragment(fragment);
    }
}
