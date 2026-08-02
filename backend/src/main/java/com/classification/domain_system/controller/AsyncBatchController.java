package com.classification.domain_system.controller;

import com.classification.domain_system.dto.AsyncBatchDto;
import com.classification.domain_system.service.AsyncBatchExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/batch")
public class AsyncBatchController {

    private final AsyncBatchExportService asyncBatchExportService;

    public AsyncBatchController(AsyncBatchExportService asyncBatchExportService) {
        this.asyncBatchExportService = asyncBatchExportService;
    }

    @PostMapping("/export/async")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read') or isAuthenticated()")
    public ResponseEntity<AsyncBatchDto.BatchTaskResponse> startAsyncExport(
            @RequestParam(required = false) UUID domainId,
            @RequestParam(defaultValue = "EXCEL") String format,
            @RequestBody(required = false) AsyncBatchDto.ExportAsyncRequest request) {
        AsyncBatchDto.BatchTaskResponse task = asyncBatchExportService.startAsyncExportWithData(domainId, format, request);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read') or isAuthenticated()")
    public ResponseEntity<AsyncBatchDto.BatchTaskResponse> getTaskStatus(@PathVariable String taskId) {
        AsyncBatchDto.BatchTaskResponse task = asyncBatchExportService.getTaskStatus(taskId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/download/{taskId}")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read') or isAuthenticated()")
    public ResponseEntity<byte[]> downloadExportFile(@PathVariable String taskId) {
        byte[] fileBytes = asyncBatchExportService.downloadTaskFile(taskId);
        String safeId = (taskId != null && taskId.length() >= 8) ? taskId.substring(0, 8) : (taskId != null ? taskId : "export");
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export_master_data_" + safeId + ".xlsx\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(fileBytes);
    }
}
