package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Entity
@Table(name = "code_group")
@Getter
@Setter
@NoArgsConstructor
public class CodeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_code", nullable = false, unique = true, length = 50)
    private String groupCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, String> name = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, String> description = new HashMap<>();

    @Column(name = "organization_id")
    private UUID organizationId;

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
