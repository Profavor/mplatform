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
@Table(name = "field_definition")
@Getter
@Setter
@NoArgsConstructor
public class FieldDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defined_at_node_id", nullable = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ClassificationNode definedAtNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Domain domain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> name = new HashMap<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_group_id", nullable = true)
    private FieldGroup fieldGroup;

    @Column(name = "field_key", nullable = false, length = 100)
    private String key;

    @Column(nullable = false, length = 50)
    private String type; // TEXT, NUMBER, DATE, etc.

    @Column(name = "unit", length = 50)
    private String unit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private String options;

    @Column(nullable = false)
    private Boolean required = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "field_order", nullable = false)
    private Integer order;

    @Column(name = "grid_width")
    private Integer gridWidth;

    @Column(name = "table_column_width")
    private Integer tableColumnWidth;

    @Column(name = "is_highlighted", columnDefinition = "boolean default false")
    private Boolean isHighlighted = false;

    @Column(name = "is_removed", nullable = false)
    private Boolean isRemoved = false;

    @Column(name = "is_multi_value", nullable = false)
    private Boolean isMultiValue = false;

    @Column(name = "is_table", nullable = false)
    private Boolean isTable = false;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = false;

    @Column(name = "is_read_only", columnDefinition = "boolean default false")
    private Boolean isReadOnly = false;

    @Column(name = "is_immutable", columnDefinition = "boolean default false")
    private Boolean isImmutable = false;

    @Column(name = "is_hidden", columnDefinition = "boolean default false")
    private Boolean isHidden = false;

    @Column(name = "masking_pattern", length = 50)
    private String maskingPattern;

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

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("domainId")
    public UUID getDomainId() {
        return domain != null ? domain.getId() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("definedAtNodeId")
    public UUID getDefinedAtNodeId() {
        return definedAtNode != null ? definedAtNode.getId() : null;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("approvalStatus")
    private String approvalStatus = "ACTIVE";

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("isPendingApproval")
    private Boolean isPendingApproval = false;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("approvalRequestId")
    private UUID approvalRequestId;

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
        if ("PENDING_APPROVAL".equalsIgnoreCase(approvalStatus) || "PENDING".equalsIgnoreCase(approvalStatus)) {
            this.isPendingApproval = true;
        }
    }

    public Boolean getIsPendingApproval() {
        return isPendingApproval;
    }

    public void setIsPendingApproval(Boolean pendingApproval) {
        isPendingApproval = pendingApproval;
    }

    public UUID getApprovalRequestId() {
        return approvalRequestId;
    }

    public void setApprovalRequestId(UUID approvalRequestId) {
        this.approvalRequestId = approvalRequestId;
    }
}
