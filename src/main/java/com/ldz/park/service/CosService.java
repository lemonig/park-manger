package com.ldz.park.service;

import com.ldz.park.config.CosProperties;
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

    private final COSClient cosClient;
    private final CosProperties cosProperties;

    public String uploadFile(MultipartFile file) throws IOException {
        String key = "upload/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(
                cosProperties.getBucket(),
                key,
                file.getInputStream(),
                metadata
        );
        cosClient.putObject(request);

        return cosProperties.getBaseUrl() + "/" + key;
    }

    public void deleteFile(String key) {
        cosClient.deleteObject(cosProperties.getBucket(), key);
    }
}