use crate::models::integration::*;
use chrono::Utc;
use sqlx::PgPool;
use uuid::Uuid;

pub struct IntegrationRepository;

impl IntegrationRepository {
    pub async fn get_channels(pool: &PgPool, org_id: Option<Uuid>) -> Result<Vec<IntegrationChannel>, sqlx::Error> {
        let channels = if let Some(oid) = org_id {
            sqlx::query_as::<_, IntegrationChannel>(
                r#"
                SELECT c.* 
                FROM integration_channels c
                JOIN classification_node n ON c.node_id = n.id
                JOIN domain d ON n.domain_id = d.id
                WHERE d.organization_id = $1
                ORDER BY c.created_at DESC
                "#,
            )
            .bind(oid)
            .fetch_all(pool)
            .await?
        } else {
            sqlx::query_as::<_, IntegrationChannel>(
                "SELECT * FROM integration_channels ORDER BY created_at DESC",
            )
            .fetch_all(pool)
            .await?
        };

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
            "#,
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
            r#"
            SELECT 
                l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                c.name AS channel_name,
                c.channel_code AS channel_code
            FROM integration_logs l
            LEFT JOIN integration_channels c ON l.channel_id = c.id
            ORDER BY l.created_at DESC 
            LIMIT $1
            "#
        )
        .bind(limit)
        .fetch_all(pool)
        .await?;

