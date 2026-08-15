package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bulk_import_job")
@Getter
@Setter
@NoArgsConstructor
public class BulkImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "total_rows", nullable = false)
    private int totalRows = 0;

    @Column(name = "processed_rows", nullable = false)
    private int processedRows = 0;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    @Column(name = "error_count", nullable = false)
    private int errorCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details_json", columnDefinition = "TEXT")
    private String errorDetailsJson = "[]";

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
