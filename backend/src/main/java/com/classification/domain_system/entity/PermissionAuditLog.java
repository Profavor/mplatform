package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "permission_audit_log", indexes = {
        @Index(name = "idx_pal_target_user", columnList = "target_user_id"),
        @Index(name = "idx_pal_action_type", columnList = "action_type"),
        @Index(name = "idx_pal_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "target_user_id", length = 64)
    private String targetUserId;

    @Column(name = "target_username", length = 128)
    private String targetUsername;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType; // GRANT_ROLE, REVOKE_ROLE, GRANT_SCOPE, REVOKE_SCOPE, CREATE_POLICY, DELETE_POLICY

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType; // USER_ROLE, DATA_SCOPE, MASKING_POLICY

    @Column(name = "target_resource_id", length = 128)
    private String targetResourceId;

    @Column(name = "target_resource_name", length = 256)
    private String targetResourceName;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "changed_by", nullable = false, length = 64)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}
