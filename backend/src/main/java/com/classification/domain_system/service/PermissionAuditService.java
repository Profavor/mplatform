package com.classification.domain_system.service;

import com.classification.domain_system.dto.PermissionAuditLogDto;
import com.classification.domain_system.entity.PermissionAuditLog;
import com.classification.domain_system.repository.PermissionAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAuditService {

    private final PermissionAuditLogRepository auditLogRepository;

    @Transactional
    public void recordLog(String targetUserId, String targetUsername, String actionType,
                          String resourceType, String targetResourceId, String targetResourceName,
                          String beforeValue, String afterValue, String changedBy, String clientIp) {
        try {
            PermissionAuditLog logEntry = PermissionAuditLog.builder()
                    .targetUserId(targetUserId)
                    .targetUsername(targetUsername)
                    .actionType(actionType)
                    .resourceType(resourceType)
                    .targetResourceId(targetResourceId)
                    .targetResourceName(targetResourceName)
                    .beforeValue(beforeValue)
                    .afterValue(afterValue)
                    .changedBy(changedBy != null ? changedBy : "SYSTEM")
                    .changedAt(LocalDateTime.now())
                    .clientIp(clientIp)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("[PermissionAudit] Failed to save permission audit log", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<PermissionAuditLogDto.Response> getAuditLogs(String targetUserId, Pageable pageable) {
        Page<PermissionAuditLog> page = targetUserId != null && !targetUserId.isBlank()
                ? auditLogRepository.findByTargetUserIdOrderByChangedAtDesc(targetUserId, pageable)
                : auditLogRepository.findAllByOrderByChangedAtDesc(pageable);

        return page.map(this::toResponse);
    }

    public PermissionAuditLogDto.Response toResponse(PermissionAuditLog log) {
        String logCode = "LOG-" + (log.getId() != null ? log.getId().toString().substring(0, 8) : "00000000");
        return PermissionAuditLogDto.Response.builder()
                .id(log.getId())
                .logCode(logCode)
                .targetUserId(log.getTargetUserId())
                .targetUsername(log.getTargetUsername())
                .actionType(log.getActionType())
                .resourceType(log.getResourceType())
                .targetResourceId(log.getTargetResourceId())
                .targetResourceName(log.getTargetResourceName())
                .beforeValue(log.getBeforeValue())
                .afterValue(log.getAfterValue())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .clientIp(log.getClientIp())
                .build();
    }
}
