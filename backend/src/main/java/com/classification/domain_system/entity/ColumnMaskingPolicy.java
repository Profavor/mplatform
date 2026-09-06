package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "column_masking_policy", indexes = {
        @Index(name = "idx_cmp_domain_id", columnList = "domain_id"),
        @Index(name = "idx_cmp_field_key", columnList = "field_key"),
        @Index(name = "idx_cmp_target", columnList = "target_type, target_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnMaskingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId; // null이면 전역(Global) 정책

    @Column(name = "field_key", nullable = false, length = 128)
    private String fieldKey;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType; // ROLE, DEPARTMENT

    @Column(name = "target_id", nullable = false, length = 128)
    private String targetId; // 역할명(예: ROLE_CS_MANAGER) 또는 부서 UUID

    @Column(name = "masking_action", nullable = false, length = 32)
    @Builder.Default
    private String maskingAction = "UNMASK"; // UNMASK, MASK

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
