use crate::error::AppError;
use crate::models::matching::{MatchCandidate, MatchingRule, SurvivorshipRule};
use sqlx::PgPool;
use uuid::Uuid;

pub struct MatchingRepository;

impl MatchingRepository {
    pub async fn find_matching_rules(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<MatchingRule>, AppError> {
        let rules = sqlx::query_as::<_, MatchingRule>(
            r#"
            SELECT id, domain_id, node_id, rule_name, match_type,
                   target_field_keys, similarity_threshold, is_active,
                   created_at, updated_at
            FROM matching_rule
            WHERE domain_id = $1 AND is_active = true
            ORDER BY created_at ASC
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        Ok(rules)
    }

    pub async fn find_candidates(
        pool: &PgPool,
        domain_id: Option<Uuid>,
        status: Option<&str>,
    ) -> Result<Vec<MatchCandidate>, AppError> {
        let candidates = sqlx::query_as::<_, MatchCandidate>(
            r#"
            SELECT id, domain_id, node_id, existing_record_id, matched_rule_id,
                   score, source, status, incoming_data_json, matched_field_details,
                   reviewed_by, reviewed_at, created_at
            FROM match_candidate
            WHERE ($1::uuid IS NULL OR domain_id = $1)
              AND ($2::text IS NULL OR status = $2)
            ORDER BY score DESC, created_at DESC
            "#,
        )
        .bind(domain_id)
        .bind(status)
        .fetch_all(pool)
        .await?;

        Ok(candidates)
    }

    pub async fn find_survivorship_rules(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<SurvivorshipRule>, AppError> {
        let rules = sqlx::query_as::<_, SurvivorshipRule>(
            r#"
            SELECT id, domain_id, field_key, strategy, priority
            FROM survivorship_rule
            WHERE domain_id = $1
            ORDER BY priority ASC
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        Ok(rules)
    }

    pub async fn merge_into_golden(
        pool: &PgPool,
        survivor_id: Uuid,
        merged_ids: &[Uuid],
        golden_data: serde_json::Value,
    ) -> Result<(), AppError> {
        let mut tx = pool.begin().await?;

        // 1. Update survivor record with merged golden record data
        sqlx::query(
            r#"
            UPDATE record
            SET data = $2,
                searchable_data = $2,
                version = version + 1,
                updated_at = NOW()
            WHERE id = $1
            "#,
        )
        .bind(survivor_id)
        .bind(golden_data)
        .execute(&mut *tx)
        .await?;

        // 2. Mark merged records as MERGED with pointer to survivor
        for &mid in merged_ids {
            sqlx::query(
                r#"
                UPDATE record
                SET status = 'MERGED',
                    merged_into_record_id = $1,
                    updated_at = NOW()
                WHERE id = $2
                "#,
            )
            .bind(survivor_id)
            .bind(mid)
            .execute(&mut *tx)
            .await?;
        }

        tx.commit().await?;
        Ok(())
    }

    pub async fn unmerge(pool: &PgPool, record_id: Uuid) -> Result<(), AppError> {
        sqlx::query(
            r#"
            UPDATE record
            SET status = 'ACTIVE',
                merged_into_record_id = NULL,
                updated_at = NOW()
            WHERE id = $1
            "#,
        )
        .bind(record_id)
        .execute(pool)
        .await?;

        Ok(())
    }
}
