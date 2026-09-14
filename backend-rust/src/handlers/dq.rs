use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::dq::{
    BatchValidateRequest, BatchValidateResponse, CreateDqRuleRequest, DqRule, DqScanResult,
    DqViolation,
};
use crate::services::dq_service::DqService;
use crate::state::AppState;
use axum::extract::Path;
use axum::extract::Query;
use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DqRuleQuery {
    pub domain_id: Uuid,
}

pub async fn get_rules(
    State(state): State<AppState>,
    Query(query): Query<DqRuleQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<DqRule>>, AppError> {
    let rules = DqService::get_rules(&state.db, query.domain_id).await?;
    Ok(Json(rules))
}

pub async fn create_rule(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<CreateDqRuleRequest>,
) -> Result<Json<DqRule>, AppError> {
    let rule = DqService::create_rule(&state.db, req).await?;
    Ok(Json(rule))
}

pub async fn scan_domain(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<DqScanResult>, AppError> {
    let result = DqService::scan_domain(&state.db, domain_id).await?;
    Ok(Json(result))
}

pub async fn get_record_violations(
    State(state): State<AppState>,
    Path(record_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<DqViolation>>, AppError> {
    let violations = DqService::get_violations_for_record(&state.db, record_id).await?;
    Ok(Json(violations))
}

pub async fn batch_validate(
    State(state): State<AppState>,
    _auth: AuthUser,
    Json(req): Json<BatchValidateRequest>,
) -> Result<Json<BatchValidateResponse>, AppError> {
    let result = DqService::batch_validate(&state.db, req).await?;
    Ok(Json(result))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateDqRuleRequest {
    pub name: String,
    pub rule_type: String,
    pub expression: String,
    pub severity: String,
    pub is_active: bool,
}

pub async fn update_rule(
    State(state): State<AppState>,
    Path(rule_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<UpdateDqRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE dq_rule
        SET name = $1, rule_type = $2, expression = $3, severity = $4, is_active = $5, updated_at = NOW()
        WHERE id = $6
        "#,
    )
    .bind(payload.name)
    .bind(payload.rule_type)
    .bind(payload.expression)
    .bind(payload.severity)
    .bind(payload.is_active)
    .bind(rule_id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_rule(
    State(state): State<AppState>,
    Path(rule_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM dq_rule WHERE id = $1")
        .bind(rule_id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}

// -------------------------------------------------------------
// Domain DQ Dashboard Endpoints
// -------------------------------------------------------------

pub async fn get_domain_dq_score(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let total_records: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*)
        FROM record r
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        "#
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    let latest_snapshot: Option<(f64, i64, i64)> = sqlx::query_as(
        "SELECT score, total_records, total_violations FROM dq_score_snapshot WHERE domain_id = $1 ORDER BY recorded_at DESC LIMIT 1"
    )
    .bind(domain_id)
    .fetch_optional(&state.db)
    .await?;

    let severity_counts: Vec<(String, i64)> = sqlx::query_as(
        r#"
        SELECT v.severity, COUNT(*)
        FROM dq_violation v
        JOIN record r ON v.record_id = r.id
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        GROUP BY v.severity
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let field_counts: Vec<(String, i64)> = sqlx::query_as(
        r#"
        SELECT v.field_key, COUNT(*)
        FROM dq_violation v
        JOIN record r ON v.record_id = r.id
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        GROUP BY v.field_key
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let mut violations_by_severity = serde_json::Map::new();
    let mut total_violations: i64 = 0;
    for (sev, cnt) in severity_counts {
        total_violations += cnt;
        violations_by_severity.insert(sev, serde_json::json!(cnt));
    }

    let mut violations_by_field = serde_json::Map::new();
    for (fld, cnt) in field_counts {
        violations_by_field.insert(fld, serde_json::json!(cnt));
    }

    let (score, final_total_records, final_total_violations) = if let Some((snap_score, snap_records, snap_violations)) = latest_snapshot {
        (snap_score, snap_records.max(total_records.0), total_violations.max(snap_violations))
    } else {
        let s = if total_records.0 == 0 {
            100.0
        } else {
            let ratio = (total_violations as f64) / (total_records.0 as f64);
            (100.0 - ratio * 100.0).clamp(0.0, 100.0)
        };
        (s, total_records.0, total_violations)
    };

    Ok(Json(serde_json::json!({
        "score": (score * 10.0).round() / 10.0,
        "totalRecords": final_total_records,
        "totalViolations": final_total_violations,
        "violationsBySeverity": violations_by_severity,
        "violationsByField": violations_by_field
    })))
}

pub async fn get_domain_dq_rules_count(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    let count: (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM dq_rule WHERE domain_id = $1 AND is_active = true"
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "count": count.0 })))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DqViolationsQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub severity: Option<String>,
    pub field_key: Option<String>,
}

#[derive(Debug, Serialize, sqlx::FromRow)]
#[serde(rename_all = "camelCase")]
pub struct DqViolationDetailItem {
    pub id: Uuid,
    pub record_id: Uuid,
    pub record_identifier: Option<String>,
    pub node_name: Option<serde_json::Value>,
    pub field_key: String,
    pub severity: String,
    pub rule_name: String,
    pub message: serde_json::Value,
    pub actual_value: Option<String>,
    pub checked_at: Option<chrono::NaiveDateTime>,
}

pub async fn get_domain_dq_violations(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(params): Query<DqViolationsQuery>,
    _auth: AuthUser,
) -> Result<Json<crate::models::record::PageResponse<DqViolationDetailItem>>, AppError> {
    let page = params.page.unwrap_or(0);
    let size = params.size.unwrap_or(10);
    let offset = page * size;

    let mut count_sql = "SELECT COUNT(*) FROM dq_violation v JOIN record r ON v.record_id = r.id JOIN classification_node cn ON r.node_id = cn.id WHERE cn.domain_id = $1".to_string();
    let mut data_sql = r#"
        SELECT 
            v.id,
            v.record_id,
            COALESCE(
                r.data->>'PRODUCT_NAME',
                r.data->>'BILL_NAME',
                r.data->>'name',
                r.data->>'NAME',
                r.data->>'title',
                r.data->>'TITLE',
                r.data->>'BILL_NO',
                r.data->>'record_identifier',
                r.id::text
            ) as record_identifier,
            cn.name as node_name,
            v.field_key,
            v.severity,
            COALESCE(dr.rule_type, 'RULE') as rule_name,
            v.message,
            v.actual_value,
            v.checked_at
        FROM dq_violation v
        JOIN record r ON v.record_id = r.id
        JOIN classification_node cn ON r.node_id = cn.id
        LEFT JOIN dq_rule dr ON v.dq_rule_id = dr.id
        WHERE cn.domain_id = $1
    "#.to_string();

    let mut bind_idx = 2;
    let mut severity_filter = None;
    let mut field_key_filter = None;

    if let Some(ref sev) = params.severity {
        if !sev.trim().is_empty() {
            count_sql.push_str(&format!(" AND v.severity = ${bind_idx}"));
            data_sql.push_str(&format!(" AND v.severity = ${bind_idx}"));
            bind_idx += 1;
            severity_filter = Some(sev.trim().to_string());
        }
    }

    if let Some(ref fk) = params.field_key {
        if !fk.trim().is_empty() {
            count_sql.push_str(&format!(" AND v.field_key = ${bind_idx}"));
            data_sql.push_str(&format!(" AND v.field_key = ${bind_idx}"));
            bind_idx += 1;
            field_key_filter = Some(fk.trim().to_string());
        }
    }

    data_sql.push_str(&format!(" ORDER BY v.checked_at DESC LIMIT ${bind_idx} OFFSET ${}", bind_idx + 1));

    let mut count_query = sqlx::query_as::<_, (i64,)>(&count_sql).bind(domain_id);
    if let Some(ref s) = severity_filter {
        count_query = count_query.bind(s);
    }
    if let Some(ref f) = field_key_filter {
        count_query = count_query.bind(f);
    }
    let total: (i64,) = count_query.fetch_one(&state.db).await?;

    let mut data_query = sqlx::query_as::<_, DqViolationDetailItem>(&data_sql).bind(domain_id);
    if let Some(ref s) = severity_filter {
        data_query = data_query.bind(s);
    }
    if let Some(ref f) = field_key_filter {
        data_query = data_query.bind(f);
    }
    data_query = data_query.bind(size).bind(offset);

    let items = data_query.fetch_all(&state.db).await?;

    Ok(Json(crate::models::record::PageResponse::new(items, total.0, page, size)))
}

#[derive(Debug, Deserialize)]
pub struct DqTrendQuery {
    pub from: Option<String>,
    pub to: Option<String>,
}

#[derive(Debug, Serialize, sqlx::FromRow)]
#[serde(rename_all = "camelCase")]
pub struct DqScoreSnapshotItem {
    pub id: Uuid,
    pub domain_id: Uuid,
    pub score: f64,
    pub total_records: i64,
    pub total_violations: i64,
    pub recorded_at: chrono::NaiveDateTime,
    pub scan_type: String,
}

pub async fn get_domain_dq_score_snapshots(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(_query): Query<DqTrendQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<DqScoreSnapshotItem>>, AppError> {
    let rows = sqlx::query_as::<_, DqScoreSnapshotItem>(
        r#"
        SELECT id, domain_id, score, total_records, total_violations, recorded_at, scan_type
        FROM dq_score_snapshot
        WHERE domain_id = $1
        ORDER BY recorded_at ASC
        LIMIT 90
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    Ok(Json(rows))
}

pub async fn trigger_domain_dq_scan(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    // 1. Run domain scan
    let _ = DqService::scan_domain(&state.db, domain_id).await?;

    // 2. Compute current score & counts
    let total_records: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*)
        FROM record r
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        "#
    )
    .bind(domain_id)
    .fetch_one(&state.db)
    .await?;

    let severity_counts: Vec<(String, i64)> = sqlx::query_as(
        r#"
        SELECT v.severity, COUNT(*)
        FROM dq_violation v
        JOIN record r ON v.record_id = r.id
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        GROUP BY v.severity
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let field_counts: Vec<(String, i64)> = sqlx::query_as(
        r#"
        SELECT v.field_key, COUNT(*)
        FROM dq_violation v
        JOIN record r ON v.record_id = r.id
        JOIN classification_node cn ON r.node_id = cn.id
        WHERE cn.domain_id = $1
        GROUP BY v.field_key
        "#
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await?;

    let mut violations_by_severity = serde_json::Map::new();
    let mut total_violations: i64 = 0;
    for (sev, cnt) in severity_counts {
        total_violations += cnt;
        violations_by_severity.insert(sev, serde_json::json!(cnt));
    }

    let mut violations_by_field = serde_json::Map::new();
    for (fld, cnt) in field_counts {
        violations_by_field.insert(fld, serde_json::json!(cnt));
    }

    let score = if total_records.0 == 0 {
        100.0
    } else {
        let ratio = (total_violations as f64) / (total_records.0 as f64);
        (100.0 - ratio * 100.0).clamp(0.0, 100.0)
    };

    let rounded_score = (score * 10.0).round() / 10.0;

    // 3. Insert snapshot
    let _ = sqlx::query(
        r#"
        INSERT INTO dq_score_snapshot (
            id, domain_id, score, total_records, total_violations, recorded_at, scan_type
        ) VALUES ($1, $2, $3, $4, $5, NOW(), 'MANUAL')
        "#
    )
    .bind(Uuid::new_v4())
    .bind(domain_id)
    .bind(rounded_score)
    .bind(total_records.0)
    .bind(total_violations)
    .execute(&state.db)
    .await;

    Ok(Json(serde_json::json!({
        "score": rounded_score,
        "totalRecords": total_records.0,
        "totalViolations": total_violations,
        "violationsBySeverity": violations_by_severity,
        "violationsByField": violations_by_field
    })))
}
