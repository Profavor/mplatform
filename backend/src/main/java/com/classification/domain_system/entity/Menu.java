package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menu")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String path;

    @Column(length = 50)
    private String icon;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder;



    // 단순 기본 값 타입 컬렉션(Set<String>)이며 메뉴 인가 체크 시 메뉴 엔티티와 항상 함께 조회되고 크기가 작아 EAGER 유지
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "menu_roles", 
            joinColumns = @JoinColumn(name = "menu_id"),
            indexes = @Index(name = "uk_menu_roles", columnList = "menu_id, role_name", unique = true)
    )
    @Column(name = "role_name")
    @Builder.Default
    private Set<String> requiredRoles = new HashSet<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
