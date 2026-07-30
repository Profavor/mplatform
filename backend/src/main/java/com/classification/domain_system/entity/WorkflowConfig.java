package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "workflow_config")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // A workflow can be bound to either a Domain or a specific ClassificationNode.
    @Column(name = "domain_id")
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    @Column(name = "node_id")
    private UUID nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", insertable = false, updatable = false)
    private ClassificationNode node;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType; // CREATE, UPDATE, DELETE, SCHEMA_CHANGE

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name", columnDefinition = "text")
    private String name; // Multilingual JSON e.g. {"ko":"국내주식 등록 서식","en":"Domestic Stock Form"}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_config", columnDefinition = "text")
    private String stepsConfig;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
