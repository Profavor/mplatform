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
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchImportService {

    private final BatchJobRepository batchJobRepository;
    private final StagingRecordRepository stagingRecordRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordService recordService;
    private final ApprovalService approvalService;
    private final DqRuleEngine dqRuleEngine;
    private final ObjectMapper objectMapper;

    @Transactional
    public BatchJob createBatch(UUID domainId, UUID nodeId, List<Map<String, Object>> records, String sourceSystem, String createdBy) {
        BatchJob job = new BatchJob();
        job.setDomainId(domainId);
        job.setJobType("IMPORT");
        job.setStatus("QUEUED");
        job.setTotalRecords(records.size());
        job.setStartedAt(LocalDateTime.now());
        job.setCreatedBy(createdBy);
        batchJobRepository.save(job);

        for (Map<String, Object> recordData : records) {
            StagingRecord sr = new StagingRecord();
            sr.setBatchId(job.getId());
            sr.setDomainId(domainId);
            sr.setNodeId(nodeId);
            try {
                sr.setRawData(objectMapper.writeValueAsString(recordData));
            } catch (Exception e) {
                sr.setRawData("{}");
            }
            sr.setStatus("PENDING");
            sr.setSourceSystem(sourceSystem);
            stagingRecordRepository.save(sr);
        }

        return job;
    }

    @Transactional
    public void validateBatch(UUID batchId) {
        BatchJob job = batchJobRepository.findById(batchId).orElseThrow(() -> new ResourceNotFoundException("Batch job not found"));
        job.setStatus("RUNNING");
        batchJobRepository.save(job);

        List<StagingRecord> records = stagingRecordRepository.findByBatchId(batchId);
        int errorCount = 0;
        int processedCount = 0;

        for (StagingRecord sr : records) {
            DqEvaluationResult evalResult = dqRuleEngine.evaluate(sr.getNodeId(), sr.getRawData());
            if (!evalResult.isValid()) {
                sr.setStatus("ERROR");
                try {
                    sr.setErrorMessage(objectMapper.writeValueAsString(
                        evalResult.getViolations().stream()
                            .filter(v -> "ERROR".equals(v.getSeverity()))
                            .map(v -> Map.of(
                                "fieldKey", v.getFieldKey(),
                                "ruleType", v.getRuleType(),
                                "message", v.getMessage()))
                            .collect(Collectors.toList())
                    ));
                } catch (Exception e) {
                    sr.setErrorMessage("[{\"message\":\"Failed to serialize error message\"}]");
                }
                errorCount++;
            } else {
                sr.setStatus("VALIDATED");
            }
            stagingRecordRepository.save(sr);
            processedCount++;
        }

        job.setProcessedRecords(processedCount);
        job.setErrorRecords(errorCount);
        job.setStatus("COMPLETED");
        job.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(job);
    }

    @Transactional
    public BatchJob commitBatch(UUID batchId) {
        BatchJob job = batchJobRepository.findById(batchId).orElseThrow(() -> new ResourceNotFoundException("Batch job not found"));
        List<StagingRecord> records = stagingRecordRepository.findByBatchId(batchId);
        
        List<Record> recordBatch = new ArrayList<>();
        List<StagingRecord> committableStagings = new ArrayList<>();
        List<UUID> allRecordIds = new ArrayList<>();
        int chunkSize = 500;
        
        for (StagingRecord sr : records) {
            if (!"VALIDATED".equals(sr.getStatus())) continue;
            
            ClassificationNode node = nodeRepository.findById(sr.getNodeId()).orElseThrow(() -> new ResourceNotFoundException("Node not found"));
            String processedData = recordService.processDataForSave(sr.getNodeId(), sr.getRawData());
            
            Record record = new Record();
            record.setNode(node);
            record.setData(processedData);
            record.setStatus("DRAFT");
            record.setSourceSystem(sr.getSourceSystem());
            recordBatch.add(record);
            committableStagings.add(sr);
            
            // 500건 단위 청크 삽입
            if (recordBatch.size() >= chunkSize) {
                recordRepository.saveAllAndFlush(recordBatch);
                allRecordIds.addAll(recordBatch.stream().map(Record::getId).collect(Collectors.toList()));
                recordBatch.clear();
            }
        }
        
        // 잔여분 삽입
        if (!recordBatch.isEmpty()) {
            recordRepository.saveAllAndFlush(recordBatch);
            allRecordIds.addAll(recordBatch.stream().map(Record::getId).collect(Collectors.toList()));
            recordBatch.clear();
        }
        
        // 스테이징 상태 업데이트 (PENDING_APPROVAL)
        for (StagingRecord sr : committableStagings) {
            sr.setStatus("PENDING_APPROVAL");
        }
        stagingRecordRepository.saveAll(committableStagings);
        
        // 대량 결재 요청 1건 생성
        com.classification.domain_system.entity.ApprovalRequest approvalReq = approvalService.requestBatchRecordCreation(
            job.getDomainId(), batchId, allRecordIds, job.getCreatedBy());
        
        // BatchJob 업데이트
        job.setProcessedRecords(committableStagings.size());
        job.setStatus("PENDING_APPROVAL");
        job.setCompletedAt(LocalDateTime.now());
        job.setCommittedRecords(committableStagings.size());
        if (approvalReq != null) {
            job.setApprovalRequestId(approvalReq.getId());
        }
        batchJobRepository.save(job);
        
        return job;
    }
}
