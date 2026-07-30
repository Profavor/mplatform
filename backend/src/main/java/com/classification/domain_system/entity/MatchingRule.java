package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "matching_rule")
@Getter
@Setter
@NoArgsConstructor
public class MatchingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    @Column(name = "node_id")
    private UUID nodeId; // Nullable if applies to all nodes in domain

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", insertable = false, updatable = false)
    private ClassificationNode node;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_field_keys", nullable = false)
    private String targetFieldKeys; // e.g. ["email", "phone"]

    @Column(name = "match_type", nullable = false, length = 20)
    private String matchType; // EXACT, FUZZY

    @Column(name = "similarity_threshold")
    private Double similarityThreshold = 0.85;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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
