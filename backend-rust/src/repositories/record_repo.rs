use crate::error::AppError;
use crate::models::record::{PageResponse, Record, RecordHistory};
use sqlx::{PgPool, Postgres, QueryBuilder};
use std::collections::HashMap;
use uuid::Uuid;

#[derive(Debug, Clone, Default)]
pub struct DynamicRecordQuery {
    pub domain_id: Option<Uuid>,
    pub node_id: Option<Uuid>,
    pub include_children: bool,
    pub status: Option<String>,
    pub keyword: Option<String>,
    pub search_params: HashMap<String, String>,
    pub sort_field: Option<String>,
    pub sort_order: Option<String>,
    pub sort: Option<String>,
    pub page: i64,
    pub size: i64,
}

pub struct RecordRepository;

impl RecordRepository {
    pub async fn find_dynamic(
        pool: &PgPool,
        q: &DynamicRecordQuery,
    ) -> Result<PageResponse<Record>, AppError> {
        let mut count_builder = init_query_builder(q, true);
        apply_filters(&mut count_builder, q);
        let total: (i64,) = count_builder
            .build_query_as::<(i64,)>()
            .fetch_one(pool)
            .await?;

        let mut data_builder = init_query_builder(q, false);
        apply_filters(&mut data_builder, q);
        apply_order_by(&mut data_builder, q);

        data_builder.push(" LIMIT ");
        data_builder.push_bind(q.size);
        data_builder.push(" OFFSET ");
        data_builder.push_bind(q.page * q.size);

        let records: Vec<Record> = data_builder
            .build_query_as::<Record>()
            .fetch_all(pool)
            .await?;

        Ok(PageResponse::new(records, total.0, q.page, q.size))
    }

    pub async fn find_paginated(
        pool: &PgPool,
        node_id: Option<Uuid>,
        status: Option<&str>,
        page: i64,
        size: i64,
    ) -> Result<PageResponse<Record>, AppError> {
        let offset = page * size;

        // Count total
        let count_row: (i64,) = sqlx::query_as(
            r#"
            SELECT COUNT(*)
            FROM record
            WHERE ($1::uuid IS NULL OR node_id = $1)
              AND ($2::text IS NULL OR status = $2)
            "#,
        )
        .bind(node_id)
        .bind(status)
        .fetch_one(pool)
        .await?;

        let total = count_row.0;

        // Fetch paginated
        let records = sqlx::query_as::<_, Record>(
            r#"
            SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                   r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                   json_build_object('id', n.id, 'name', n.name) as node
            FROM record r
            LEFT JOIN classification_node n ON r.node_id = n.id
            WHERE ($1::uuid IS NULL OR r.node_id = $1)
              AND ($2::text IS NULL OR r.status = $2)
            ORDER BY r.created_at DESC
            LIMIT $3 OFFSET $4
            "#,
        )
        .bind(node_id)
        .bind(status)
        .bind(size)
        .bind(offset)
        .fetch_all(pool)
        .await?;

        Ok(PageResponse::new(records, total, page, size))
    }

    pub async fn find_by_id(pool: &PgPool, id: Uuid) -> Result<Option<Record>, AppError> {
        let record = sqlx::query_as::<_, Record>(
            r#"
            SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                   r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                   json_build_object('id', n.id, 'name', n.name) as node
            FROM record r
            LEFT JOIN classification_node n ON r.node_id = n.id
            WHERE r.id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(record)
    }

    pub async fn insert(
        pool: &PgPool,
        id: Uuid,
        node_id: Uuid,
        data: serde_json::Value,
        source_system: Option<&str>,
    ) -> Result<Record, AppError> {
        let record = sqlx::query_as::<_, Record>(
            r#"
            INSERT INTO record (
                id, node_id, data, searchable_data, status, version, source_system,
                created_at, updated_at
            )
            VALUES (
                $1, $2, $3, $3, 'ACTIVE', 1, $4,
                NOW(), NOW()
            )
            RETURNING
                id, node_id, data, searchable_data, status, version, source_system,
                merged_into_record_id, approval_request_id, created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(node_id)
        .bind(data)
        .bind(source_system)
        .fetch_one(pool)
        .await?;

        Ok(record)
    }

    pub async fn insert_history(
        pool: &PgPool,
        record_id: Uuid,
        version: i32,
        change_type: &str,
        changed_by: &str,
        previous_data: Option<serde_json::Value>,
        new_data: Option<serde_json::Value>,
        source_system: Option<&str>,
    ) -> Result<RecordHistory, AppError> {
        let history = sqlx::query_as::<_, RecordHistory>(
            r#"
            INSERT INTO record_history (
                id, record_id, version, change_type, changed_by,
                previous_data, new_data, source_system, changed_at
            )
            VALUES (
                gen_random_uuid(), $1, $2, $3, $4,
                $5, $6, $7, NOW()
            )
            RETURNING
                id, record_id, version, change_type, changed_by,
                previous_data, new_data, source_system, approval_request_id, changed_at
            "#,
        )
        .bind(record_id)
        .bind(version)
        .bind(change_type)
        .bind(changed_by)
        .bind(previous_data)
        .bind(new_data)
        .bind(source_system)
        .fetch_one(pool)
        .await?;

        Ok(history)
    }

    pub async fn find_histories_by_record_id(
        pool: &PgPool,
        record_id: Uuid,
    ) -> Result<Vec<RecordHistory>, AppError> {
        let histories = sqlx::query_as::<_, RecordHistory>(
            r#"
            SELECT id, record_id, version, change_type, changed_by,
                   previous_data, new_data, source_system, approval_request_id, changed_at
            FROM record_history
            WHERE record_id = $1
            ORDER BY version ASC
            "#,
        )
        .bind(record_id)
        .fetch_all(pool)
        .await?;

        Ok(histories)
    }
}

fn init_query_builder<'a>(
    q: &'a DynamicRecordQuery,
    is_count: bool,
) -> QueryBuilder<'a, Postgres> {
    let mut builder: QueryBuilder<'a, Postgres> = QueryBuilder::new("");

