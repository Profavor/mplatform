package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "master_relation", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source_domain_id", "source_field_key"})
})
@Getter
@Setter
@NoArgsConstructor
public class MasterRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_domain_id", nullable = false)
    private UUID sourceDomainId;

    @Column(name = "source_field_key", nullable = false, length = 100)
    private String sourceFieldKey;

    @Column(name = "target_domain_id", nullable = false)
    private UUID targetDomainId;

    @Column(name = "relation_type", nullable = false, length = 50)
    private String relationType; // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY

    @Column(name = "cascade_policy", nullable = false, length = 50)
    private String cascadePolicy; // RESTRICT, SET_NULL, CASCADE

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
