package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.classification.domain_system.service.opensearch.RecordSyncListener;

@Entity
@Table(name = "record")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners({RecordSyncListener.class})
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private ClassificationNode node;

    @Column(nullable = false, length = 50)
    private String status; // DRAFT, PENDING_APPROVAL, ACTIVE, INACTIVE, MISMATCHED, REJECTED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String data;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approval_request_id")
    private UUID approvalRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", insertable = false, updatable = false)
    private ApprovalRequest approvalRequest;

    @jakarta.persistence.Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "merged_into_record_id")
    private UUID mergedIntoRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_into_record_id", insertable = false, updatable = false)
    private Record mergedIntoRecord;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "searchable_data")
    private String searchableData;

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
