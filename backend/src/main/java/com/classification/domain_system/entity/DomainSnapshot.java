package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "domain_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "snapshot_name", nullable = false, length = 100)
    private String snapshotName;

    @Column(name = "version_tag", nullable = false, length = 50)
    private String versionTag;

    @Column(name = "record_count", nullable = false)
    private int recordCount;

    @Column(name = "snapshot_data", columnDefinition = "TEXT", nullable = false)
    private String snapshotData;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
