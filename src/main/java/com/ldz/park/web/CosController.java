package com.ldz.park.web;

import com.ldz.park.entity.FileRecord;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CosService;
import com.ldz.park.service.FileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/cos")
@RequiredArgsConstructor
public class CosController {

    private final CosService cosService;
    private final FileRecordService fileRecordService;

    @PostMapping("/upload")
    public ApiResponse upload(@RequestParam MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        ApiResponse apiResponse = new ApiResponse();

        String url = cosService.uploadFile(file);
        String originalFilename = file.getOriginalFilename(); // 原始文件名（例如 "image.jpg"）
        Long size = file.getSize();             // 文件大小（单位：字节）
        String contentType  = file.getContentType();      // MIME 类型（例如 "image/png"）
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(fileId);
        fileRecord.setUrl(url);
        fileRecord.setExt(extension);
        fileRecord.setSize(size);
        fileRecord.setContentType(contentType);
        fileRecordService.insert(fileRecord);

        HashMap<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("id",fileId);
        apiResponse.setData(map);
         return apiResponse;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam String key) {
        //cosService.deleteFile(key);
        return ResponseEntity.ok("删除成功");
    }
}