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
    public ResponseEntity<?> downloadFile(@PathVariable String fileName,
                                          @RequestParam(value = "name", required = false) String originalName,
                                          @RequestHeader HttpHeaders headers) {
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

                long contentLength = resource.contentLength();

                if (headers.getRange().isEmpty()) {
                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                            .body(resource);
                } else {
                    HttpRange range = headers.getRange().get(0);
                    long start = range.getRangeStart(contentLength);
                    long end = range.getRangeEnd(contentLength);
                    long rangeLength = Math.min(1024 * 1024 * 5, end - start + 1); // Max 5MB chunk
                    org.springframework.core.io.support.ResourceRegion region = new org.springframework.core.io.support.ResourceRegion(resource, start, rangeLength);

                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .contentType(mediaType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                            .body(region);
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
