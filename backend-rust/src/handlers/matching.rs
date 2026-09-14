use axum::extract::State;
use axum::extract::Path;
use axum::http::StatusCode;
use axum::extract::Query;
use axum::Json;
use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::matching::{MatchCandidate, MatchingRule, MergeRequest, MergeResult, SurvivorshipRule};
use crate::services::matching_service::MatchingService;
use crate::state::AppState;
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MatchingRuleQuery {
    pub domain_id: Uuid,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CandidateQuery {
    pub domain_id: Option<Uuid>,
    pub status: Option<String>,
}

pub async fn get_matching_rules(
    State(state): State<AppState>,
    Query(query): Query<MatchingRuleQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<MatchingRule>>, AppError> {
    let rules = MatchingService::get_matching_rules(&state.db, query.domain_id).await?;
    Ok(Json(rules))
}

pub async fn get_candidates(
    State(state): State<AppState>,
    Query(query): Query<CandidateQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<MatchCandidate>>, AppError> {
    let candidates = MatchingService::get_candidates(&state.db, query.domain_id, query.status.as_deref()).await?;
    Ok(Json(candidates))
}

pub async fn get_survivorship_rules(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<SurvivorshipRule>>, AppError> {
    let rules = MatchingService::get_survivorship_rules(&state.db, domain_id).await?;
    Ok(Json(rules))
}

pub async fn merge_records(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(req): Json<MergeRequest>,
) -> Result<Json<MergeResult>, AppError> {
    let result = MatchingService::execute_merge(&state.db, req, &auth.claims.sub).await?;
    Ok(Json(result))
}

pub async fn unmerge_record(
    State(state): State<AppState>,
    Path(record_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    MatchingService::execute_unmerge(&state.db, record_id, &auth.claims.sub).await?;
    Ok(Json(serde_json::json!({
        "status": "UNMERGED",
        "recordId": record_id
    })))
}


#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MatchingRuleRequest {
    pub name: String,
    pub match_type: String,
    pub fields: serde_json::Value,
    pub threshold: Option<f64>,
    pub is_active: bool,
}

pub async fn create_matching_rule(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let row = sqlx::query(
        r#"
        INSERT INTO matching_rule (id, domain_id, name, match_type, fields, threshold, is_active, created_at, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, $7, NOW(), NOW())
        "#,
    )
    .bind(id)
    .bind(domain_id)
    .bind(payload.name)
    .bind(payload.match_type)
    .bind(payload.fields)
    .bind(payload.threshold)
    .bind(payload.is_active)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn update_matching_rule(
    State(state): State<AppState>,
    Path((_domain_id, rule_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let row = sqlx::query(
        r#"
        UPDATE matching_rule
        SET name = $1, match_type = $2, fields = $3, threshold = $4, is_active = $5, updated_at = NOW()
        WHERE id = $6
        "#,
    )
    .bind(payload.name)
    .bind(payload.match_type)
    .bind(payload.fields)
    .bind(payload.threshold)
    .bind(payload.is_active)
    .bind(rule_id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({})))
}

pub async fn delete_matching_rule(
    State(state): State<AppState>,
    Path((_domain_id, rule_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM matching_rule WHERE id = $1").bind(rule_id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}



