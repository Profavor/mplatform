package com.classification.domain_system.repository;

public interface JsonQueryStrategy {
    /**
     * Builds a WHERE clause condition for JSON contains.
     * @param columnAlias the alias of the entity/column
     * @param key the JSON key
     * @param paramName the JPA named parameter to bind
     */
    String buildJsonContainsClause(String columnAlias, String key, String paramName);

    /**
     * Builds a SELECT/ORDER BY expression to extract a JSON string field.
     * @param columnAlias the alias of the entity/column
     * @param key the JSON key
     */
    String buildJsonExtractForOrderBy(String columnAlias, String key);
}
