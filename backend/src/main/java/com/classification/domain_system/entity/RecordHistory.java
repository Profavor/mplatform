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
@Table(name = "record_history")
@Getter
@Setter
@NoArgsConstructor
public class RecordHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Record record;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // CREATE, UPDATE, DELETE

    @Column(name = "changed_by", nullable = true, length = 100)
    private String changedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_data")
    private String previousData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data")
    private String newData;

    @Column(name = "approval_request_id")
    private UUID approvalRequestId;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
