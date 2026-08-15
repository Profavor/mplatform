package com.classification.domain_system.service;

import com.classification.domain_system.dto.ComplianceAuditReportDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.SensitiveDataAccessLog;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SensitiveDataAccessLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComplianceAuditServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;

    @InjectMocks
    private ComplianceAuditService auditService;

    private UUID recordId;
    private Record record;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        record = new Record();
        record.setId(recordId);
        record.setStatus("ACTIVE");
        record.setVersion(2);
        record.setCreatedAt(LocalDateTime.now().minusDays(5));
        record.setSourceSystem("WEB_ADMIN");

        ClassificationNode node = new ClassificationNode();
        node.setName(Map.of("ko", "VIP 고객"));
        record.setNode(node);
    }

    @Test
    @DisplayName("getRecordLifecycleReport: 5대 감사 로그 통합 타임라인 집계 검증")
    void testGetRecordLifecycleReport() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        RecordHistory h1 = new RecordHistory();
        h1.setRecordId(recordId);
        h1.setVersion(2);
        h1.setChangeType("UPDATE");
        h1.setChangedBy("admin");
        h1.setChangedAt(LocalDateTime.now().minusDays(3));
        when(recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId)).thenReturn(List.of(h1));

        ApprovalRequest req = new ApprovalRequest();
        req.setId(UUID.randomUUID());
        req.setTargetId(recordId);
        req.setStatus("APPROVED");
        req.setRequesterId("admin");
        req.setCreatedAt(LocalDateTime.now().minusDays(3).minusHours(1));
        when(approvalRequestRepository.findByTargetIdOrderByCreatedAtAsc(recordId)).thenReturn(List.of(req));

        SensitiveDataAccessLog accessLog = new SensitiveDataAccessLog();
        accessLog.setTargetId(recordId);
        accessLog.setTargetType("RECORD");
        accessLog.setUserId("auditor");
        accessLog.setAccessReason("정기 보안 감사");
        accessLog.setAccessedAt(LocalDateTime.now().minusDays(1));
        when(sensitiveDataAccessLogRepository.findByTargetIdAndTargetType(recordId, "RECORD")).thenReturn(List.of(accessLog));

        ComplianceAuditReportDto report = auditService.getRecordLifecycleReport(recordId);

        assertThat(report).isNotNull();
        assertThat(report.getRecordCode()).startsWith("REC-");
        assertThat(report.getNodeName()).isEqualTo("VIP 고객");
        assertThat(report.getLifecycleEvents()).isNotEmpty();

        // 1 (CREATION) + 1 (UPDATE) + 2 (APPROVAL_REQUEST + APPROVAL_APPROVED) + 1 (SENSITIVE_VIEW) = 5 events
        assertThat(report.getLifecycleEvents()).hasSize(5);

        List<String> eventTypes = report.getLifecycleEvents().stream()
                .map(ComplianceAuditReportDto.LifecycleEvent::getEventType)
                .toList();

        assertThat(eventTypes).contains("CREATION", "UPDATE", "APPROVAL_REQUEST", "APPROVAL_APPROVED", "SENSITIVE_VIEW");
    }

    @Test
    @DisplayName("getRecordLifecycleReport: 레코드 미존재 시 ResourceNotFoundException 발생")
    void testNotFound() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditService.getRecordLifecycleReport(recordId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Record not found");
    }
}
