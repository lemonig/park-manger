package com.ldz.park.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class TencentCOSUtil {
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;


}