    if let Some(node_id) = q.node_id {
        if q.include_children {
            builder.push(
                r#"
                WITH RECURSIVE node_tree AS (
                    SELECT id FROM classification_node WHERE id = 
                "#,
            );
            builder.push_bind(node_id);
            builder.push(
                r#"
                    UNION ALL
                    SELECT c.id FROM classification_node c
                    INNER JOIN node_tree nt ON c.parent_id = nt.id
                    WHERE c.is_deleted = false
                )
                "#,
            );
            if is_count {
                builder.push("SELECT COUNT(r.id) FROM record r JOIN classification_node n ON r.node_id = n.id ");
            } else {
                builder.push(
                    r#"
                    SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                           r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                           json_build_object('id', n.id, 'name', n.name) as node
                    FROM record r
                    JOIN classification_node n ON r.node_id = n.id
                    "#,
                );
            }
            builder.push("WHERE (r.node_id IN (SELECT id FROM node_tree) OR EXISTS (SELECT 1 FROM record_secondary_node rsn WHERE rsn.record_id = r.id AND rsn.node_id IN (SELECT id FROM node_tree))) ");
        } else {
            if is_count {
                builder.push("SELECT COUNT(r.id) FROM record r JOIN classification_node n ON r.node_id = n.id ");
            } else {
                builder.push(
                    r#"
                    SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                           r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                           json_build_object('id', n.id, 'name', n.name) as node
                    FROM record r
                    JOIN classification_node n ON r.node_id = n.id
                    "#,
                );
            }
            builder.push("WHERE (r.node_id = ");
            builder.push_bind(node_id);
            builder.push(" OR EXISTS (SELECT 1 FROM record_secondary_node rsn WHERE rsn.record_id = r.id AND rsn.node_id = ");
            builder.push_bind(node_id);
            builder.push(")) ");
        }
    } else if let Some(domain_id) = q.domain_id {
        if is_count {
            builder.push("SELECT COUNT(r.id) FROM record r JOIN classification_node n ON r.node_id = n.id ");
        } else {
            builder.push(
                r#"
                SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                       r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                       json_build_object('id', n.id, 'name', n.name) as node
                FROM record r
                JOIN classification_node n ON r.node_id = n.id
                "#,
            );
        }
        builder.push("WHERE n.domain_id = ");
        builder.push_bind(domain_id);
        builder.push(" ");
    } else {
        if is_count {
            builder.push("SELECT COUNT(r.id) FROM record r LEFT JOIN classification_node n ON r.node_id = n.id WHERE 1=1 ");
        } else {
            builder.push(
                r#"
                SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                       r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                       json_build_object('id', n.id, 'name', n.name) as node
                FROM record r
                LEFT JOIN classification_node n ON r.node_id = n.id
                WHERE 1=1
                "#,
            );
        }
    }

    builder
}

