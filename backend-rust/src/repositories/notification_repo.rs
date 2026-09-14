use crate::error::AppError;
use crate::models::notification::Notification;
use sqlx::PgPool;
use uuid::Uuid;

pub struct NotificationRepository;

impl NotificationRepository {
    pub async fn find_by_user(pool: &PgPool, user_id: &str) -> Result<Vec<Notification>, AppError> {
        let notifs = sqlx::query_as::<_, Notification>(
            r#"
            SELECT id, user_id, type, title, message, link_url, is_read, created_at
            FROM notifications
            WHERE user_id = $1
            ORDER BY created_at DESC
            LIMIT 50
            "#,
        )
        .bind(user_id)
        .fetch_all(pool)
        .await?;

        Ok(notifs)
    }

    pub async fn count_unread(pool: &PgPool, user_id: &str) -> Result<i64, AppError> {
        let count_row: (i64,) = sqlx::query_as(
            r#"
            SELECT COUNT(*)
            FROM notifications
            WHERE user_id = $1 AND is_read = false
            "#,
        )
        .bind(user_id)
        .fetch_one(pool)
        .await?;

        Ok(count_row.0)
    }

    pub async fn mark_read(pool: &PgPool, id: Uuid, user_id: &str) -> Result<bool, AppError> {
        let result = sqlx::query(
            r#"
            UPDATE notifications
            SET is_read = true
            WHERE id = $1 AND user_id = $2
            "#,
        )
        .bind(id)
        .bind(user_id)
        .execute(pool)
        .await?;

        Ok(result.rows_affected() > 0)
    }
}
