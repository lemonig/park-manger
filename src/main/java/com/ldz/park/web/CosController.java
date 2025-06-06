package com.ldz.park.web;

import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cos")
@RequiredArgsConstructor
public class CosController {

    private final CosService cosService;

    @PostMapping("/upload")
    public ApiResponse upload(@RequestBody MultipartFile file) throws IOException {
        String url = cosService.uploadFile(file);
         return new ApiResponse(url);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam String key) {
        //cosService.deleteFile(key);
        return ResponseEntity.ok("删除成功");
    }
}