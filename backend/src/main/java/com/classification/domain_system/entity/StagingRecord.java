package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "staging_record")
@Getter
@Setter
@NoArgsConstructor
public class StagingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "node_id")
    private UUID nodeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data")
    private String rawData;

    @Column(name = "status", length = 50)
    private String status; // PENDING, VALIDATED, ERROR, COMMITTED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
