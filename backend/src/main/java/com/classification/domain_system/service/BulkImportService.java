package com.classification.domain_system.service;

import com.classification.domain_system.dto.BulkImportDto;
import com.classification.domain_system.entity.BulkImportJob;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.BulkImportJobRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkImportService {

    private final BulkImportJobRepository jobRepository;
    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public BulkImportDto.Progress startImportJob(BulkImportDto.Request request, String requestedBy) {
        if (request == null || request.getDomainId() == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Invalid bulk import request.");
        }

        Domain domain = domainRepository.findById(request.getDomainId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + request.getDomainId()));

        ClassificationNode node = null;
        if (request.getNodeId() != null) {
            node = nodeRepository.findById(request.getNodeId()).orElse(null);
        }
        if (node == null) {
            node = nodeRepository.findFirstByDomain_IdAndIsDeletedFalse(domain.getId());
        }
        if (node == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "No classification node found for domain: " + domain.getId());
        }

        BulkImportJob job = new BulkImportJob();
        job.setDomainId(domain.getId());
        job.setFileName(request.getFileName() != null ? request.getFileName() : "bulk_data.csv");
        job.setStatus("PROCESSING");
        job.setTotalRows(request.getRows().size());
        job.setCreatedBy(requestedBy);
        job = jobRepository.save(job);

        // Process records
        processImport(job.getId(), node, request.getRows());

        return getJobProgress(job.getId());
    }

    @Transactional
    public void processImport(UUID jobId, ClassificationNode node, List<Map<String, Object>> rows) {
        BulkImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        List<BulkImportDto.ErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            try {
                if (row == null || row.isEmpty()) {
                    throw new IllegalArgumentException("Empty row data");
                }

                Record record = new Record();
                record.setNode(node);
                record.setStatus("ACTIVE");
                record.setData(objectMapper.writeValueAsString(row));
                record.setVersion(1);
                recordRepository.save(record);

                successCount++;
            } catch (Exception e) {
                errorCount++;
                errors.add(BulkImportDto.ErrorDetail.builder()
                        .rowNumber(i + 1)
                        .recordKey(String.valueOf(row != null ? row.getOrDefault("id", "ROW_" + (i + 1)) : "ROW_" + (i + 1)))
                        .errorMessage(e.getMessage() != null ? e.getMessage() : "Validation/Insert failed")
                        .build());
            }
        }

        job.setProcessedRows(rows.size());
        job.setSuccessCount(successCount);
        job.setErrorCount(errorCount);
        job.setStatus("COMPLETED");
        job.setCompletedAt(LocalDateTime.now());

        try {
            job.setErrorDetailsJson(objectMapper.writeValueAsString(errors));
        } catch (Exception e) {
            job.setErrorDetailsJson("[]");
        }

        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public BulkImportDto.Progress getJobProgress(UUID jobId) {
        BulkImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        List<BulkImportDto.ErrorDetail> errorDetails = Collections.emptyList();
        if (job.getErrorDetailsJson() != null && !job.getErrorDetailsJson().isBlank()) {
            try {
                errorDetails = objectMapper.readValue(job.getErrorDetailsJson(), new TypeReference<List<BulkImportDto.ErrorDetail>>() {});
            } catch (Exception ignored) {}
        }

        double pct = job.getTotalRows() > 0 ? ((double) job.getProcessedRows() / job.getTotalRows()) * 100.0 : 100.0;

        return BulkImportDto.Progress.builder()
                .jobId(job.getId())
                .domainId(job.getDomainId())
                .fileName(job.getFileName())
                .status(job.getStatus())
                .totalRows(job.getTotalRows())
                .processedRows(job.getProcessedRows())
                .successCount(job.getSuccessCount())
                .errorCount(job.getErrorCount())
                .progressPercentage(Math.round(pct * 10.0) / 10.0)
                .errorDetails(errorDetails)
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