fn apply_filters<'a>(
    builder: &mut QueryBuilder<'a, Postgres>,
    q: &'a DynamicRecordQuery,
) {
    if let Some(ref st) = q.status {
        builder.push(" AND r.status = ");
        builder.push_bind(st.as_str());
    } else {
        builder.push(" AND r.status NOT IN ('DELETED', 'REJECTED', 'MISMATCHED') ");
    }

    if let Some(ref kw) = q.keyword {
        let pattern = format!("%{}%", kw);
        builder.push(" AND (CAST(COALESCE(r.searchable_data, r.data) AS text) ILIKE ");
        builder.push_bind(pattern);
        builder.push(") ");
    }

    for (raw_key, val) in &q.search_params {
        let k = if let Some(stripped) = raw_key.strip_prefix("search_") {
            stripped
        } else {
            continue;
        };

        if k.starts_with("op_") || k.ends_with("_max") || k == "multi_keys" || k == "status" {
            continue;
        }

        let val = val.trim();
        if val.is_empty() {
            continue;
        }

        if k == "multi_val" {
            if let Some(multi_keys_str) = q
                .search_params
                .get("search_multi_keys")
                .or_else(|| q.search_params.get("multi_keys"))
            {
                let pattern = format!("%{}%", val);
                let fields: Vec<&str> = multi_keys_str
                    .split(',')
                    .map(|s| s.trim())
                    .filter(|s| !s.is_empty())
                    .collect();
                if !fields.is_empty() {
                    builder.push(" AND ( ");
                    for (i, f) in fields.iter().enumerate() {
                        if i > 0 {
                            builder.push(" OR ");
                        }
                        let safe_f: String = f
                            .chars()
                            .map(|c| if c.is_ascii_alphanumeric() || c == '_' { c } else { '_' })
                            .collect();
                        builder.push(format!("(r.data->>'{safe_f}') ILIKE "));
                        builder.push_bind(pattern.clone());
                        builder.push(format!(" OR (r.data->'{safe_f}'->>'ko') ILIKE "));
                        builder.push_bind(pattern.clone());
                        builder.push(format!(" OR (r.data->'{safe_f}'->>'en') ILIKE "));
                        builder.push_bind(pattern.clone());
                    }
                    builder.push(" ) ");
                }
            }
            continue;
        }

        let safe_key: String = k
            .chars()
            .map(|c| if c.is_ascii_alphanumeric() || c == '_' { c } else { '_' })
            .collect();
        let safe_lower = safe_key.to_lowercase();
        let safe_upper = safe_key.to_uppercase();
        let op = q
            .search_params
            .get(&format!("search_op_{k}"))
            .or_else(|| q.search_params.get(&format!("op_{k}")))
            .map(|s| s.as_str())
            .unwrap_or("EQ");

        match op {
            "EQ" => {
                builder.push(" AND ( ");
                builder.push(format!("(r.data->>'{safe_key}') ILIKE "));
                builder.push_bind(val.to_string());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'ko') ILIKE "));
                builder.push_bind(val.to_string());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'en') ILIKE "));
                builder.push_bind(val.to_string());
                if safe_lower != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_lower}') ILIKE "));
                    builder.push_bind(val.to_string());
                }
                if safe_upper != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_upper}') ILIKE "));
                    builder.push_bind(val.to_string());
                }
                builder.push(" ) ");
            }
            "CONTAINS" => {
                let pattern = format!("%{}%", val);
                builder.push(" AND ( ");
                builder.push(format!("(r.data->>'{safe_key}') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'ko') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'en') ILIKE "));
                builder.push_bind(pattern.clone());
                if safe_lower != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_lower}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                if safe_upper != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_upper}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                builder.push(" ) ");
            }
            "STARTS_WITH" => {
                let pattern = format!("{}%", val);
                builder.push(" AND ( ");
                builder.push(format!("(r.data->>'{safe_key}') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'ko') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'en') ILIKE "));
                builder.push_bind(pattern.clone());
                if safe_lower != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_lower}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                if safe_upper != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_upper}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                builder.push(" ) ");
            }
            "ENDS_WITH" => {
                let pattern = format!("%{}", val);
                builder.push(" AND ( ");
                builder.push(format!("(r.data->>'{safe_key}') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'ko') ILIKE "));
                builder.push_bind(pattern.clone());
                builder.push(format!(" OR (r.data->'{safe_key}'->>'en') ILIKE "));
                builder.push_bind(pattern.clone());
                if safe_lower != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_lower}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                if safe_upper != safe_key {
                    builder.push(format!(" OR (r.data->>'{safe_upper}') ILIKE "));
                    builder.push_bind(pattern.clone());
                }
                builder.push(" ) ");
            }
            "IN" => {
                let items: Vec<String> = val
                    .split(',')
                    .map(|s| s.trim().to_string())
                    .filter(|s| !s.is_empty())
                    .collect();
                if !items.is_empty() {
                    builder.push(" AND ( ");
                    builder.push(format!("(r.data->>'{safe_key}') = ANY("));
                    builder.push_bind(items.clone());
                    builder.push(")");
                    if safe_lower != safe_key {
                        builder.push(format!(" OR (r.data->>'{safe_lower}') = ANY("));
                        builder.push_bind(items.clone());
                        builder.push(")");
                    }
                    if safe_upper != safe_key {
                        builder.push(format!(" OR (r.data->>'{safe_upper}') = ANY("));
                        builder.push_bind(items.clone());
                        builder.push(")");
                    }
                    builder.push(" ) ");
                }
            }
            "BETWEEN" => {
                let max_val_str = q
                    .search_params
                    .get(&format!("search_{k}_max"))
                    .map(|s| s.as_str())
                    .unwrap_or(val);
                if let (Ok(v_min), Ok(v_max)) = (val.parse::<f64>(), max_val_str.parse::<f64>()) {
                    builder.push(format!(
                        " AND ( \
                         (NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_key}', '') ~ '^-?[0-9]+(\\.[0-9]+)?$' \
                          AND CAST(NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_key}', '') AS NUMERIC) BETWEEN "
                    ));
                    builder.push_bind(v_min);
                    builder.push(" AND ");
                    builder.push_bind(v_max);
                    builder.push(format!(
                        ") OR (NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_lower}', '') ~ '^-?[0-9]+(\\.[0-9]+)?$' \
                          AND CAST(NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_lower}', '') AS NUMERIC) BETWEEN "
                    ));
                    builder.push_bind(v_min);
                    builder.push(" AND ");
                    builder.push_bind(v_max);
                    builder.push(") ) ");
                }
            }
            "GT" | "LT" | "GTE" | "LTE" => {
                let sql_op = match op {
                    "GT" => ">",
                    "LT" => "<",
                    "GTE" => ">=",
                    "LTE" => "<=",
                    _ => "=",
                };
                if let Ok(num) = val.parse::<f64>() {
                    builder.push(format!(
                        " AND ( \
                         (NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_key}', '') ~ '^-?[0-9]+(\\.[0-9]+)?$' \
                          AND CAST(NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_key}', '') AS NUMERIC) {sql_op} "
                    ));
                    builder.push_bind(num);
                    builder.push(format!(
                        ") OR (NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_lower}', '') ~ '^-?[0-9]+(\\.[0-9]+)?$' \
                          AND CAST(NULLIF(COALESCE(r.searchable_data, r.data)->>'{safe_lower}', '') AS NUMERIC) {sql_op} "
                    ));
                    builder.push_bind(num);
                    builder.push(") ) ");
                }
            }
            _ => {}
        }
    }
}

