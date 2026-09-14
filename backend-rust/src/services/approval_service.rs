use crate::error::AppError;
use crate::models::approval::{
    ApprovalDetailResponse, ApprovalRequest, ApprovalStep, CreateApprovalRequest,
};
use crate::repositories::approval_repo::ApprovalRepository;
use crate::repositories::record_repo::RecordRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct ApprovalService;

impl ApprovalService {
    pub async fn get_requests(
        pool: &PgPool,
        status: Option<&str>,
        requester: Option<&str>,
    ) -> Result<Vec<ApprovalRequest>, AppError> {
        ApprovalRepository::find_all(pool, status, requester).await
    }

    pub async fn get_request_detail(
        pool: &PgPool,
        id: Uuid,
    ) -> Result<ApprovalDetailResponse, AppError> {
        let request = ApprovalRepository::find_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Approval request not found: {id}")))?;

        let steps = ApprovalRepository::find_steps(pool, id).await?;

        Ok(ApprovalDetailResponse { request, steps })
    }

    pub async fn create_request(
        pool: &PgPool,
        req: CreateApprovalRequest,
        requester: &str,
    ) -> Result<ApprovalRequest, AppError> {
        ApprovalRepository::create(pool, req, requester).await
    }

    pub async fn approve(
        pool: &PgPool,
        id: Uuid,
        comment: Option<&str>,
        actor: &str,
    ) -> Result<ApprovalDetailResponse, AppError> {
        let (request_id, req) = match ApprovalRepository::find_by_id(pool, id).await? {
            Some(req) => (id, req),
            None => {
                let step: Option<ApprovalStep> =
                    sqlx::query_as("SELECT * FROM approval_step WHERE id = $1")
                        .bind(id)
                        .fetch_optional(pool)
                        .await?;
                if let Some(step) = step {
                    let req = ApprovalRepository::find_by_id(pool, step.request_id)
                        .await?
                        .ok_or_else(|| {
                            AppError::NotFound(format!(
                                "Approval request not found: {}",
                                step.request_id
                            ))
                        })?;
                    (step.request_id, req)
                } else {
                    return Err(AppError::NotFound(format!(
                        "Approval request or step not found: {id}"
                    )));
                }
            }
        };

        // Check if actor is an active delegated approver during valid delegation period
        let step_assignee: Option<(Option<String>,)> = sqlx::query_as(
            "SELECT assignee_id FROM approval_step WHERE request_id = $1 AND status = 'PENDING' LIMIT 1"
        )
        .bind(request_id)
        .fetch_optional(pool)
        .await
        .unwrap_or(None);

        let mut final_comment = comment.unwrap_or("").to_string();
        if let Some((Some(ref aid),)) = step_assignee {
            if aid != actor {
                let is_proxy: bool = sqlx::query_scalar(
                    r#"
                    SELECT EXISTS(
                        SELECT 1 FROM approval_delegations ad
                        LEFT JOIN users u ON ad.delegator_user_id = u.id
                        WHERE (ad.delegator_user_id = $1 OR u.username = $1)
                          AND (ad.delegatee_user_id = $2 OR ad.delegatee_user_id IN (SELECT id FROM users WHERE username = $2))
                          AND ad.is_active = true
                          AND ad.start_date <= NOW()
                          AND ad.end_date >= NOW()
                    )
                    "#
                )
                .bind(aid)
                .bind(actor)
                .fetch_one(pool)
                .await
                .unwrap_or(false);

                if is_proxy {
                    final_comment = if final_comment.is_empty() {
                        "[대결]".to_string()
                    } else {
                        format!("[대결] {}", final_comment)
                    };
                }
            }
        }

        let comment_param = if final_comment.is_empty() {
            None
        } else {
            Some(final_comment.as_str())
        };
        ApprovalRepository::approve_current_step(pool, request_id, comment_param).await?;

        // If target is RECORD, apply changes to record
        if req.target_type == "RECORD" || req.target_type == "RECORD_UPDATE" {
            if let Some(mut record) = RecordRepository::find_by_id(pool, req.target_id).await? {
                if let (Some(r_obj), Some(c_obj)) = (
                    record.data.as_mut().and_then(|d| d.as_object_mut()),
                    req.changes.as_object(),
                ) {
                    for (k, v) in c_obj {
                        r_obj.insert(k.clone(), v.clone());
                    }
                    let new_version = record.version + 1;
                    let final_data = record.data.clone();

                    let _ = sqlx::query(
                        r#"
                        UPDATE record
                        SET data = $1, searchable_data = $1, version = $2, updated_at = NOW()
                        WHERE id = $3
                        "#,
                    )
                    .bind(&final_data)
                    .bind(new_version)
                    .bind(record.id)
                    .execute(pool)
                    .await;

                    let _ = RecordRepository::insert_history(
                        pool,
                        record.id,
                        new_version,
                        "APPROVAL_APPLY",
                        actor,
                        None,
                        final_data.clone(),
                        None,
                        Some(req.id),
                    )
                    .await;

                    if let Some(ref d) = final_data {
                        let pool_clone = pool.clone();
                        let rec_id = record.id;
                        let node_id = record.node_id;
                        let d_clone = d.clone();
                        tokio::spawn(async move {
                            let _ = crate::services::outbound_service::OutboundService::dispatch_record_change(
                                &pool_clone,
                                rec_id,
                                node_id,
                                "UPDATE",
                                &d_clone,
                            )
                            .await;
                        });
                    }
                }
            }
        } else if req.target_type == "RECORD_CREATE" {
            let record_id = req.target_id;
            let node_id = req.node_id.unwrap_or(req.target_id);
            let final_data = req.changes.clone();

            let _ = sqlx::query(
                r#"
                INSERT INTO record (
                    id, node_id, data, searchable_data, status, version, created_at, updated_at
                )
                VALUES ($1, $2, $3, $3, 'ACTIVE', 1, NOW(), NOW())
                ON CONFLICT (id) DO UPDATE
                SET data = $3, searchable_data = $3, status = 'ACTIVE', updated_at = NOW()
                "#,
            )
            .bind(record_id)
            .bind(node_id)
            .bind(&final_data)
            .execute(pool)
            .await;

            let _ = RecordRepository::insert_history(
                pool,
                record_id,
                1,
                "CREATE",
                actor,
                None,
                Some(final_data.clone()),
                None,
                Some(req.id),
            )
            .await;

            let pool_clone = pool.clone();
            tokio::spawn(async move {
                let _ = crate::services::outbound_service::OutboundService::dispatch_record_change(
                    &pool_clone,
                    record_id,
                    node_id,
                    "CREATE",
                    &final_data,
                )
                .await;
            });
        }

        Self::get_request_detail(pool, request_id).await
    }

