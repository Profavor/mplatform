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
            WHERE status = 'FAIL'
              AND retry_count >= 3
        "#;
        let _ = sqlx::query(dl_query).execute(pool).await;

        // 2. Query overdue FAIL logs that can be retried
        #[derive(sqlx::FromRow)]
        struct OverdueLog {
            id: Uuid,
            retry_count: i32,
        }

        let rows = sqlx::query_as::<_, OverdueLog>(
            r#"
            SELECT id, retry_count
            FROM integration_logs
            WHERE status = 'FAIL'
              AND (next_retry_at IS NULL OR next_retry_at <= NOW())
              AND retry_count < 3
            LIMIT 20
            "#
        )
        .fetch_all(pool)
        .await?;

        let mut retried = 0;
        for row in rows {
            let next_count = row.retry_count + 1;
            let backoff_secs = 10 * (1 << next_count);
            let update_query = format!(
                r#"
                UPDATE integration_logs
                SET retry_count = $1,
                    next_retry_at = NOW() + INTERVAL '{} seconds'
                WHERE id = $2
                "#,
                backoff_secs
            );
            let _ = sqlx::query(&update_query)
                .bind(next_count)
                .bind(row.id)
                .execute(pool)
                .await;
            retried += 1;
        }

        if retried > 0 {
            tracing::info!("🔄 [Integration Retry Batch] Retried {} failed integration logs", retried);
        }
        Ok(retried)
    }
}
