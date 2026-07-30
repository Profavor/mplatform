package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Record;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class CustomRecordRepositoryImpl implements CustomRecordRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private Boolean isH2Cache = null;

    private boolean isH2Database() {
        if (isH2Cache != null) return isH2Cache;
        try {
            if (entityManager != null) {
                Map<String, Object> props = entityManager.getEntityManagerFactory().getProperties();
                for (Object val : props.values()) {
                    if (val != null && val.toString().toLowerCase().contains("h2")) {
                        isH2Cache = true;
                        return true;
                    }
                }
                org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
                if (session != null) {
                    Boolean res = session.doReturningWork(conn -> {
                        String name = conn.getMetaData().getDatabaseProductName();
                        return name != null && name.toLowerCase().contains("h2");
                    });
                    if (res != null) {
                        isH2Cache = res;
                        return res;
                    }
                }
            }
        } catch (Exception ignored) {}
        isH2Cache = false;
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Record> findDynamicRecords(List<UUID> nodeIds, String status, Map<String, String> searchParams, Pageable pageable) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable != null ? pageable : Pageable.unpaged(), 0);
        }

        StringBuilder nodeInClause = new StringBuilder("r.node_id IN (");
        for (int i = 0; i < nodeIds.size(); i++) {
            if (i > 0) nodeInClause.append(", ");
            nodeInClause.append(":nodeId_").append(i);
        }
        nodeInClause.append(") ");

        StringBuilder sql = new StringBuilder(
            "SELECT r.* FROM record r " +
            "WHERE " + nodeInClause +
            "AND r.status NOT IN ('REJECTED', 'MISMATCHED') "
        );
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM record r " +
            "WHERE " + nodeInClause +
            "AND r.status NOT IN ('REJECTED', 'MISMATCHED') "
        );

        if (status != null && !status.isEmpty()) {
            sql.append(" AND r.status = :status ");
            countSql.append(" AND r.status = :status ");
        }

        boolean isH2 = isH2Database();
        int paramIndex = 0;
        if (searchParams != null) {
            for (String key : searchParams.keySet()) {
                if (key.startsWith("op_") || key.endsWith("_max")) continue;
                
                String safeKey = key.replaceAll("[^a-zA-Z0-9_]", "_");
                String op = searchParams.getOrDefault("op_" + key, "EQ");
                
                if ("EQ".equals(op) || "CONTAINS".equals(op) || "STARTS_WITH".equals(op) || "ENDS_WITH".equals(op)) {
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        String pgPrefix = 
                            "CAST(r.data AS jsonb) @> CAST(:searchValStr" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValStrLower" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValNum" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValNumLower" + paramIndex + " AS jsonb) OR ";

                        cond = " AND (" + pgPrefix +
                               " (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey + "'->>'ko', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey + "'->>'en', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey.toLowerCase() + "'->>'ko', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey.toLowerCase() + "'->>'en', '') ILIKE :searchValLike" + paramIndex + ")) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                } else if ("BETWEEN".equals(op)) {
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        cond = " AND ( (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') AS NUMERIC) BETWEEN :searchValMin" + paramIndex + " AND :searchValMax" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') AS NUMERIC) BETWEEN :searchValMin" + paramIndex + " AND :searchValMax" + paramIndex + ") ) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                } else {
                    String sqlOp = switch (op) {
                        case "GT" -> ">";
                        case "LT" -> "<";
                        case "GTE" -> ">=";
                        case "LTE" -> "<=";
                        default -> "=";
                    };
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        cond = " AND ( (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') AS NUMERIC) " + sqlOp + " :searchVal" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') AS NUMERIC) " + sqlOp + " :searchVal" + paramIndex + ") ) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                }
            }
        }

        appendOrderByClause(sql, pageable);

        if (pageable != null && pageable.isPaged()) {
            sql.append(" LIMIT :limit OFFSET :offset");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Record.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        
        for (int i = 0; i < nodeIds.size(); i++) {
            query.setParameter("nodeId_" + i, nodeIds.get(i));
            countQuery.setParameter("nodeId_" + i, nodeIds.get(i));
        }

        if (status != null && !status.isEmpty()) {
            query.setParameter("status", status);
            countQuery.setParameter("status", status);
        }

        paramIndex = 0;
        if (searchParams != null) {
            for (String key : searchParams.keySet()) {
                if (key.startsWith("op_") || key.endsWith("_max")) continue;
                String op = searchParams.getOrDefault("op_" + key, "EQ");
                String val = searchParams.get(key);
                String safeKey = key.replaceAll("[^a-zA-Z0-9_]", "_");
                
                if ("EQ".equals(op) || "CONTAINS".equals(op) || "STARTS_WITH".equals(op) || "ENDS_WITH".equals(op)) {
                    if (!isH2) {
                        String strVal = "{\"" + safeKey + "\": \"" + val.replace("\"", "\\\"") + "\"}";
                        String strValLower = "{\"" + safeKey.toLowerCase() + "\": \"" + val.replace("\"", "\\\"") + "\"}";
                        query.setParameter("searchValStr" + paramIndex, strVal);
                        countQuery.setParameter("searchValStr" + paramIndex, strVal);
                        query.setParameter("searchValStrLower" + paramIndex, strValLower);
                        countQuery.setParameter("searchValStrLower" + paramIndex, strValLower);
                    }
                    String likeVal = switch (op) {
                        case "EQ" -> isH2 ? "%" + val + "%" : val;
                        case "STARTS_WITH" -> val + "%";
                        case "ENDS_WITH" -> "%" + val;
                        default -> "%" + val + "%";
                    };
                    query.setParameter("searchValLike" + paramIndex, likeVal);
                    countQuery.setParameter("searchValLike" + paramIndex, likeVal);
                    if (!isH2) {
                        if (val != null && val.matches("-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                            String numVal = "{\"" + safeKey + "\": " + val + "}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": " + val + "}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        } else if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val)) {
                            String numVal = "{\"" + safeKey + "\": " + (val != null ? val.toLowerCase() : "false") + "}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": " + (val != null ? val.toLowerCase() : "false") + "}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        } else {
                            String numVal = "{\"" + safeKey + "\": null}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": null}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        }
                    }
                } else if ("BETWEEN".equals(op)) {
                    if (isH2) {
                        query.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                        countQuery.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                    } else {
                        String maxVal = searchParams.get(key + "_max");
                        double vMin = Double.parseDouble(val);
                        double vMax = Double.parseDouble(maxVal != null && !maxVal.isEmpty() ? maxVal : val);
                        query.setParameter("searchValMin" + paramIndex, vMin);
                        countQuery.setParameter("searchValMin" + paramIndex, vMin);
                        query.setParameter("searchValMax" + paramIndex, vMax);
                        countQuery.setParameter("searchValMax" + paramIndex, vMax);
                    }
                } else {
                    if (isH2) {
                        query.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                        countQuery.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                    } else {
                        double sVal = Double.parseDouble(val);
                        query.setParameter("searchVal" + paramIndex, sVal);
                        countQuery.setParameter("searchVal" + paramIndex, sVal);
                    }
                }
                paramIndex++;
            }
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (pageable != null && pageable.isPaged()) {
            query.setParameter("limit", pageable.getPageSize());
            query.setParameter("offset", pageable.getOffset());
        }

        List<Record> results = query.getResultList();
        return new PageImpl<>(results, pageable != null ? pageable : Pageable.unpaged(), total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Record> findDynamicRecordsByDomain(UUID domainId, Map<String, String> searchParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder(
            "SELECT r.* FROM record r " +
            "JOIN classification_node n ON r.node_id = n.id " +
            "WHERE n.domain_id = :domainId " +
            "AND r.status NOT IN ('REJECTED', 'MISMATCHED') "
        );
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM record r " +
            "JOIN classification_node n ON r.node_id = n.id " +
            "WHERE n.domain_id = :domainId " +
            "AND r.status NOT IN ('REJECTED', 'MISMATCHED') "
        );

        boolean isH2 = isH2Database();
        int paramIndex = 0;
        if (searchParams != null) {
            for (String key : searchParams.keySet()) {
                if (key.startsWith("op_") || key.endsWith("_max")) continue;
                
                String safeKey = key.replaceAll("[^a-zA-Z0-9_]", "_");
                String op = searchParams.getOrDefault("op_" + key, "EQ");
                
                if ("EQ".equals(op) || "CONTAINS".equals(op) || "STARTS_WITH".equals(op) || "ENDS_WITH".equals(op)) {
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        String pgPrefix = 
                            "CAST(r.data AS jsonb) @> CAST(:searchValStr" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValStrLower" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValNum" + paramIndex + " AS jsonb) " +
                            " OR CAST(r.data AS jsonb) @> CAST(:searchValNumLower" + paramIndex + " AS jsonb) OR ";

                        cond = " AND (" + pgPrefix +
                               " (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey + "'->>'ko', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey + "'->>'en', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey.toLowerCase() + "'->>'ko', '') ILIKE :searchValLike" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->'" + safeKey.toLowerCase() + "'->>'en', '') ILIKE :searchValLike" + paramIndex + ")) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                } else if ("BETWEEN".equals(op)) {
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        cond = " AND ( (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') AS NUMERIC) BETWEEN :searchValMin" + paramIndex + " AND :searchValMax" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') AS NUMERIC) BETWEEN :searchValMin" + paramIndex + " AND :searchValMax" + paramIndex + ") ) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                } else {
                    String sqlOp = switch (op) {
                        case "GT" -> ">";
                        case "LT" -> "<";
                        case "GTE" -> ">=";
                        case "LTE" -> "<=";
                        default -> "=";
                    };
                    String cond;
                    if (isH2) {
                        cond = " AND (CAST(r.data AS VARCHAR) LIKE :searchValLike" + paramIndex + ") ";
                    } else {
                        cond = " AND ( (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey + "', '') AS NUMERIC) " + sqlOp + " :searchVal" + paramIndex + ") " +
                               " OR (NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') ~ '^[0-9]+(\\.[0-9]+)?$' " +
                               " AND CAST(NULLIF(CAST(r.data AS jsonb)->>'" + safeKey.toLowerCase() + "', '') AS NUMERIC) " + sqlOp + " :searchVal" + paramIndex + ") ) ";
                    }
                    sql.append(cond);
                    countSql.append(cond);
                    paramIndex++;
                }
            }
        }

        appendOrderByClause(sql, pageable);

        if (pageable != null && pageable.isPaged()) {
            sql.append(" LIMIT :limit OFFSET :offset");
        }

        Query query = entityManager.createNativeQuery(sql.toString(), Record.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        
        query.setParameter("domainId", domainId);
        countQuery.setParameter("domainId", domainId);

        paramIndex = 0;
        if (searchParams != null) {
            for (String key : searchParams.keySet()) {
                if (key.startsWith("op_") || key.endsWith("_max")) continue;
                String op = searchParams.getOrDefault("op_" + key, "EQ");
                String val = searchParams.get(key);
                String safeKey = key.replaceAll("[^a-zA-Z0-9_]", "_");
                
                if ("EQ".equals(op) || "CONTAINS".equals(op) || "STARTS_WITH".equals(op) || "ENDS_WITH".equals(op)) {
                    if (!isH2) {
                        String strVal = "{\"" + safeKey + "\": \"" + val.replace("\"", "\\\"") + "\"}";
                        String strValLower = "{\"" + safeKey.toLowerCase() + "\": \"" + val.replace("\"", "\\\"") + "\"}";
                        query.setParameter("searchValStr" + paramIndex, strVal);
                        countQuery.setParameter("searchValStr" + paramIndex, strVal);
                        query.setParameter("searchValStrLower" + paramIndex, strValLower);
                        countQuery.setParameter("searchValStrLower" + paramIndex, strValLower);
                    }
                    String likeVal = switch (op) {
                        case "EQ" -> isH2 ? "%" + val + "%" : val;
                        case "STARTS_WITH" -> val + "%";
                        case "ENDS_WITH" -> "%" + val;
                        default -> "%" + val + "%";
                    };
                    query.setParameter("searchValLike" + paramIndex, likeVal);
                    countQuery.setParameter("searchValLike" + paramIndex, likeVal);
                    if (!isH2) {
                        if (val != null && val.matches("-?(0|[1-9]\\d*)(\\.\\d+)?")) {
                            String numVal = "{\"" + safeKey + "\": " + val + "}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": " + val + "}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        } else if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val)) {
                            String numVal = "{\"" + safeKey + "\": " + (val != null ? val.toLowerCase() : "false") + "}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": " + (val != null ? val.toLowerCase() : "false") + "}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        } else {
                            String numVal = "{\"" + safeKey + "\": null}";
                            String numValLower = "{\"" + safeKey.toLowerCase() + "\": null}";
                            query.setParameter("searchValNum" + paramIndex, numVal);
                            countQuery.setParameter("searchValNum" + paramIndex, numVal);
                            query.setParameter("searchValNumLower" + paramIndex, numValLower);
                            countQuery.setParameter("searchValNumLower" + paramIndex, numValLower);
                        }
                    }
                } else if ("BETWEEN".equals(op)) {
                    if (isH2) {
                        query.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                        countQuery.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                    } else {
                        String maxVal = searchParams.get(key + "_max");
                        double vMin = Double.parseDouble(val);
                        double vMax = Double.parseDouble(maxVal != null && !maxVal.isEmpty() ? maxVal : val);
                        query.setParameter("searchValMin" + paramIndex, vMin);
                        countQuery.setParameter("searchValMin" + paramIndex, vMin);
                        query.setParameter("searchValMax" + paramIndex, vMax);
                        countQuery.setParameter("searchValMax" + paramIndex, vMax);
                    }
                } else {
                    if (isH2) {
                        query.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                        countQuery.setParameter("searchValLike" + paramIndex, "%" + val + "%");
                    } else {
                        double sVal = Double.parseDouble(val);
                        query.setParameter("searchVal" + paramIndex, sVal);
                        countQuery.setParameter("searchVal" + paramIndex, sVal);
                    }
                }
                paramIndex++;
            }
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (pageable != null && pageable.isPaged()) {
            query.setParameter("limit", pageable.getPageSize());
            query.setParameter("offset", pageable.getOffset());
        }

        List<Record> results = query.getResultList();
        return new PageImpl<>(results, pageable != null ? pageable : Pageable.unpaged(), total);
    }

    private void appendOrderByClause(StringBuilder sql, Pageable pageable) {
        if (pageable != null && pageable.getSort().isSorted()) {
            sql.append(" ORDER BY ");
            boolean first = true;
            for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                if (!first) sql.append(", ");
                String prop = order.getProperty();
                if (prop.startsWith("data.")) {
                    prop = prop.substring(5);
                }
                String dir = order.getDirection().name();
                if ("id".equalsIgnoreCase(prop)) {
                    sql.append("r.id ").append(dir);
                } else if ("status".equalsIgnoreCase(prop)) {
                    sql.append("r.status ").append(dir);
                } else if ("createdAt".equalsIgnoreCase(prop) || "created_at".equalsIgnoreCase(prop)) {
                    sql.append("r.created_at ").append(dir);
                } else if ("updatedAt".equalsIgnoreCase(prop) || "updated_at".equalsIgnoreCase(prop)) {
                    sql.append("r.updated_at ").append(dir);
                } else if ("nodeName".equalsIgnoreCase(prop) || "node".equalsIgnoreCase(prop)) {
                    sql.append("r.node_id ").append(dir);
                } else {
                    boolean isH2 = isH2Database();
                    if (isH2) {
                        sql.append("r.created_at ").append(dir);
                    } else {
                        String safeProp = prop.replaceAll("[^a-zA-Z0-9_]", "_");
                        String lowerProp = safeProp.toLowerCase();
                        sql.append("CASE WHEN NULLIF(CAST(r.data AS jsonb)->>'").append(safeProp).append("', '') ~ '^-?[0-9]+(\\\\.[0-9]+)?$' ")
                           .append("THEN CAST(NULLIF(CAST(r.data AS jsonb)->>'").append(safeProp).append("', '') AS NUMERIC) ")
                           .append("WHEN NULLIF(CAST(r.data AS jsonb)->>'").append(lowerProp).append("', '') ~ '^-?[0-9]+(\\\\.[0-9]+)?$' ")
                           .append("THEN CAST(NULLIF(CAST(r.data AS jsonb)->>'").append(lowerProp).append("', '') AS NUMERIC) ")
                           .append("ELSE NULL END ").append(dir).append(" NULLS LAST, ");
                        sql.append("COALESCE(NULLIF(CAST(r.data AS jsonb)->>'").append(safeProp).append("', ''), NULLIF(CAST(r.data AS jsonb)->>'")
                           .append(lowerProp).append("', '')) ").append(dir).append(" NULLS LAST");
                    }
                }
                first = false;
            }
        } else {
            sql.append(" ORDER BY r.created_at DESC");
        }
    }
}
