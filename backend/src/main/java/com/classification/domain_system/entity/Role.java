package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDateTime;

@Entity
@Table(name = "role", indexes = {
    @Index(name = "uk_role_org_name", columnList = "organization_id, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 단순 기본 값 타입 컬렉션(Set<String>)이며 인증/인가 컨텍스트에서 역할 조회 시 항상 함께 필요하고 크기가 수십 건 이하로 작아 EAGER 유지
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions", 
            joinColumns = @JoinColumn(name = "role_id"),
            indexes = @Index(name = "uk_role_permissions", columnList = "role_id, permission", unique = true)
    )
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>();

    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.isSystemRole == null) this.isSystemRole = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
