package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

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

    @Column(name = "node_id")
    private UUID nodeId;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType; // CREATE, UPDATE, DELETE, SCHEMA_CHANGE

    @Column(name = "name", columnDefinition = "text")
    private String name; // Multilingual JSON e.g. {"ko":"국내주식 등록 서식","en":"Domestic Stock Form"}

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

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
