package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "integration_channels")
@Getter
@Setter
public class IntegrationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // WEB_SERVICE, JDBC, MESSAGE_QUEUE

    @Column(nullable = false, length = 20)
    @org.hibernate.annotations.ColumnDefault("'OUTBOUND'")
    private String direction = "OUTBOUND"; // INBOUND, OUTBOUND

    public String getDirection() {
        if (this.direction == null || this.direction.isBlank()) {
            return "OUTBOUND";
        }
        return this.direction;
    }

    @Column(name = "node_id")
    private UUID nodeId; // Associated Domain (ClassificationNode) ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", insertable = false, updatable = false)
    private ClassificationNode node;

    @Column(columnDefinition = "TEXT")
    private String configJson; // Target endpoint, DB connection, Topic name, etc.

    @Column(columnDefinition = "TEXT")
    private String mappingConfigJson; // SpEL mapping rules: { mappings: [{targetField: "...", sourceExpression: "..."}] }

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval = false;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;

    @Column(name = "retry_backoff_ms", nullable = false)
    private long retryBackoffMs = 1000L;

    @Column(name = "use_exponential_backoff", nullable = false)
    private boolean useExponentialBackoff = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
