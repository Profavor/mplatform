package com.classification.domain_system.service;

import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.entity.StagingRecord;
import com.classification.domain_system.repository.BatchJobRepository;
import com.classification.domain_system.repository.StagingRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchImportService {

    private final BatchJobRepository batchJobRepository;
    private final StagingRecordRepository stagingRecordRepository;
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
        BatchJob job = batchJobRepository.findById(batchId).orElseThrow();
        job.setStatus("RUNNING");
        batchJobRepository.save(job);

        List<StagingRecord> records = stagingRecordRepository.findByBatchId(batchId);
        int errorCount = 0;
        int processedCount = 0;

        for (StagingRecord sr : records) {
            // Mock validation
            if (sr.getRawData() != null && sr.getRawData().contains("error")) {
                sr.setStatus("ERROR");
                sr.setErrorMessage("Mock validation error");
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
    public void commitBatch(UUID batchId) {
        // Mock commit
        List<StagingRecord> records = stagingRecordRepository.findByBatchId(batchId);
        for (StagingRecord sr : records) {
            if ("VALIDATED".equals(sr.getStatus())) {
                sr.setStatus("COMMITTED");
                stagingRecordRepository.save(sr);
                // In real scenario, insert into Record table
            }
        }
    }
}
