package com.classification.domain_system.service;

import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.entity.StagingRecord;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.BatchJobRepository;
import com.classification.domain_system.repository.StagingRecordRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqViolation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchImportServiceTest {

    @Mock private BatchJobRepository batchJobRepository;
    @Mock private StagingRecordRepository stagingRecordRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private RecordService recordService;
    @Mock private ApprovalService approvalService;
    @Mock private DqRuleEngine dqRuleEngine;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BatchImportService batchImportService;

    @Test
    void testValidateBatch_DQ_Success() throws Exception {
        // Mock dependencies with lenient
        batchImportService = new BatchImportService(
            batchJobRepository, stagingRecordRepository, recordRepository, nodeRepository,
            recordService, approvalService, dqRuleEngine, objectMapper
        );

        UUID batchId = UUID.randomUUID();
        BatchJob job = new BatchJob();
        job.setId(batchId);
        when(batchJobRepository.findById(batchId)).thenReturn(Optional.of(job));

        StagingRecord sr = new StagingRecord();
        sr.setNodeId(UUID.randomUUID());
        sr.setRawData("{\"key\":\"value\"}");
        when(stagingRecordRepository.findByBatchId(batchId)).thenReturn(List.of(sr));

        DqEvaluationResult evalResult = new DqEvaluationResult(true, new ArrayList<>());
        when(dqRuleEngine.evaluate(eq(sr.getNodeId()), anyString())).thenReturn(evalResult);

        batchImportService.validateBatch(batchId);

        assertEquals("VALIDATED", sr.getStatus());
        assertEquals(1, job.getProcessedRecords());
        assertEquals(0, job.getErrorRecords());
        assertEquals("COMPLETED", job.getStatus());
        verify(batchJobRepository, times(2)).save(job);
        verify(stagingRecordRepository, times(1)).save(sr);
    }

    @Test
    void testValidateBatch_DQ_Error() throws Exception {
        batchImportService = new BatchImportService(
            batchJobRepository, stagingRecordRepository, recordRepository, nodeRepository,
            recordService, approvalService, dqRuleEngine, objectMapper
        );

        UUID batchId = UUID.randomUUID();
        BatchJob job = new BatchJob();
        job.setId(batchId);
        when(batchJobRepository.findById(batchId)).thenReturn(Optional.of(job));

        StagingRecord sr = new StagingRecord();
        sr.setNodeId(UUID.randomUUID());
        sr.setRawData("{\"key\":\"error\"}");
        when(stagingRecordRepository.findByBatchId(batchId)).thenReturn(List.of(sr));

        DqEvaluationResult evalResult = new DqEvaluationResult(false, List.of(
            new DqViolation("key", "NOT_NULL", "ERROR", "Value cannot be null")
        ));
        when(dqRuleEngine.evaluate(eq(sr.getNodeId()), anyString())).thenReturn(evalResult);

        batchImportService.validateBatch(batchId);

        assertEquals("ERROR", sr.getStatus());
        assertNotNull(sr.getErrorMessage());
        assertTrue(sr.getErrorMessage().contains("Value cannot be null"));
        assertEquals(1, job.getProcessedRecords());
        assertEquals(1, job.getErrorRecords());
    }

    @Test
    void testCommitBatch() {
        batchImportService = new BatchImportService(
            batchJobRepository, stagingRecordRepository, recordRepository, nodeRepository,
            recordService, approvalService, dqRuleEngine, objectMapper
        );

        UUID batchId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        BatchJob job = new BatchJob();
        job.setId(batchId);
        job.setDomainId(domainId);
        job.setCreatedBy("user1");
        
        when(batchJobRepository.findById(batchId)).thenReturn(Optional.of(job));

        StagingRecord sr = new StagingRecord();
        sr.setStatus("VALIDATED");
        sr.setNodeId(UUID.randomUUID());
        sr.setSourceSystem("SYS");
        sr.setRawData("{\"key\":\"value\"}");
        when(stagingRecordRepository.findByBatchId(batchId)).thenReturn(List.of(sr));

        ClassificationNode node = new ClassificationNode();
        when(nodeRepository.findById(sr.getNodeId())).thenReturn(Optional.of(node));
        when(recordService.processDataForSave(sr.getNodeId(), sr.getRawData())).thenReturn("{\"processed\":\"value\"}");

        BatchJob result = batchImportService.commitBatch(batchId);

        assertEquals("PENDING_APPROVAL", sr.getStatus());
        verify(recordRepository, times(1)).saveAllAndFlush(anyList());
        verify(stagingRecordRepository, times(1)).saveAll(anyList());
        verify(approvalService, times(1)).requestBatchRecordCreation(eq(domainId), eq(batchId), anyList(), eq("user1"));
        assertEquals("PENDING_APPROVAL", result.getStatus());
        assertEquals(1, result.getCommittedRecords());
    }
}
