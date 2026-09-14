use crate::error::AppError;
use crate::models::approval::{ApprovalRequest, ApprovalStep, CreateApprovalRequest};
use sqlx::PgPool;
use uuid::Uuid;

pub struct ApprovalRepository;

impl ApprovalRepository {
    pub async fn find_all(
        pool: &PgPool,
        status: Option<&str>,
        requester_id: Option<&str>,
    ) -> Result<Vec<ApprovalRequest>, AppError> {
        let requests = sqlx::query_as::<_, ApprovalRequest>(
            r#"
            SELECT id, target_id, target_type, requester_id, status,
                   reason, changes, current_step_order, node_id,
                   observer_ids, version, created_at, updated_at
            FROM approval_request
            WHERE ($1::text IS NULL OR status = $1)
              AND ($2::text IS NULL OR requester_id = $2)
            ORDER BY created_at DESC
            "#,
        )
        .bind(status)
        .bind(requester_id)
        .fetch_all(pool)
        .await?;

        Ok(requests)
    }

    pub async fn find_by_id(pool: &PgPool, id: Uuid) -> Result<Option<ApprovalRequest>, AppError> {
        let req = sqlx::query_as::<_, ApprovalRequest>(
            r#"
            SELECT id, target_id, target_type, requester_id, status,
                   reason, changes, current_step_order, node_id,
                   observer_ids, version, created_at, updated_at
            FROM approval_request
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(req)
    }

    pub async fn find_steps(
        pool: &PgPool,
        request_id: Uuid,
    ) -> Result<Vec<ApprovalStep>, AppError> {
        let steps = sqlx::query_as::<_, ApprovalStep>(
            r#"
            SELECT id, request_id, step_order, step_type, assignee_id,
                   assignee_role, status, comment, sla_hours, sla_due_at,
                   is_escalated, escalated_from_user_id, escalated_at,
                   version, created_at, updated_at
            FROM approval_step
            WHERE request_id = $1
            ORDER BY step_order ASC
            "#,
        )
        .bind(request_id)
        .fetch_all(pool)
        .await?;

        Ok(steps)
    }

    pub async fn create(
        pool: &PgPool,
        req: CreateApprovalRequest,
        requester: &str,
    ) -> Result<ApprovalRequest, AppError> {
        let mut tx = pool.begin().await?;
        let request_id = Uuid::new_v4();

        let approval = sqlx::query_as::<_, ApprovalRequest>(
            r#"
            INSERT INTO approval_request (
                id, target_id, target_type, requester_id, status,
                reason, changes, current_step_order, node_id, version,
                created_at, updated_at
            )
            VALUES ($1, $2, $3, $4, 'PENDING', $5, $6, 1, $7, 0, NOW(), NOW())
            RETURNING
                id, target_id, target_type, requester_id, status,
                reason, changes, current_step_order, node_id,
                observer_ids, version, created_at, updated_at
            "#,
        )
        .bind(request_id)
        .bind(req.target_id)
        .bind(req.target_type)
        .bind(requester)
        .bind(req.reason)
        .bind(req.changes)
        .bind(req.node_id)
        .fetch_one(&mut *tx)
        .await?;

        // Create initial approval step
        let step_id = Uuid::new_v4();
        sqlx::query(
            r#"
            INSERT INTO approval_step (
                id, request_id, step_order, step_type, assignee_id,
                status, is_escalated, version, created_at, updated_at
            )
            VALUES ($1, $2, 1, 'APPROVE', $3, 'PENDING', false, 0, NOW(), NOW())
            "#,
        )
        .bind(step_id)
        .bind(request_id)
        .bind(req.assignee_id)
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;
        Ok(approval)
    }

    pub async fn approve_current_step(
        pool: &PgPool,
        request_id: Uuid,
        comment: Option<&str>,
    ) -> Result<(), AppError> {
        let mut tx = pool.begin().await?;

        // 1. Mark step APPROVED
        sqlx::query(
            r#"
            UPDATE approval_step
            SET status = 'APPROVED',
                comment = $2,
                updated_at = NOW()
            WHERE request_id = $1 AND status = 'PENDING'
            "#,
        )
        .bind(request_id)
        .bind(comment)
        .execute(&mut *tx)
        .await?;

        // 2. Mark request APPROVED
        sqlx::query(
            r#"
            UPDATE approval_request
            SET status = 'APPROVED',
                updated_at = NOW()
            WHERE id = $1
            "#,
        )
        .bind(request_id)
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;
        Ok(())
    }

    pub async fn reject_current_step(
        pool: &PgPool,
        request_id: Uuid,
        reason: Option<&str>,
    ) -> Result<(), AppError> {
        let mut tx = pool.begin().await?;

        sqlx::query(
            r#"
            UPDATE approval_step
            SET status = 'REJECTED',
                comment = $2,
                updated_at = NOW()
            WHERE request_id = $1 AND status = 'PENDING'
            "#,
        )
        .bind(request_id)
        .bind(reason)
        .execute(&mut *tx)
        .await?;

        sqlx::query(
            r#"
            UPDATE approval_request
            SET status = 'REJECTED',
                updated_at = NOW()
            WHERE id = $1
            "#,
        )
        .bind(request_id)
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;
        Ok(())
    }
}
