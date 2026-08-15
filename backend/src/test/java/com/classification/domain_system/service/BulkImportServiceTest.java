package com.classification.domain_system.service;

import com.classification.domain_system.dto.BulkImportDto;
import com.classification.domain_system.entity.BulkImportJob;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.BulkImportJobRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BulkImportServiceTest {

    @Mock private BulkImportJobRepository jobRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private BulkImportService bulkImportService;

    private UUID domainId;
    private Domain domain;
    private ClassificationNode node;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인"));

        node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setName(Map.of("ko", "기본 노드"));
    }

    @Test
    @DisplayName("startImportJob: 정상 행은 삽입되고 오류 행은 상세 기록되어 완료됨")
    void testStartImportJob() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(nodeRepository.findFirstByDomain_IdAndIsDeletedFalse(domainId)).thenReturn(node);

        UUID jobId = UUID.randomUUID();
        when(jobRepository.save(any(BulkImportJob.class))).thenAnswer(i -> {
            BulkImportJob j = i.getArgument(0);
            if (j.getId() == null) j.setId(jobId);
            return j;
        });

        when(jobRepository.findById(jobId)).thenAnswer(i -> {
            BulkImportJob j = new BulkImportJob();
            j.setId(jobId);
            j.setDomainId(domainId);
            j.setStatus("COMPLETED");
            j.setTotalRows(2);
            j.setProcessedRows(2);
            j.setSuccessCount(1);
            j.setErrorCount(1);
            j.setErrorDetailsJson("[{\"rowNumber\":2,\"recordKey\":\"ROW_2\",\"errorMessage\":\"Empty row data\"}]");
            return Optional.of(j);
        });

        BulkImportDto.Request req = BulkImportDto.Request.builder()
                .domainId(domainId)
                .fileName("customers.csv")
                .rows(List.of(
                        Map.of("name", "홍길동", "phone", "010-1234-5678"),
                        Collections.emptyMap() // 의도적 빈 행 (오류 유발)
                ))
                .build();

        BulkImportDto.Progress progress = bulkImportService.startImportJob(req, "admin");

        assertThat(progress).isNotNull();
        assertThat(progress.getStatus()).isEqualTo("COMPLETED");
        assertThat(progress.getSuccessCount()).isEqualTo(1);
        assertThat(progress.getErrorCount()).isEqualTo(1);
        assertThat(progress.getErrorDetails()).hasSize(1);
        assertThat(progress.getErrorDetails().get(0).getRowNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("startImportJob: 존재하지 않는 도메인이면 ResourceNotFoundException 발생")
    void testStartImportNotFound() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        BulkImportDto.Request req = BulkImportDto.Request.builder()
                .domainId(domainId)
                .fileName("customers.csv")
                .rows(List.of(Map.of("name", "홍길동")))
                .build();

        assertThatThrownBy(() -> bulkImportService.startImportJob(req, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Domain not found");
    }
}
