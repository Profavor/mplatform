package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_job")
@Getter
@Setter
@NoArgsConstructor
public class BatchJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "job_type", length = 50)
    private String jobType; // IMPORT, EXPORT, DQ_SCAN

    @Column(name = "status", length = 50)
    private String status; // QUEUED, RUNNING, COMPLETED, FAILED

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "processed_records")
    private Integer processedRecords = 0;

    @Column(name = "error_records")
    private Integer errorRecords = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "committed_records")
    private Integer committedRecords = 0;

    @Column(name = "approval_request_id")
    private UUID approvalRequestId;
}
