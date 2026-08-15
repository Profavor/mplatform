package com.classification.domain_system.service;

import com.classification.domain_system.dto.ComplianceAuditReportDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.SensitiveDataAccessLog;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SensitiveDataAccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceAuditService {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;

    @Transactional(readOnly = true)
    public ComplianceAuditReportDto getRecordLifecycleReport(UUID recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found: " + recordId));

        String domainName = (record.getNode() != null && record.getNode().getDomain() != null && record.getNode().getDomain().getName() != null)
                ? record.getNode().getDomain().getName().getOrDefault("ko", "도메인") : "도메인";
        String nodeName = (record.getNode() != null && record.getNode().getName() != null)
                ? record.getNode().getName().getOrDefault("ko", "분류 노드") : "분류 노드";

        String recordCode = "REC-" + record.getId().toString().substring(0, 8);

        List<ComplianceAuditReportDto.LifecycleEvent> events = new ArrayList<>();

        // 1. Initial Creation Event
        events.add(ComplianceAuditReportDto.LifecycleEvent.builder()
                .eventType("CREATION")
                .timestamp(record.getCreatedAt())
                .actorId(record.getSourceSystem() != null ? record.getSourceSystem() : "SYSTEM")
                .actorName(record.getSourceSystem() != null ? record.getSourceSystem() : "SYSTEM")
                .summary("마스터 레코드 초기 등록")
                .detail(String.format("분류: %s, 버전: 1", nodeName))
                .build());

        // 2. Change Histories (Updates, Reclassifications, Rollbacks)
        List<RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId);
        for (RecordHistory h : histories) {
            String type = "UPDATE";
            if (h.getChangeType() != null) {
                type = h.getChangeType();
            }

            events.add(ComplianceAuditReportDto.LifecycleEvent.builder()
                    .eventType(type)
                    .timestamp(h.getChangedAt())
                    .actorId(h.getChangedBy() != null ? h.getChangedBy() : "USER")
                    .actorName(h.getChangedBy() != null ? h.getChangedBy() : "USER")
                    .summary(String.format("데이터 변경 (v%d)", h.getVersion()))
                    .detail(String.format("변경 유형: %s, 출처: %s", h.getChangeType(), h.getSourceSystem() != null ? h.getSourceSystem() : "WEB"))
                    .build());
        }

        // 3. Approval Workflow Events
        List<ApprovalRequest> approvals = approvalRequestRepository.findByTargetIdOrderByCreatedAtAsc(recordId);
        for (ApprovalRequest a : approvals) {
            events.add(ComplianceAuditReportDto.LifecycleEvent.builder()
                    .eventType("APPROVAL_REQUEST")
                    .timestamp(a.getCreatedAt())
                    .actorId(a.getRequesterId())
                    .actorName(a.getRequesterName() != null ? a.getRequesterName() : a.getRequesterId())
                    .summary("변경 승인 결재 상신")
                    .detail(String.format("결재 상태: %s", a.getStatus()))
                    .build());

            if ("APPROVED".equals(a.getStatus()) || "REJECTED".equals(a.getStatus())) {
                events.add(ComplianceAuditReportDto.LifecycleEvent.builder()
                        .eventType("APPROVED".equals(a.getStatus()) ? "APPROVAL_APPROVED" : "APPROVAL_REJECTED")
                        .timestamp(a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt())
                        .actorId("APPROVER")
                        .actorName("결재 승인권자")
                        .summary("APPROVED".equals(a.getStatus()) ? "결재 승인 완료" : "결재 반려")
                        .detail(String.format("최종 처리 상태: %s", a.getStatus()))
                        .build());
            }
        }

        // 4. Sensitive Data View Access Logs
        List<SensitiveDataAccessLog> accessLogs = sensitiveDataAccessLogRepository.findByTargetIdAndTargetType(recordId, "RECORD");
        for (SensitiveDataAccessLog aLog : accessLogs) {
            events.add(ComplianceAuditReportDto.LifecycleEvent.builder()
                    .eventType("SENSITIVE_VIEW")
                    .timestamp(aLog.getAccessedAt())
                    .actorId(aLog.getUserId())
                    .actorName(aLog.getUsername() != null ? aLog.getUsername() : aLog.getUserId())
                    .summary("민감/암호화 데이터 복호화 열람")
                    .detail(String.format("열람 사유: %s (IP: %s, 필드: %s)", aLog.getAccessReason(), aLog.getIpAddress(), aLog.getFieldKeys()))
                    .build());
        }

        // Sort chronologically
        events.sort(Comparator.comparing(ComplianceAuditReportDto.LifecycleEvent::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));

        return ComplianceAuditReportDto.builder()
                .recordId(record.getId())
                .recordCode(recordCode)
                .domainName(domainName)
                .nodeName(nodeName)
                .currentStatus(record.getStatus())
                .currentVersion(record.getVersion())
                .createdAt(record.getCreatedAt())
                .createdBy(record.getSourceSystem())
                .lifecycleEvents(events)
                .build();
    }
}
