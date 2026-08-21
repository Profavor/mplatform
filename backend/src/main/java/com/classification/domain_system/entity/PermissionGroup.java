package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permission_group")
@Getter
@Setter
@NoArgsConstructor
public class PermissionGroup {

    @Id
    private String id; // e.g., 'domain', 'node', 'field', 'dq', 'org'

    @Column(nullable = false)
    private String code;

    @Column(name = "title_ko", nullable = false)
    private String titleKo;

    @Column(name = "title_en")
    private String titleEn;

    private String icon;

    private String color;

    @Column(name = "chip_class")
    private String chipClass;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<PermissionItem> items = new ArrayList<>();

    public void addItem(PermissionItem item) {
        items.add(item);
        item.setGroup(this);
    }

    public void removeItem(PermissionItem item) {
        items.remove(item);
        item.setGroup(null);
    }
}
