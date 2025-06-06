package com.ldz.park.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
@Data
public class CosConfig {
    private String secretId = "AKID86sQksnUxEH7j7IFyTlpfHfpmXFjz2rf";
    private String secretKey = "hBRTItGPRGuNdiZCbnlV4QZUBf0GbIp9";
    private String region;
    private String bucketName;
    private String prefix;

    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }
}