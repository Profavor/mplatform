package com.classification.domain_system.controller;

import com.classification.domain_system.service.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unknown_file";
        }

        String savedFileName = fileStorageService.storeFile(file);

        // 원본 파일명을 URL 파라미터로 인코딩하여 포함
        String encodedOriginalName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String fileDownloadUri = "/api/files/download/" + savedFileName + "?name=" + encodedOriginalName;

        Map<String, String> response = new HashMap<>();
        response.put("fileName", originalFileName);
        response.put("url", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, @RequestParam(value = "name", required = false) String originalName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            if (resource != null && resource.exists()) {
                String downloadName = originalName != null ? originalName : resource.getFilename();
                String safeName = UriUtils.encode(downloadName, StandardCharsets.UTF_8);

                ContentDisposition contentDisposition = ContentDisposition.attachment()
                        .filename(safeName)
                        .filename(downloadName, StandardCharsets.UTF_8)
                        .build();

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
