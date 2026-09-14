use crate::error::AppError;
use crate::models::notification::NotificationResponse;
use crate::repositories::notification_repo::NotificationRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct NotificationService;

impl NotificationService {
    pub async fn get_my_notifications(
        pool: &PgPool,
        user_id: &str,
    ) -> Result<Vec<NotificationResponse>, AppError> {
        let notifs = NotificationRepository::find_by_user(pool, user_id).await?;
        Ok(notifs.into_iter().map(NotificationResponse::from).collect())
    }

    pub async fn get_unread_count(pool: &PgPool, user_id: &str) -> Result<i64, AppError> {
        NotificationRepository::count_unread(pool, user_id).await
    }

    pub async fn mark_as_read(pool: &PgPool, id: Uuid, user_id: &str) -> Result<bool, AppError> {
        NotificationRepository::mark_read(pool, id, user_id).await
    }
}
