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

    @Column(name = "requester_id", nullable = false, length = 100)
    private String requesterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User requester;

    @Transient
    private String requesterName;

    public String getRequesterName() {
        if (requester != null && requester.getUsername() != null) {
            return requester.getUsername();
        }
        return requesterName != null ? requesterName : requesterId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("requesterUsername")
    public String getRequesterUsername() {
        return getRequesterName();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private ClassificationNode classificationNode;

    @com.fasterxml.jackson.annotation.JsonProperty("domainName")
    public Object getDomainName() {
        if (classificationNode != null && classificationNode.getDomain() != null) {
            return classificationNode.getDomain().getName();
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("domainId")
    public UUID getDomainId() {
        if (classificationNode != null && classificationNode.getDomain() != null) {
            return classificationNode.getDomain().getId();
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("classificationName")
    public Object getClassificationName() {
        if (classificationNode != null) {
            return classificationNode.getName();
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("classificationId")
    public UUID getClassificationId() {
        if (classificationNode != null) {
            return classificationNode.getId();
        }
        return null;
    }

    @Column(name = "current_step_order")
    private Integer currentStepOrder;

    @Transient
    private List<ApprovalStep> steps = new ArrayList<>();

    public void addStep(ApprovalStep step) {
        if (step != null) {
            this.steps.add(step);
            step.setApprovalRequest(this);
        }
    }

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String changes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "observer_ids")
    private String observerIds;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

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
