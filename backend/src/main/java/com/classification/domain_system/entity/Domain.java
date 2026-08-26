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
@Table(name = "\"domain\"")
@Getter
@Setter
@NoArgsConstructor
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> name = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, String> description = new HashMap<>();

    @Column(name = "identifier_field_id")
    private UUID identifierFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identifier_field_id", insertable = false, updatable = false)
    private FieldDefinition identifierField;

    @Column(name = "display_name_field_id")
    private UUID displayNameFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "display_name_field_id", insertable = false, updatable = false)
    private FieldDefinition displayNameField;

    @Column(name = "description_field_id")
    private UUID descriptionFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_field_id", insertable = false, updatable = false)
    private FieldDefinition descriptionField;

    @Column(name = "image_field_id")
    private UUID imageFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_field_id", insertable = false, updatable = false)
    private FieldDefinition imageField;

    @Column(name = "icon")
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "numbering_pattern")
    private String numberingPattern;

    @Column(name = "auto_dq_scan_enabled", nullable = false)
    private boolean autoDqScanEnabled = false;

    @Column(name = "domain_type", length = 50)
    private String domainType = "GENERAL";

    @Column(name = "specialized_category", length = 50)
    private String specializedCategory;

    @Column(name = "current_sequence", nullable = false, columnDefinition = "bigint default 0")
    private Long currentSequence = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail_layout_config")
    private Map<String, Object> detailLayoutConfig = new HashMap<>();

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
