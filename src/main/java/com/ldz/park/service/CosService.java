package com.ldz.park.service;

import com.ldz.park.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
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
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                extension = originalFilename.substring(dotIndex);
            }
        }
        String prefix = config.getPrefix() == null ? "" : config.getPrefix();
        String key = prefix + UUID.randomUUID() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }

        PutObjectRequest request = new PutObjectRequest(
                config.getBucketName(),
                key,
                file.getInputStream(),
                metadata
        );

        config.cosClient().putObject(request);
        return "https://" + config.getBucketName() + ".cos." + config.getRegion() + ".myqcloud.com/" + key;
    }

    public void deleteFile(String key) {
        config.cosClient().deleteObject(config.getBucketName(), key);
    }
}