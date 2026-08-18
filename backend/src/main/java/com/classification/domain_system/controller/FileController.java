package com.classification.domain_system.controller;

import com.classification.domain_system.service.storage.FileStorageService;
import com.classification.domain_system.service.storage.FileValidationUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpRange;
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

        // 원본 파일명 및 용량을 URL 파라미터로 인코딩하여 포함
        String encodedOriginalName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        long fileSize = file.getSize();
        String fileDownloadUri = "/api/files/download/" + savedFileName + "?name=" + encodedOriginalName + "&size=" + fileSize;

        Map<String, String> response = new HashMap<>();
        response.put("fileName", originalFileName);
        response.put("url", fileDownloadUri);
        response.put("size", String.valueOf(fileSize));
        response.put("fileSize", String.valueOf(fileSize));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info/{fileName:.+}")
    public ResponseEntity<?> getFileInfo(@PathVariable String fileName) {
        try {
            String cleanFileName = UriUtils.decode(fileName, StandardCharsets.UTF_8);
            cleanFileName = cleanFileName.replaceAll("^[\\[\"\\s']+|[\\]\"\\s']+$", "");
            Resource resource = fileStorageService.loadFileAsResource(cleanFileName);
            if (resource != null && resource.exists()) {
                Map<String, Object> info = new HashMap<>();
                info.put("fileName", cleanFileName);
                info.put("size", resource.contentLength());
                return ResponseEntity.ok(info);
            }
            return ResponseEntity.notFound().build();
        } catch (Throwable ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName,
                                          @RequestParam(value = "name", required = false) String originalName,
                                          @RequestHeader(required = false) HttpHeaders headers) {
        try {
            String cleanFileName = UriUtils.decode(fileName, StandardCharsets.UTF_8);
            cleanFileName = cleanFileName.replaceAll("^[\\[\"\\s']+|[\\]\"\\s']+$", "");

            Resource resource = fileStorageService.loadFileAsResource(cleanFileName);
            if (resource != null && resource.exists()) {
                String downloadName = originalName != null ? UriUtils.decode(originalName, StandardCharsets.UTF_8) : resource.getFilename();
                if (downloadName == null) downloadName = "file";
                downloadName = downloadName.replaceAll("^[\\[\"\\s']+|[\\]\"\\s']+$", "");

                boolean isDangerous = FileValidationUtil.isDangerousExtension(downloadName);
                
                MediaType mediaType = isDangerous 
                        ? MediaType.APPLICATION_OCTET_STREAM 
                        : MediaTypeFactory.getMediaType(downloadName).orElse(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM));

                String safeAsciiName = downloadName.replaceAll("[^a-zA-Z0-9._-]", "_");
                if (safeAsciiName.isBlank()) safeAsciiName = "file";

                ContentDisposition contentDisposition = (isDangerous ? ContentDisposition.attachment() : ContentDisposition.inline())
                        .filename(safeAsciiName)
                        .filename(downloadName, StandardCharsets.UTF_8)
                        .build();

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Throwable ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
