package com.classification.domain_system.controller;

import com.classification.domain_system.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/export/domains/{domainId}")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    @GetMapping("/template")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable UUID domainId,
            @RequestParam(required = false) UUID nodeId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        byte[] csvData = excelExportService.generateTemplate(domainId, nodeId, lang);
        String filename = URLEncoder.encode("template_" + domainId.toString().substring(0, 8) + ".csv", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/records")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<byte[]> exportRecords(
            @PathVariable UUID domainId,
            @RequestParam(required = false) UUID nodeId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        byte[] csvData = excelExportService.exportRecordsToCsv(domainId, nodeId, lang);
        String filename = URLEncoder.encode("records_" + domainId.toString().substring(0, 8) + ".csv", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
