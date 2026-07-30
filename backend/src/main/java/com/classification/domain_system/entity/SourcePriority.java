package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "source_priority")
@Getter
@Setter
@NoArgsConstructor
public class SourcePriority {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    @Column(name = "source_system", nullable = false, length = 100)
    private String sourceSystem;

    @Column(name = "priority", nullable = false)
    private Integer priority; // 낮을수록 우선순위가 높음 (예: 1 > 2 > 3)
}
