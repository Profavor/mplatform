use crate::error::AppError;
use crate::middleware::auth::AuthUser;
use crate::models::matching::{
    BatchCandidateActionRequest, MatchCandidate, MatchCandidateDto, MatchingRule, MergeRequest,
    MergeResult, SurvivorshipRule,
};
use crate::models::record::PageResponse;
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

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CandidatePageQuery {
    pub page: Option<i64>,
    pub size: Option<i64>,
    pub status: Option<String>,
}

async fn validate_domain_access(
    pool: &sqlx::PgPool,
    domain_id: Uuid,
    auth: &AuthUser,
) -> Result<(), AppError> {
    if let Some(org_id) = auth.organization_id {
        let exists: bool = sqlx::query_scalar(
            "SELECT EXISTS(SELECT 1 FROM domain WHERE id = $1 AND organization_id = $2)",
        )
        .bind(domain_id)
        .bind(org_id)
        .fetch_one(pool)
        .await
        .unwrap_or(false);
        if !exists {
            return Err(AppError::Forbidden("접근 권한이 없는 도메인입니다".to_string()));
        }
    }
    Ok(())
}

pub async fn get_matching_rules(
    State(state): State<AppState>,
    Query(query): Query<MatchingRuleQuery>,
    auth: AuthUser,
) -> Result<Json<Vec<MatchingRule>>, AppError> {
    validate_domain_access(&state.db, query.domain_id, &auth).await?;
    let rules = MatchingService::get_matching_rules(&state.db, query.domain_id).await?;
    Ok(Json(rules))
}

pub async fn get_domain_matching_rules(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<Vec<MatchingRule>>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
    let rules = MatchingService::get_matching_rules(&state.db, domain_id).await?;
    Ok(Json(rules))
}

pub async fn get_feedback_summary(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<Vec<serde_json::Value>>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
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

pub async fn get_domain_candidates(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    Query(query): Query<CandidatePageQuery>,
    auth: AuthUser,
) -> Result<Json<PageResponse<MatchCandidateDto>>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
    let page = query.page.unwrap_or(0).max(0);
    let size = query.size.unwrap_or(20).max(1);
    let offset = page * size;
    let status_filter = query.status.as_deref().filter(|s| !s.is_empty() && *s != "ALL");

    let count_row: (i64,) = sqlx::query_as(
        r#"
        SELECT COUNT(*)
        FROM match_candidate mc
        WHERE mc.domain_id = $1
          AND ($2::text IS NULL OR mc.status = $2)
        "#,
    )
    .bind(domain_id)
    .bind(status_filter)
    .fetch_one(&state.db)
    .await?;

    let rows = sqlx::query_as::<_, (
        Uuid, Option<Uuid>, Uuid, Uuid, Option<Uuid>,
        f64, String, String, String, Option<String>,
        Option<String>, Option<chrono::NaiveDateTime>, Option<chrono::NaiveDateTime>,
        Option<serde_json::Value>
    )>(
        r#"
        SELECT 
            mc.id, mc.domain_id, mc.node_id, mc.existing_record_id, mc.matched_rule_id,
            mc.score, mc.source, mc.status, mc.incoming_data_json, mc.matched_field_details,
            mc.reviewed_by, mc.reviewed_at, mc.created_at,
            r.data AS existing_record_data
        FROM match_candidate mc
        LEFT JOIN record r ON mc.existing_record_id = r.id
        WHERE mc.domain_id = $1
          AND ($2::text IS NULL OR mc.status = $2)
        ORDER BY mc.score DESC, mc.created_at DESC
        LIMIT $3 OFFSET $4
        "#,
    )
    .bind(domain_id)
    .bind(status_filter)
    .bind(size)
    .bind(offset)
    .fetch_all(&state.db)
    .await?;

    let dtos: Vec<MatchCandidateDto> = rows.into_iter().map(|(
        id, domain_id, node_id, existing_record_id, matched_rule_id,
        score, source, status, incoming_data_json, matched_field_details,
        reviewed_by, reviewed_at, created_at, existing_record_data
    )| {
        let incoming_data = serde_json::from_str::<serde_json::Value>(&incoming_data_json)
            .unwrap_or_else(|_| serde_json::json!({ "raw": incoming_data_json }));

        let mut rec_val = existing_record_data.unwrap_or_else(|| serde_json::json!({}));
        if let Some(obj) = rec_val.as_object_mut() {
            obj.insert("id".to_string(), serde_json::json!(existing_record_id));
        } else {
            rec_val = serde_json::json!({ "id": existing_record_id });
        }

        MatchCandidateDto {
            id,
            domain_id,
            node_id,
            existing_record_id,
            matched_rule_id,
            score,
            source,
            status,
            incoming_data_json,
            incoming_data,
            existing_record: rec_val,
            matched_field_details,
            reviewed_by,
            reviewed_at,
            created_at,
        }
    }).collect();

    Ok(Json(PageResponse::new(dtos, count_row.0, page, size)))
}

