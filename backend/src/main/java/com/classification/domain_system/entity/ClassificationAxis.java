package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 도메인 내의 분류축(Classification Axis) 정의 엔티티.
 * 하나의 도메인은 여러 분류축(예: 부서 축, 고용형태 축, 카테고리 축)을 가질 수 있습니다.
 */
@Entity
@Table(name = "classification_axis", indexes = {
        @Index(name = "idx_axis_domain_id", columnList = "domain_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ClassificationAxis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    /** 분류축 코드 (예: DEFAULT, DEPT, EMPLOYMENT) */
    @Column(name = "axis_code", nullable = false, length = 50)
    private String axisCode;

    /** 다국어 분류축 명칭 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> name = new HashMap<>();

    @Column(length = 1000)
    private String description;

    /** 기본 분류축 여부 (도메인당 1개) */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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

    @com.fasterxml.jackson.annotation.JsonProperty("domainId")
    public UUID getDomainId() {
        return this.domain != null ? this.domain.getId() : null;
    }
}
