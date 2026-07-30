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
@Table(name = "taxonomy_version")
@Getter
@Setter
@NoArgsConstructor
public class TaxonomyVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "version_label", nullable = false, length = 50)
    private String versionLabel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data")
    private String snapshotData;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
}
