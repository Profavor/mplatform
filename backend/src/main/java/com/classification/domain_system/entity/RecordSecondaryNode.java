package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 레코드와 서브 분류축 노드 간의 다대다 매핑 엔티티.
 * 하나의 레코드는 기본 노드 외에도 다른 분류축의 노드에 동시 등록될 수 있습니다.
 */
@Entity
@Table(name = "record_secondary_node", indexes = {
        @Index(name = "idx_record_sec_record", columnList = "record_id"),
        @Index(name = "idx_record_sec_node", columnList = "node_id"),
        @Index(name = "idx_record_sec_axis", columnList = "axis_id"),
        @Index(name = "uk_record_axis", columnList = "record_id, axis_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class RecordSecondaryNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", insertable = false, updatable = false)
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private ClassificationNode node;

    @Column(name = "axis_id", nullable = false)
    private UUID axisId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
