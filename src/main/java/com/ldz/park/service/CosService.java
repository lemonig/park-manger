package com.ldz.park.service;

import com.ldz.park.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosService {
    private final CosConfig config;

    public String uploadFile(MultipartFile file) throws IOException {
        String key = config.getPrefix() + UUID.randomUUID() + "-" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(
                config.getBucketName(),
                key,
                file.getInputStream(),
                metadata
        );

        PutObjectResult putObjectResult = config.cosClient().putObject(request);
        return "https://" + config.getBucketName() + ".cos." + config.getRegion() + ".myqcloud.com/" + key;
    }

    public void deleteFile(String key) {
        config.cosClient().deleteObject(config.getBucketName(), key);
    }
}