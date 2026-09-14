use crate::models::integration::*;
use chrono::Utc;
use sqlx::PgPool;
use uuid::Uuid;

pub struct IntegrationRepository;

impl IntegrationRepository {
    pub async fn get_channels(pool: &PgPool) -> Result<Vec<IntegrationChannel>, sqlx::Error> {
        let channels = sqlx::query_as::<_, IntegrationChannel>(
            "SELECT * FROM integration_channels ORDER BY created_at DESC"
        )
        .fetch_all(pool)
        .await?;

        Ok(channels)
    }

    pub async fn create_channel(
        pool: &PgPool,
        req: CreateChannelRequest,
    ) -> Result<IntegrationChannel, sqlx::Error> {
        let id = Uuid::new_v4();
        let now = Utc::now();

        let channel = sqlx::query_as::<_, IntegrationChannel>(
            r#"
            INSERT INTO integration_channels (
                id, name, channel_code, type, direction, config_json,
                mapping_config_json, is_active, requires_approval,
                max_retries, retry_backoff_ms, use_exponential_backoff,
                node_id, created_at, updated_at
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $14)
            RETURNING *
            "#
        )
        .bind(id)
        .bind(req.name)
        .bind(req.channel_code)
        .bind(req.r#type)
        .bind(req.direction)
        .bind(req.config_json)
        .bind(req.mapping_config_json)
        .bind(req.is_active)
        .bind(req.requires_approval)
        .bind(req.max_retries)
        .bind(req.retry_backoff_ms)
        .bind(req.use_exponential_backoff)
        .bind(req.node_id)
        .bind(now)
        .fetch_one(pool)
        .await?;

        Ok(channel)
    }

    pub async fn get_logs(pool: &PgPool, limit: i64) -> Result<Vec<IntegrationLog>, sqlx::Error> {
        let logs = sqlx::query_as::<_, IntegrationLog>(
            "SELECT id, channel_id, record_id, event_type, status, retry_count, error_message, original_payload, mapped_payload, created_at FROM integration_logs ORDER BY created_at DESC LIMIT $1"
        )
        .bind(limit)
        .fetch_all(pool)
        .await?;

        Ok(logs)
    }
}
