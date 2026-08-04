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
@Table(name = "record")
@Getter
@Setter
@NoArgsConstructor
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
    @Column(name = "version", nullable = false, columnDefinition = "integer default 1")
    private Integer version = 1;

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
        updateSearchableData();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateSearchableData();
    }

    private void updateSearchableData() {
        try {
            if (this.node != null && this.data != null) {
                com.classification.domain_system.service.RecordService recordService = 
                    com.classification.domain_system.context.ApplicationContextProvider.getApplicationContext().getBean(com.classification.domain_system.service.RecordService.class);
                this.searchableData = recordService.generateSearchableData(this.node.getId(), this.data);
            }
        } catch (Exception e) {
            // Ignore if context is not ready (e.g. during tests) or if there's an error.
        }
    }
}