    pub async fn reject(
        pool: &PgPool,
        id: Uuid,
        reason: Option<&str>,
        actor: &str,
    ) -> Result<ApprovalDetailResponse, AppError> {
        let (request_id, _req) = match ApprovalRepository::find_by_id(pool, id).await? {
            Some(req) => (id, req),
            None => {
                let step: Option<ApprovalStep> =
                    sqlx::query_as("SELECT * FROM approval_step WHERE id = $1")
                        .bind(id)
                        .fetch_optional(pool)
                        .await?;
                if let Some(step) = step {
                    let req = ApprovalRepository::find_by_id(pool, step.request_id)
                        .await?
                        .ok_or_else(|| {
                            AppError::NotFound(format!(
                                "Approval request not found: {}",
                                step.request_id
                            ))
                        })?;
                    (step.request_id, req)
                } else {
                    return Err(AppError::NotFound(format!(
                        "Approval request or step not found: {id}"
                    )));
                }
            }
        };

        // Check if actor is an active delegated approver during valid delegation period
        let step_assignee: Option<(Option<String>,)> = sqlx::query_as(
            "SELECT assignee_id FROM approval_step WHERE request_id = $1 AND status = 'PENDING' LIMIT 1"
        )
        .bind(request_id)
        .fetch_optional(pool)
        .await
        .unwrap_or(None);

        let mut final_reason = reason.unwrap_or("").to_string();
        if let Some((Some(ref aid),)) = step_assignee {
            if aid != actor {
                let is_proxy: bool = sqlx::query_scalar(
                    r#"
                    SELECT EXISTS(
                        SELECT 1 FROM approval_delegations ad
                        LEFT JOIN users u ON ad.delegator_user_id = u.id
                        WHERE (ad.delegator_user_id = $1 OR u.username = $1)
                          AND (ad.delegatee_user_id = $2 OR ad.delegatee_user_id IN (SELECT id FROM users WHERE username = $2))
                          AND ad.is_active = true
                          AND ad.start_date <= NOW()
                          AND ad.end_date >= NOW()
                    )
                    "#
                )
                .bind(aid)
                .bind(actor)
                .fetch_one(pool)
                .await
                .unwrap_or(false);

                if is_proxy {
                    final_reason = if final_reason.is_empty() {
                        "[대결]".to_string()
                    } else {
                        format!("[대결] {}", final_reason)
                    };
                }
            }
        }

        let reason_param = if final_reason.is_empty() {
            None
        } else {
            Some(final_reason.as_str())
        };
        ApprovalRepository::reject_current_step(pool, request_id, reason_param).await?;
        Self::get_request_detail(pool, request_id).await
    }
}