pub async fn reject_candidate(
    State(state): State<AppState>,
    Path(candidate_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query(
        r#"
        UPDATE match_candidate
        SET status = 'REJECTED',
            reviewed_by = $2,
            reviewed_at = NOW()
        WHERE id = $1
        "#,
    )
    .bind(candidate_id)
    .bind(&auth.claims.sub)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "id": candidate_id, "status": "REJECTED" })))
}

pub async fn confirm_candidate(
    State(state): State<AppState>,
    Path(candidate_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<serde_json::Value>, AppError> {
    sqlx::query(
        r#"
        UPDATE match_candidate
        SET status = 'CONFIRMED_MERGE',
            reviewed_by = $2,
            reviewed_at = NOW()
        WHERE id = $1
        "#,
    )
    .bind(candidate_id)
    .bind(&auth.claims.sub)
    .execute(&state.db)
    .await?;

    Ok(Json(serde_json::json!({ "id": candidate_id, "status": "CONFIRMED_MERGE" })))
}

pub async fn batch_reject_candidates(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<BatchCandidateActionRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let count = sqlx::query(
        r#"
        UPDATE match_candidate
        SET status = 'REJECTED',
            reviewed_by = $2,
            reviewed_at = NOW()
        WHERE id = ANY($1)
        "#,
    )
    .bind(&payload.ids)
    .bind(&auth.claims.sub)
    .execute(&state.db)
    .await?
    .rows_affected();

    Ok(Json(serde_json::json!({ "count": count, "status": "REJECTED" })))
}

pub async fn batch_confirm_candidates(
    State(state): State<AppState>,
    auth: AuthUser,
    Json(payload): Json<BatchCandidateActionRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    let count = sqlx::query(
        r#"
        UPDATE match_candidate
        SET status = 'CONFIRMED_MERGE',
            reviewed_by = $2,
            reviewed_at = NOW()
        WHERE id = ANY($1)
        "#,
    )
    .bind(&payload.ids)
    .bind(&auth.claims.sub)
    .execute(&state.db)
    .await?
    .rows_affected();

    Ok(Json(serde_json::json!({ "count": count, "status": "CONFIRMED_MERGE" })))
}


pub async fn get_survivorship_rules(
    State(state): State<AppState>,
    Path(domain_id): Path<Uuid>,
    auth: AuthUser,
) -> Result<Json<Vec<SurvivorshipRule>>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
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
    auth: AuthUser,
    Json(rules): Json<Vec<UpdateSurvivorshipRuleDto>>,
) -> Result<Json<Vec<SurvivorshipRule>>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
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
    auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
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
    Path((domain_id, rule_id)): Path<(Uuid, Uuid)>,
    auth: AuthUser,
    Json(payload): Json<MatchingRuleRequest>,
) -> Result<Json<serde_json::Value>, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
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
    Path((domain_id, rule_id)): Path<(Uuid, Uuid)>,
    auth: AuthUser,
) -> Result<StatusCode, AppError> {
    validate_domain_access(&state.db, domain_id, &auth).await?;
    sqlx::query("DELETE FROM matching_rule WHERE id = $1")
        .bind(rule_id)
        .execute(&state.db)
        .await?;
    Ok(axum::http::StatusCode::NO_CONTENT)
}
