use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::matching::{
    MatchCandidate, MatchingRule, MergeRequest, MergeResult, SurvivorshipRule,
};
use crate::services::matching_service::MatchingService;
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

pub async fn get_domain_matching_rules(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<MatchingRule>>, AppError> {
    let rules = MatchingService::get_matching_rules(&state.db, domain_id).await?;
    Ok(Json(rules))
}

pub async fn get_feedback_summary(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    use sqlx::Row;
    let rows = sqlx::query(
        r#"
        SELECT matched_rule_id, status, count(*) as cnt
        FROM match_candidate
        WHERE domain_id = $1
        GROUP BY matched_rule_id, status
        "#,
    )
    .bind(domain_id)
    .fetch_all(&state.db)
    .await;

    match rows {
        Ok(records) => {
            let list: Vec<serde_json::Value> = records
                .iter()
                .map(|r| {
                    let rule_id: Option<Uuid> = r.try_get("matched_rule_id").ok();
                    let status: String = r.try_get("status").unwrap_or_default();
                    let count: i64 = r.try_get("cnt").unwrap_or(0);
                    serde_json::json!({
                        "ruleId": rule_id,
                        "status": status,
                        "count": count
                    })
                })
                .collect();
            Ok(Json(list))
        }
        Err(_) => Ok(Json(vec![])),
    }
}

pub async fn get_candidates(
    State(state): State<AppState>,
    Query(query): Query<CandidateQuery>,
    _auth: AuthUser,
) -> Result<Json<Vec<MatchCandidate>>, AppError> {
    let candidates =
        MatchingService::get_candidates(&state.db, query.domain_id, query.status.as_deref())
            .await?;
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

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateSurvivorshipRuleDto {
    pub field_key: Option<String>,
    pub strategy: String,
    pub priority: Option<i32>,
}

pub async fn update_survivorship_rules(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(rules): Json<Vec<UpdateSurvivorshipRuleDto>>,
) -> Result<Json<Vec<SurvivorshipRule>>, AppError> {
    let mut tx = state.db.begin().await?;

    sqlx::query("DELETE FROM survivorship_rule WHERE domain_id = $1")
        .bind(domain_id)
        .execute(&mut *tx)
        .await?;

    let mut inserted = Vec::new();
    let mut seen = std::collections::HashSet::new();
    let mut current_priority = 1;

    for r in rules {
        let fk = r.field_key.filter(|k| !k.trim().is_empty());
        let key_pair = (fk.clone(), r.strategy.clone());
        if seen.contains(&key_pair) {
            continue;
        }
        seen.insert(key_pair);

        let p = r.priority.unwrap_or(current_priority);
        let id = Uuid::new_v4();

        let row = sqlx::query_as::<_, SurvivorshipRule>(
            r#"
            INSERT INTO survivorship_rule (id, domain_id, field_key, priority, strategy)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *
            "#,
        )
        .bind(id)
        .bind(domain_id)
        .bind(&fk)
        .bind(p)
        .bind(&r.strategy)
        .fetch_one(&mut *tx)
        .await?;

        inserted.push(row);
        current_priority += 1;
    }

    tx.commit().await?;
    Ok(Json(inserted))
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
    pub rule_name: Option<String>,
    pub name: Option<String>,
    pub match_type: String,
    pub target_field_keys: Option<serde_json::Value>,
    pub fields: Option<serde_json::Value>,
    pub similarity_threshold: Option<f64>,
    pub threshold: Option<f64>,
    pub is_active: Option<bool>,
}

pub async fn create_matching_rule(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    _auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let id = Uuid::new_v4();
    let rule_name = payload.rule_name.or(payload.name).unwrap_or_default();
    let target_keys = payload
        .target_field_keys
        .or(payload.fields)
        .map(|v| match v {
            serde_json::Value::String(s) => {
                serde_json::from_str(&s).unwrap_or(serde_json::Value::String(s))
            }
            other => other,
        })
        .unwrap_or_else(|| serde_json::json!([]));
    let similarity_threshold = payload.similarity_threshold.or(payload.threshold);
    let is_active = payload.is_active.unwrap_or(true);

    sqlx::query(
        r#"
        INSERT INTO matching_rule (id, domain_id, rule_name, match_type, target_field_keys, similarity_threshold, is_active, created_at, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, $7, NOW(), NOW())
        "#,
    )
    .bind(id)
    .bind(domain_id)
    .bind(rule_name)
    .bind(payload.match_type)
    .bind(target_keys)
    .bind(similarity_threshold)
    .bind(is_active)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "id": id })))
}

pub async fn update_matching_rule(
    State(state): State<AppState>,
    Path((_domain_id, rule_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let rule_name = payload.rule_name.or(payload.name).unwrap_or_default();
    let target_keys = payload
        .target_field_keys
        .or(payload.fields)
        .map(|v| match v {
            serde_json::Value::String(s) => {
                serde_json::from_str(&s).unwrap_or(serde_json::Value::String(s))
            }
            other => other,
        })
        .unwrap_or_else(|| serde_json::json!([]));
    let similarity_threshold = payload.similarity_threshold.or(payload.threshold);
    let is_active = payload.is_active.unwrap_or(true);

    sqlx::query(
        r#"
        UPDATE matching_rule
        SET rule_name = $1, match_type = $2, target_field_keys = $3, similarity_threshold = $4, is_active = $5, updated_at = NOW()
        WHERE id = $6
        "#,
    )
    .bind(rule_name)
    .bind(payload.match_type)
    .bind(target_keys)
    .bind(similarity_threshold)
    .bind(is_active)
    .bind(rule_id)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "id": rule_id })))
}

pub async fn delete_matching_rule(
    State(state): State<AppState>,
    Path((_domain_id, rule_id)): Path<(Uuid, Uuid)>,
    _auth: AuthUser,
) -> Result<StatusCode, AppError> {
    sqlx::query("DELETE FROM matching_rule WHERE id = $1")
        .bind(rule_id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
