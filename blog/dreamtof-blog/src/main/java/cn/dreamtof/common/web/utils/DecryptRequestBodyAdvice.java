package cn.dreamtof.common.web.utils;

import cn.dreamtof.core.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ControllerAdvice
@Slf4j
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.crypto.enabled:true}")
    private boolean cryptoEnabled;

    @Value("${security.crypto.graceful-paths:}")
    private List<String> gracefulPaths;

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return cryptoEnabled;
    }

    @Override
    public @NonNull HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        String[] ssHeaders = new String[16];
        boolean hasHeaders = false;
        for (int i = 0; i < 16; i++) {
            ssHeaders[i] = inputMessage.getHeaders().getFirst("X-S-" + i);
            if (ssHeaders[i] != null) hasHeaders = true;
        }

        if (!hasHeaders) {
            return inputMessage;
        }

        String requestKeyBase64;
        String algorithm;
        String keyId;
        int requestCount;

        try {
            AesKeyManager.TryChainResult result = AesKeyManager.tryChainIdentifyAndConsume(ssHeaders);
            keyId = result.keyId;
            requestCount = result.requestCount;
            requestKeyBase64 = result.requestKeyBase64;
            algorithm = NativeCryptoUtils.getAlgorithmForRequest(requestCount);

        } catch (BusinessException e) {
            throw new IOException(e.getMessage());
        } catch (Exception e) {
            throw new IOException("16-Header 密钥识别失败", e);
        }

        byte[] bodyBytes = inputMessage.getBody().readAllBytes();
        JsonNode rootNode = objectMapper.readTree(bodyBytes);
        String encryptedData = rootNode.has("data") ? rootNode.get("data").asText() : null;

        if (encryptedData != null) {
            try {
                String decryptedJson = NativeCryptoUtils.decrypt(encryptedData, requestKeyBase64, algorithm);

                JsonNode decryptedNode = objectMapper.readTree(decryptedJson);
                if (decryptedNode.has("_ct") && !decryptedNode.get("_ct").isNull()) {
                    String ctBase64 = decryptedNode.get("_ct").asText();
                    try {
                        AesKeyManager.processEvolutionCt(keyId, ctBase64);
                    } catch (Exception evoErr) {
                        log.warn("[DecryptRequestBodyAdvice] _ct 处理失败: keyId={}", keyId, evoErr);
                    }
                    ((ObjectNode) decryptedNode).remove("_ct");
                    decryptedJson = objectMapper.writeValueAsString(decryptedNode);
                }

                final String finalDecryptedJson = decryptedJson;

                if (log.isDebugEnabled()) {
                    log.debug("[DecryptRequestBodyAdvice] count={} algo={} keyId={} 解密成功", requestCount, algorithm, keyId);
                }

                return new HttpInputMessage() {
                    @Override
                    public InputStream getBody() {
                        return new ByteArrayInputStream(finalDecryptedJson.getBytes(StandardCharsets.UTF_8));
                    }

                    @Override
                    public org.springframework.http.HttpHeaders getHeaders() {
                        return inputMessage.getHeaders();
                    }
                };
            } catch (Exception e) {
                String path = inputMessage.getHeaders().getFirst(":path");
                boolean isGraceful = path != null && gracefulPaths.stream().anyMatch(path::startsWith);
                if (isGraceful) {
                    log.warn("[DecryptRequestBodyAdvice] 解密失败但路径在白名单中，降级通过: path={}, error={}", path, e.getMessage());
                    return inputMessage;
                }
                throw new IOException("解密失败", e);
            }
        }
        return inputMessage;
    }
}
