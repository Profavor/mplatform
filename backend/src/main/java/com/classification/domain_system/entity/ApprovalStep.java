package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "approval_step")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalStep {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnoreProperties("steps")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @Column(name = "step_type", nullable = false, length = 20)
    private String stepType; // CONSENSUS, APPROVAL

    @Column(name = "assignee_id", length = 100)
    private String assigneeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User assigneeUser;

    @Transient
    private String assigneeName;

    public String getAssigneeName() {
        if (assigneeUser != null && assigneeUser.getUsername() != null) {
            return assigneeUser.getUsername();
        }
        return assigneeName != null ? assigneeName : assigneeId;
    }

    @Column(name = "assignee_role", length = 50)
    private String assigneeRole;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED, WAITING

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(length = 1000)
    private String comment;

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
        if (this.status == null) {
            this.status = "WAITING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
