package com.classification.domain_system.repository;

import org.springframework.stereotype.Component;

@Component
public class PostgresJsonQueryStrategy implements JsonQueryStrategy {

    @Override
    public String buildJsonContainsClause(String columnAlias, String key, String paramName) {
        // PostgreSQL: FUNCTION('jsonb_extract_path_text', r.data, 'key') LIKE ... or jsonb @>
        // But since we want partial matches, we typically use text extraction and LIKE in JPQL.
        // Or if we want exact JSON containment:
        // return "function('jsonb_contains', " + columnAlias + ", :" + paramName + ") = true";
        
        // Actually, the original code used function('jsonb_extract_path_text', ...)
        // Wait, the original PostgreSQL logic in CustomRecordRepositoryImpl was:
        // "function('jsonb_extract_path_text', " + columnAlias + ".data, '" + key + "') LIKE :" + paramName
        return "function('jsonb_extract_path_text', " + columnAlias + ".data, '" + key + "') LIKE :" + paramName;
    }

    @Override
    public String buildJsonExtractForOrderBy(String columnAlias, String key) {
        return "function('jsonb_extract_path_text', " + columnAlias + ".data, '" + key + "')";
    }
}
