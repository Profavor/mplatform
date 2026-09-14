use crate::models::system::*;
use sqlx::PgPool;

pub struct SystemRepository;

impl SystemRepository {
    pub async fn get_features(pool: &PgPool) -> Result<Vec<SystemFeature>, sqlx::Error> {
        let features = sqlx::query_as::<_, SystemFeature>(
            "SELECT * FROM system_features ORDER BY feature_no ASC"
        )
        .fetch_all(pool)
        .await?;

        Ok(features)
    }

    pub async fn get_config(pool: &PgPool) -> Result<Vec<SystemConfigItem>, sqlx::Error> {
        let config = sqlx::query_as::<_, SystemConfigItem>(
            "SELECT * FROM system_config ORDER BY config_key ASC"
        )
        .fetch_all(pool)
        .await?;

        Ok(config)
    }

    pub async fn get_error_logs(pool: &PgPool, page: i64, size: i64) -> Result<(Vec<ErrorLogItem>, i64), sqlx::Error> {
        let offset = page * size;
        let logs = sqlx::query_as::<_, ErrorLogItem>(
            "SELECT * FROM error_log ORDER BY logged_at DESC LIMIT $1 OFFSET $2"
        )
        .bind(size)
        .bind(offset)
        .fetch_all(pool)
        .await?;

        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM error_log")
            .fetch_one(pool)
            .await
            .unwrap_or((0,));

        Ok((logs, total.0))
    }
}
