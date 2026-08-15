package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "field_group")
@Getter
@Setter
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FieldGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Domain domain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> name = new HashMap<>();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_default_open", columnDefinition = "boolean default true")
    private Boolean isDefaultOpen = true;

    @com.fasterxml.jackson.annotation.JsonProperty("sectorId")
    public UUID getSectorId() {
        return sector != null ? sector.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("sector")
    public Map<String, Object> getSectorSummary() {
        if (sector == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", sector.getId());
        map.put("name", sector.getName());
        return map;
    }
}
