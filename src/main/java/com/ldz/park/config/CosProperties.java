package com.ldz.park.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
@Data
public class CosProperties {
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String baseUrl;
}
