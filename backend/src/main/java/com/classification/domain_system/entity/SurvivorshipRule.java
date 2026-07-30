package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "survivorship_rule")
@Getter
@Setter
@NoArgsConstructor
public class SurvivorshipRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    @Column(name = "field_key")
    private String fieldKey;

    @Column(name = "strategy", nullable = false, length = 50)
    private String strategy;

    @Column(name = "priority", nullable = false)
    private Integer priority;
}
