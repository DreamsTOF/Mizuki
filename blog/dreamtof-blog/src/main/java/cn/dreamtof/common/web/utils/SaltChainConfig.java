package cn.dreamtof.common.web.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.crypto.salt-chain")
public class SaltChainConfig {

    private int identityVersion = 1;
    private int commVersion = 1;
    private int minSupportedVersion = 0;
    private String goLiveDate = "2025-01-01";
}
