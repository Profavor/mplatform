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
@Table(name = "dq_violation")
@Getter
@Setter
@NoArgsConstructor
public class DqViolation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false)
    private Record record;

    @Column(name = "dq_rule_id", nullable = false)
    private UUID dqRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dq_rule_id", insertable = false, updatable = false)
    private DqRule dqRule;

    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey;

    @Column(name = "severity", nullable = false, length = 10)
    private String severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message")
    private Map<String, String> message = new HashMap<>();

    @Column(name = "actual_value", length = 500)
    private String actualValue;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;
}
