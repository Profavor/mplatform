package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.service.BatchImportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    @Mock
    private BatchImportService batchImportService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private BatchController batchController;

    private BatchJob createMockBatchJob(UUID domainId) {
        BatchJob job = new BatchJob();
        job.setId(UUID.randomUUID());
        job.setDomainId(domainId);
        job.setJobType("IMPORT");
        job.setStatus("QUEUED");
        job.setTotalRecords(100);
        return job;
    }

    @Test
    @DisplayName("대용량 배치 임포트 작업 생성 성공")
    void testCreateBatch_Success() {
        UUID domainId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        List<Map<String, Object>> records = List.of(Map.of("field1", "val1"));

        when(authContext.getUserId()).thenReturn("user01");
        BatchJob job = createMockBatchJob(domainId);
        when(batchImportService.createBatch(eq(domainId), eq(nodeId), eq(records), eq("ERP"), eq("user01")))
                .thenReturn(job);

        ResponseEntity<BatchJob> response = batchController.createBatch(domainId, nodeId, "ERP", records);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(domainId, response.getBody().getDomainId());
        verify(batchImportService).createBatch(eq(domainId), eq(nodeId), eq(records), eq("ERP"), eq("user01"));
    }

    @Test
    @DisplayName("배치 임포트 데이터 유효성 검증 성공")
    void testValidateBatch_Success() {
        UUID batchId = UUID.randomUUID();

        ResponseEntity<Void> response = batchController.validateBatch(batchId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(batchImportService).validateBatch(batchId);
    }

    @Test
    @DisplayName("배치 임포트 최종 커밋 반영 성공")
    void testCommitBatch_Success() {
        UUID batchId = UUID.randomUUID();
        BatchJob job = createMockBatchJob(UUID.randomUUID());
        job.setId(batchId);
        job.setStatus("COMPLETED");

        when(batchImportService.commitBatch(batchId)).thenReturn(job);

        ResponseEntity<BatchJob> response = batchController.commitBatch(batchId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPLETED", response.getBody().getStatus());
        verify(batchImportService).commitBatch(batchId);
    }
}
