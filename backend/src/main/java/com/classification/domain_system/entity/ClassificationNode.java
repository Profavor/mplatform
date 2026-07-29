package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "classification_node")
@Getter
@Setter
@NoArgsConstructor
public class ClassificationNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ClassificationNode parent;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "axis_id")
    private ClassificationAxis axis;

    @OneToMany(mappedBy = "parent")
    @OrderBy("order ASC")
    @org.hibernate.annotations.SQLRestriction("is_deleted = false")
    private java.util.List<ClassificationNode> children = new java.util.ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> name = new HashMap<>();

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(nullable = false)
    private Integer depth;

    @Column(name = "node_order", nullable = false)
    private Integer order;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "icon")
    private String icon;

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

    @com.fasterxml.jackson.annotation.JsonProperty("domainName")
    public Map<String, String> getDomainName() {
        return this.domain != null ? this.domain.getName() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("axisId")
    public UUID getAxisId() {
        return this.axis != null ? this.axis.getId() : null;
    }
}
