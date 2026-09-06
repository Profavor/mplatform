package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_scope_permission", indexes = {
        @Index(name = "idx_dsp_user_id", columnList = "user_id"),
        @Index(name = "idx_dsp_domain_id", columnList = "domain_id"),
        @Index(name = "idx_dsp_node_id", columnList = "node_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataScopePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private ClassificationNode node;

    @Column(name = "permission_level", nullable = false, length = 32)
    @Builder.Default
    private String permissionLevel = "READ"; // READ, WRITE, ADMIN

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
