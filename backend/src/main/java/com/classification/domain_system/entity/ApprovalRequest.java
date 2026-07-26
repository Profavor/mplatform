package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "approval_request")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // SCHEMA, RECORD

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Transient
    private String requesterName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id")
    private ClassificationNode classificationNode;

    @Column(name = "current_step_order")
    private Integer currentStepOrder;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ApprovalStep> steps = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String changes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "observer_ids")
    private String observerIds;

    @Transient
    private List<String> observerNames;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(columnDefinition = "bigint default 0")
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
