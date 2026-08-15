package com.classification.domain_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_routing_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRoutingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "domain_id", nullable = true)
    private UUID domainId;

    @Column(name = "condition_field", length = 50)
    private String conditionField;

    @Column(name = "condition_operator", length = 20)
    private String conditionOperator; // EQUALS, CONTAINS, GTE, SENSITIVE

    @Column(name = "condition_value", length = 100)
    private String conditionValue;

    @Column(name = "steps_json", columnDefinition = "TEXT", nullable = false)
    private String stepsJson; // e.g. [{"step": 1, "role": "DEPT_HEAD"}, {"step": 2, "role": "SECURITY_ADMIN"}]

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
