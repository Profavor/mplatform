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
use serde::Deserialize;
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
