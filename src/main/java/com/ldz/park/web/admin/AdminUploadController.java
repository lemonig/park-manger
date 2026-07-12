package com.ldz.park.web.admin;

import com.ldz.park.entity.FileRecord;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CosService;
import com.ldz.park.service.FileRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Tag(name = "后台管理 / 文件上传")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUploadController {

    private final CosService cosService;
    private final FileRecordService fileRecordService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public ApiResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ApiResponse.badRequest("请选择上传文件");
        }

        String fileId = UUID.randomUUID().toString();
        String url = cosService.uploadFile(file);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }
        Long size = file.getSize();
        String contentType = file.getContentType();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1);
        }

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(fileId);
        fileRecord.setUrl(url);
        fileRecord.setExt(extension);
        fileRecord.setSize(size);
        fileRecord.setFilename(originalFilename);
        fileRecord.setOriginalFilename(originalFilename);
        fileRecord.setContentType(contentType);
        Date now = new Date();
        fileRecord.setCreateTime(now);
        fileRecord.setUpdateTime(now);
        fileRecordService.insert(fileRecord);

        HashMap<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("id", fileId);
        return ApiResponse.success(map);
    }
}
