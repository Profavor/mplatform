package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "business_terms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "term_name", nullable = false)
    @Builder.Default
    private Map<String, String> termName = new HashMap<>();

    @Column(name = "term_code", nullable = false, unique = true, length = 100)
    private String termCode;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "abbreviation", length = 50)
    private String abbreviation;

    @Column(name = "synonyms", length = 500)
    private String synonyms; // Comma-separated synonyms

    @Column(name = "data_type", length = 50)
    private String dataType; // STRING, NUMBER, DATE, etc.

    @Column(name = "sensitivity_level", length = 30)
    @Builder.Default
    private String sensitivityLevel = "GENERAL"; // GENERAL, SENSITIVE, CRITICAL

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
