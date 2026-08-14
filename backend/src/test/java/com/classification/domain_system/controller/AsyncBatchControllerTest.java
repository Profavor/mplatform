package com.classification.domain_system.controller;

import com.classification.domain_system.dto.AsyncBatchDto;
import com.classification.domain_system.service.AsyncBatchExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncBatchControllerTest {

    @Mock
    private AsyncBatchExportService asyncBatchExportService;

    @InjectMocks
    private AsyncBatchController asyncBatchController;

    @Test
    @DisplayName("비동기 Export 작업 시작 성공")
    void testStartAsyncExport_Success() {
        UUID domainId = UUID.randomUUID();
        AsyncBatchDto.ExportAsyncRequest request = new AsyncBatchDto.ExportAsyncRequest();
        request.setDomainId(domainId);

        AsyncBatchDto.BatchTaskResponse taskResponse = new AsyncBatchDto.BatchTaskResponse(
                "task-12345678", "PROCESSING", 0, 0L, 100L
        );

        when(asyncBatchExportService.startAsyncExportWithData(eq(domainId), eq("EXCEL"), any(AsyncBatchDto.ExportAsyncRequest.class)))
                .thenReturn(taskResponse);

        ResponseEntity<AsyncBatchDto.BatchTaskResponse> response = asyncBatchController.startAsyncExport(domainId, "EXCEL", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("task-12345678", response.getBody().getTaskId());
        assertEquals("PROCESSING", response.getBody().getStatus());
    }

    @Test
    @DisplayName("비동기 작업 상태 및 진행률 조회 성공")
    void testGetTaskStatus_Success() {
        String taskId = "task-12345678";
        AsyncBatchDto.BatchTaskResponse taskResponse = new AsyncBatchDto.BatchTaskResponse(
                taskId, "COMPLETED", 100, 100L, 100L
        );
        taskResponse.setDownloadUrl("/api/batch/download/" + taskId);

        when(asyncBatchExportService.getTaskStatus(taskId)).thenReturn(taskResponse);

        ResponseEntity<AsyncBatchDto.BatchTaskResponse> response = asyncBatchController.getTaskStatus(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().getStatus());
        assertEquals(100, response.getBody().getProgressPercent());
    }

    @Test
    @DisplayName("완료된 Export 파일 다운로드 및 헤더 검증")
    void testDownloadExportFile_Success() {
        String taskId = "task-12345678";
        byte[] dummyBytes = new byte[]{1, 2, 3, 4};

        when(asyncBatchExportService.downloadTaskFile(taskId)).thenReturn(dummyBytes);

        ResponseEntity<byte[]> response = asyncBatchController.downloadExportFile(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().length);
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("export_master_data_task-123.xlsx"));
    }
}
