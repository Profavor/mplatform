package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_candidate")
@Getter
@Setter
@NoArgsConstructor
public class MatchCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", insertable = false, updatable = false)
    private ClassificationNode node;

    @Column(name = "existing_record_id", nullable = false)
    private UUID existingRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "existing_record_id", insertable = false, updatable = false)
    private Record existingRecord;

    @Column(name = "incoming_data_json", columnDefinition = "TEXT", nullable = false)
    private String incomingDataJson;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "matched_field_details", columnDefinition = "TEXT")
    private String matchedFieldDetails;

    @Column(name = "domain_id")
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    @Column(name = "matched_rule_id")
    private UUID matchedRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_rule_id", insertable = false, updatable = false)
    private MatchingRule matchedRule;

    @Column(name = "source", nullable = false, length = 20)
    private String source; // MANUAL, INBOUND

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, CONFIRMED_MERGE, REJECTED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
