use crate::error::AppError;
use crate::models::approval::{ApprovalDetailResponse, ApprovalRequest, CreateApprovalRequest};
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
        let req = ApprovalRepository::find_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Approval request not found: {id}")))?;

        // Check if actor is an active delegated approver during valid delegation period
        let step_assignee: Option<(Option<String>,)> = sqlx::query_as(
            "SELECT assignee_id FROM approval_step WHERE request_id = $1 AND status = 'PENDING' LIMIT 1"
        )
        .bind(id)
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

        let comment_param = if final_comment.is_empty() { None } else { Some(final_comment.as_str()) };
        ApprovalRepository::approve_current_step(pool, id, comment_param).await?;

        // If target is RECORD, apply changes to record
        if req.target_type == "RECORD" || req.target_type == "RECORD_UPDATE" {
            if let Some(mut record) = RecordRepository::find_by_id(pool, req.target_id).await? {
                if let (Some(r_obj), Some(c_obj)) = (record.data.as_mut().and_then(|d| d.as_object_mut()), req.changes.as_object()) {
                    for (k, v) in c_obj {
                        r_obj.insert(k.clone(), v.clone());
                    }
                    let _ = RecordRepository::insert_history(
                        pool,
                        record.id,
                        record.version + 1,
                        "APPROVAL_APPLY",
                        actor,
                        None,
                        record.data.clone(),
                        None,
                    )
                    .await;
                }
            }
        }

        Self::get_request_detail(pool, id).await
    }

    pub async fn reject(
        pool: &PgPool,
        id: Uuid,
        reason: Option<&str>,
        actor: &str,
    ) -> Result<ApprovalDetailResponse, AppError> {
        // Check if actor is an active delegated approver during valid delegation period
        let step_assignee: Option<(Option<String>,)> = sqlx::query_as(
            "SELECT assignee_id FROM approval_step WHERE request_id = $1 AND status = 'PENDING' LIMIT 1"
        )
        .bind(id)
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

        let reason_param = if final_reason.is_empty() { None } else { Some(final_reason.as_str()) };
        ApprovalRepository::reject_current_step(pool, id, reason_param).await?;
        Self::get_request_detail(pool, id).await
    }
}
