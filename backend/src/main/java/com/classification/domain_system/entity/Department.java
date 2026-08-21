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
@Table(name = "department", indexes = {
    @Index(name = "uk_department_org_name", columnList = "organization_id, name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {

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

    @Column(name = "parent_department_id")
    private UUID parentDepartmentId;

    @Column(columnDefinition = "TEXT")
    private String description;



    // 단순 기본 값 타입 컬렉션(Set<String>)이며 부서별 역할 인가 확인 시 항상 함께 조회되고 크기가 수십 건 이하로 작아 EAGER 유지
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "department_roles", 
            joinColumns = @JoinColumn(name = "department_id"),
            indexes = @Index(name = "uk_department_roles", columnList = "department_id, role_name", unique = true)
    )
    @Column(name = "role_name")
    private Set<String> roles = new HashSet<>();

    @Column(length = 100)
    private String icon;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