fn apply_order_by<'a>(
    builder: &mut QueryBuilder<'a, Postgres>,
    q: &'a DynamicRecordQuery,
) {
    let (field, is_desc) = if let Some(ref sf) = q.sort_field {
        let is_desc = q
            .sort_order
            .as_deref()
            .map(|o| o.eq_ignore_ascii_case("DESC"))
            .unwrap_or(false);
        (sf.as_str(), is_desc)
    } else if let Some(ref s) = q.sort {
        let segs: Vec<&str> = s.split(';').collect();
        let first = segs[0];
        let parts: Vec<&str> = first.split(',').collect();
        let field = parts[0].trim();
        let is_desc = parts
            .get(1)
            .map(|d| d.trim().eq_ignore_ascii_case("desc"))
            .unwrap_or(false);
        (field, is_desc)
    } else {
        ("created_at", true)
    };

    let dir = if is_desc { "DESC" } else { "ASC" };
    match field {
        "created_at" | "createdAt" => {
            builder.push(format!(" ORDER BY r.created_at {dir} "));
        }
        "updated_at" | "updatedAt" => {
            builder.push(format!(" ORDER BY r.updated_at {dir} "));
        }
        "status" => {
            builder.push(format!(" ORDER BY r.status {dir} "));
        }
        "id" => {
            builder.push(format!(" ORDER BY r.id {dir} "));
        }
        "version" => {
            builder.push(format!(" ORDER BY r.version {dir} "));
        }
        "source_system" | "sourceSystem" => {
            builder.push(format!(" ORDER BY r.source_system {dir} "));
        }
        "nodeName" | "sys_node_name" => {
            builder.push(format!(" ORDER BY n.name {dir} "));
        }
        custom => {
            let safe: String = custom
                .chars()
                .map(|c| if c.is_ascii_alphanumeric() || c == '_' { c } else { '_' })
                .collect();
            builder.push(format!(
                " ORDER BY (r.data->>'{safe}') {dir} NULLS LAST, r.created_at DESC "
            ));
        }
    }
}

