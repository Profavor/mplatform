package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UNIQUE evaluator — checks domain-wide uniqueness via native PostgreSQL JSONB query.
 */
@Component
public class UniqueEvaluator implements RuleEvaluator {
    private final JdbcTemplate jdbcTemplate;
    private final org.springframework.core.env.Environment env;

    public UniqueEvaluator(JdbcTemplate jdbcTemplate, org.springframework.core.env.Environment env) {
        this.jdbcTemplate = jdbcTemplate;
        this.env = env;
    }

    private boolean isH2Database() {
        String url = env.getProperty("spring.datasource.url");
        return url != null && url.startsWith("jdbc:h2:");
    }

    @Override
    public DqRuleType supports() {
        return DqRuleType.UNIQUE;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (value == null || value.isNull()
                || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty(); // null values are not checked for uniqueness
        }
        if (context.getDomainId() == null) {
            return Optional.empty();
        }

        String textValue = value.asText();
        String fieldKey = field.getKey();
        
        String sql;
        Integer count;
        
        // Use LIKE for H2 (since JSON_VALUE is often missing or broken in older H2 versions), ->> for PostgreSQL
        boolean isH2 = isH2Database();
        
        String jsonCondition = isH2 ? "r.data LIKE ?" : "r.data->>'" + fieldKey + "' = ?";
        String paramValue = isH2 ? "%\"" + fieldKey + "\":\"" + textValue + "\"%" : textValue;

        if (context.getRecordId() != null) {
            sql = "SELECT COUNT(*) FROM record r " +
                  "JOIN classification_node cn ON r.node_id = cn.id " +
                  "WHERE cn.domain_id = ? AND " + jsonCondition + " " +
                  "AND r.status NOT IN ('REJECTED','MISMATCHED') AND r.id <> ?";
            count = jdbcTemplate.queryForObject(sql, Integer.class,
                    context.getDomainId(), paramValue, context.getRecordId());
        } else {
            sql = "SELECT COUNT(*) FROM record r " +
                  "JOIN classification_node cn ON r.node_id = cn.id " +
                  "WHERE cn.domain_id = ? AND " + jsonCondition + " " +
                  "AND r.status NOT IN ('REJECTED','MISMATCHED')";
            count = jdbcTemplate.queryForObject(sql, Integer.class,
                    context.getDomainId(), paramValue);
        }

        if (count != null && count > 0) {
            return Optional.of("Value '" + textValue + "' already exists. Must be unique within the domain.");
        }
        return Optional.empty();
    }
}
