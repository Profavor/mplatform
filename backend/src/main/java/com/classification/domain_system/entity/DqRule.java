package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dq_rule")
@Getter
@Setter
@NoArgsConstructor
public class DqRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private FieldDefinition fieldDefinition;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private DqRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private DqSeverity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params")
    private String params;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message")
    private Map<String, String> message = new HashMap<>();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("fieldDefinitionId")
    public UUID getFieldDefinitionId() {
        return fieldDefinition != null ? fieldDefinition.getId() : null;
    }
}
