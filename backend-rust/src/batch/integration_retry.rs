use sqlx::PgPool;
use uuid::Uuid;

pub struct IntegrationRetryJob;

impl IntegrationRetryJob {
    pub const LOCK_ID: i64 = 100_002;

    /// Scans failed integration logs and retries or moves to DEAD_LETTER queue.
    pub async fn run(pool: &PgPool) -> Result<u64, sqlx::Error> {
        // 1. Mark logs as DEAD_LETTER if retry_count >= 3
        let dl_query = r#"
            UPDATE integration_logs
            SET status = 'DEAD_LETTER',
                next_retry_at = NULL
            WHERE status IN ('FAIL', 'PENDING')
              AND retry_count >= 3
        "#;
        let _ = sqlx::query(dl_query).execute(pool).await;

        // 2. Perform real retry transmissions using OutboundService
        let retried = crate::services::outbound_service::OutboundService::retry_failed_logs(pool)
            .await
            .unwrap_or(0);

        if retried > 0 {
            tracing::info!(
                "🔄 [Integration Retry Batch] Retried {} failed integration logs",
                retried
            );
        }
        Ok(retried as u64)
    }
}