        Ok(logs)
    }

    pub async fn get_logs_paged(
        pool: &PgPool,
        org_id: Option<Uuid>,
        channel_id: Option<Uuid>,
        only_dead_letter: bool,
        limit: i64,
        offset: i64,
    ) -> Result<(Vec<IntegrationLog>, i64), sqlx::Error> {
        let (total, logs) = match (org_id, channel_id, only_dead_letter) {
            (Some(oid), Some(cid), true) => {
                let total: i64 = sqlx::query_scalar(
                    r#"
                    SELECT COUNT(*) FROM integration_logs l
                    WHERE l.channel_id = $1 AND l.status != 'SUCCESS'
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $2
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $2
                        )
                      )
                    "#
                )
                .bind(cid)
                .bind(oid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.channel_id = $1 AND l.status != 'SUCCESS'
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $4
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $4
                        )
                      )
                    ORDER BY l.created_at DESC
                    LIMIT $2 OFFSET $3
                    "#
                )
                .bind(cid)
                .bind(limit)
                .bind(offset)
                .bind(oid)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (Some(oid), Some(cid), false) => {
                let total: i64 = sqlx::query_scalar(
                    r#"
                    SELECT COUNT(*) FROM integration_logs l
                    WHERE l.channel_id = $1
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $2
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $2
                        )
                      )
                    "#
                )
                .bind(cid)
                .bind(oid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.channel_id = $1
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $4
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $4
                        )
                      )
                    ORDER BY l.created_at DESC
                    LIMIT $2 OFFSET $3
                    "#
                )
                .bind(cid)
                .bind(limit)
                .bind(offset)
                .bind(oid)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (Some(oid), None, true) => {
                let total: i64 = sqlx::query_scalar(
                    r#"
                    SELECT COUNT(*) FROM integration_logs l
                    WHERE l.status != 'SUCCESS'
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $1
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $1
                        )
                      )
                    "#
                )
                .bind(oid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.status != 'SUCCESS'
                      AND (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $3
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $3
                        )
                      )
                    ORDER BY l.created_at DESC
                    LIMIT $1 OFFSET $2
                    "#
                )
                .bind(limit)
                .bind(offset)
                .bind(oid)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (Some(oid), None, false) => {
                let total: i64 = sqlx::query_scalar(
                    r#"
                    SELECT COUNT(*) FROM integration_logs l
                    WHERE (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $1
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $1
                        )
                    )
                    "#
                )
                .bind(oid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE (
                        l.channel_id IN (
                          SELECT c2.id FROM integration_channels c2 
                          JOIN classification_node n2 ON c2.node_id = n2.id 
                          JOIN domain d2 ON n2.domain_id = d2.id 
                          WHERE d2.organization_id = $3
                        )
                        OR l.record_id IN (
                          SELECT r2.id FROM record r2 
                          JOIN classification_node cn2 ON r2.node_id = cn2.id JOIN domain d2 ON cn2.domain_id = d2.id 
                          WHERE d2.organization_id = $3
                        )
                    )
                    ORDER BY l.created_at DESC
                    LIMIT $1 OFFSET $2
                    "#
                )
                .bind(limit)
                .bind(offset)
                .bind(oid)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (None, Some(cid), true) => {
                let total: i64 = sqlx::query_scalar(
                    "SELECT COUNT(*) FROM integration_logs WHERE channel_id = $1 AND status != 'SUCCESS'"
                )
                .bind(cid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.channel_id = $1 AND l.status != 'SUCCESS'
                    ORDER BY l.created_at DESC
                    LIMIT $2 OFFSET $3
                    "#
                )
                .bind(cid)
                .bind(limit)
                .bind(offset)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (None, Some(cid), false) => {
                let total: i64 = sqlx::query_scalar(
                    "SELECT COUNT(*) FROM integration_logs WHERE channel_id = $1",
                )
                .bind(cid)
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.channel_id = $1
                    ORDER BY l.created_at DESC
                    LIMIT $2 OFFSET $3
                    "#
                )
                .bind(cid)
                .bind(limit)
                .bind(offset)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (None, None, true) => {
                let total: i64 = sqlx::query_scalar(
                    "SELECT COUNT(*) FROM integration_logs WHERE status != 'SUCCESS'",
                )
                .fetch_one(pool)
                .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    WHERE l.status != 'SUCCESS'
                    ORDER BY l.created_at DESC
                    LIMIT $1 OFFSET $2
                    "#
                )
                .bind(limit)
                .bind(offset)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
            (None, None, false) => {
                let total: i64 = sqlx::query_scalar("SELECT COUNT(*) FROM integration_logs")
                    .fetch_one(pool)
                    .await?;

                let logs = sqlx::query_as::<_, IntegrationLog>(
                    r#"
                    SELECT 
                        l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                        l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                        COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                        c.name AS channel_name,
                        c.channel_code AS channel_code
                    FROM integration_logs l
                    LEFT JOIN integration_channels c ON l.channel_id = c.id
                    ORDER BY l.created_at DESC
                    LIMIT $1 OFFSET $2
                    "#
                )
                .bind(limit)
                .bind(offset)
                .fetch_all(pool)
                .await?;

                (total, logs)
            }
        };

        Ok((logs, total))
    }

    pub async fn get_logs_by_record(
        pool: &PgPool,
        record_id: Uuid,
    ) -> Result<Vec<IntegrationLog>, sqlx::Error> {
        let logs = sqlx::query_as::<_, IntegrationLog>(
            r#"
            SELECT 
                l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                c.name AS channel_name,
                c.channel_code AS channel_code
            FROM integration_logs l
            LEFT JOIN integration_channels c ON l.channel_id = c.id
            WHERE l.record_id = $1
            ORDER BY l.created_at DESC
            LIMIT 50
            "#
        )
        .bind(record_id)
        .fetch_all(pool)
        .await?;

        if !logs.is_empty() {
            return Ok(logs);
        }

        // Fallback: Return recent inbound/batch channel logs if no record-specific log was recorded
        let fallback_logs = sqlx::query_as::<_, IntegrationLog>(
            r#"
            SELECT 
                l.id, l.channel_id, l.record_id, l.event_type, l.status, l.retry_count, 
                l.error_message, l.original_payload, l.mapped_payload, l.created_at,
                COALESCE(c.direction, CASE WHEN l.event_type ILIKE '%INBOUND%' OR l.event_type ILIKE '%SPRING_BATCH%' OR l.event_type ILIKE '%INGEST%' THEN 'INBOUND' ELSE 'OUTBOUND' END) AS direction,
                c.name AS channel_name,
                c.channel_code AS channel_code
            FROM integration_logs l
            LEFT JOIN integration_channels c ON l.channel_id = c.id
            WHERE l.channel_id IN (
                SELECT id FROM integration_channels 
                WHERE channel_code = 'CH-KRX-INBOUND-001' OR type = 'SPRING_BATCH' OR type = 'SYSTEM_BATCH'
            )
            ORDER BY l.created_at DESC
            LIMIT 10
            "#
        )
        .fetch_all(pool)
        .await?;

        Ok(fallback_logs)
    }

    pub async fn get_channel_by_id(
        pool: &PgPool,
        id: Uuid,
    ) -> Result<Option<IntegrationChannel>, sqlx::Error> {
        let channel = sqlx::query_as::<_, IntegrationChannel>(
            "SELECT * FROM integration_channels WHERE id = $1",
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(channel)
    }

    pub async fn get_logs_last_24h(
        pool: &PgPool,
        channel_id: Uuid,
    ) -> Result<Vec<(String, Option<chrono::NaiveDateTime>)>, sqlx::Error> {
        #[derive(sqlx::FromRow)]
        struct LogSimpleRow {
            status: String,
            created_at: Option<chrono::NaiveDateTime>,
        }

        let rows = sqlx::query_as::<_, LogSimpleRow>(
            r#"
            SELECT status, created_at
            FROM integration_logs
            WHERE channel_id = $1
              AND created_at >= NOW() - INTERVAL '24 hours'
            ORDER BY created_at ASC
            "#,
        )
        .bind(channel_id)
        .fetch_all(pool)
        .await?;

        Ok(rows.into_iter().map(|r| (r.status, r.created_at)).collect())
    }
}
