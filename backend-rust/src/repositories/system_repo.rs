use crate::models::system::*;
use sqlx::PgPool;
use uuid::Uuid;

pub struct SystemRepository;

impl SystemRepository {
    pub async fn get_features(pool: &PgPool) -> Result<Vec<SystemFeature>, sqlx::Error> {
        let features = sqlx::query_as::<_, SystemFeature>(
            "SELECT * FROM system_features ORDER BY feature_no ASC",
        )
        .fetch_all(pool)
        .await?;

        Ok(features)
    }

    pub async fn get_config(pool: &PgPool) -> Result<Vec<SystemConfigItem>, sqlx::Error> {
        let config = sqlx::query_as::<_, SystemConfigItem>(
            "SELECT * FROM system_config ORDER BY config_key ASC",
        )
        .fetch_all(pool)
        .await?;

        Ok(config)
    }

    pub async fn get_error_logs(
        pool: &PgPool,
        org_id: Option<Uuid>,
        page: i64,
        size: i64,
    ) -> Result<(Vec<ErrorLogItem>, i64), sqlx::Error> {
        let offset = page * size;

        if let Some(oid) = org_id {
            let logs = sqlx::query_as::<_, ErrorLogItem>(
                r#"
                SELECT * FROM error_log
                WHERE (
                    user_id IN (SELECT id::text FROM users WHERE organization_id = $1)
                    OR user_id IN (SELECT username FROM users WHERE organization_id = $1)
                )
                ORDER BY logged_at DESC
                LIMIT $2 OFFSET $3
                "#,
            )
            .bind(oid)
            .bind(size)
            .bind(offset)
            .fetch_all(pool)
            .await?;

            let total: (i64,) = sqlx::query_as(
                r#"
                SELECT COUNT(*) FROM error_log
                WHERE (
                    user_id IN (SELECT id::text FROM users WHERE organization_id = $1)
                    OR user_id IN (SELECT username FROM users WHERE organization_id = $1)
                )
                "#,
            )
            .bind(oid)
            .fetch_one(pool)
            .await
            .unwrap_or((0,));

            Ok((logs, total.0))
        } else {
            let logs = sqlx::query_as::<_, ErrorLogItem>(
                "SELECT * FROM error_log ORDER BY logged_at DESC LIMIT $1 OFFSET $2",
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
}
