use sqlx::PgPool;

pub struct AdvisoryLock;

impl AdvisoryLock {
    /// Try to acquire an advisory lock by 64-bit integer ID.
    /// Returns true if the lock was acquired, false if held by another connection.
    pub async fn try_acquire(pool: &PgPool, lock_id: i64) -> bool {
        let res: Result<Option<bool>, sqlx::Error> = sqlx::query_scalar("SELECT pg_try_advisory_lock($1)")
            .bind(lock_id)
            .fetch_optional(pool)
            .await;
        res.unwrap_or(None).unwrap_or(false)
    }

    /// Releases the advisory lock.
    pub async fn release(pool: &PgPool, lock_id: i64) {
        let _ = sqlx::query("SELECT pg_advisory_unlock($1)")
            .bind(lock_id)
            .execute(pool)
            .await;
    }
}
