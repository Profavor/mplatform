use sqlx::PgPool;

pub struct ChatCleanupJob;

impl ChatCleanupJob {
    pub const LOCK_ID: i64 = 100_004;

    /// Cleans up expired ephemeral chat messages.
    pub async fn run(pool: &PgPool) -> Result<u64, sqlx::Error> {
        let res = sqlx::query(
            r#"
            DELETE FROM chat_message
            WHERE is_ephemeral = true AND created_at < NOW() - INTERVAL '7 days'
            "#,
        )
        .execute(pool)
        .await?;

        let rows = res.rows_affected();
        if rows > 0 {
            tracing::info!(
                "🧹 [Chat Cleanup Batch] Purged {} expired ephemeral messages",
                rows
            );
        }
        Ok(rows)
    }
}
