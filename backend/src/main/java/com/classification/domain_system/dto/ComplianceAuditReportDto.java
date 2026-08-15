package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAuditReportDto {

    private UUID recordId;
    private String recordCode; // Raw UUID 방지용 식별 코드 (예: REC-340a0917)
    private String domainName;
    private String nodeName;
    private String currentStatus;
    private Integer currentVersion;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<LifecycleEvent> lifecycleEvents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LifecycleEvent {
        private String eventType; // CREATION, UPDATE, APPROVAL_REQUEST, APPROVAL_APPROVED, APPROVAL_REJECTED, SENSITIVE_VIEW, INTEGRATION_SYNC, ROLLBACK, RECLASSIFICATION
        private LocalDateTime timestamp;
        private String actorId;
        private String actorName;
        private String summary;
        private String detail;
    }
}
