use crate::models::governance::*;
use chrono::Utc;
use sqlx::{PgPool, Row};
use uuid::Uuid;

pub struct GovernanceRepository;

impl GovernanceRepository {
    pub async fn get_business_terms(
        pool: &PgPool,
        domain_id: Option<Uuid>,
    ) -> Result<Vec<BusinessTerm>, sqlx::Error> {
        let terms = if let Some(did) = domain_id {
            sqlx::query_as::<_, BusinessTerm>(
                "SELECT * FROM business_terms WHERE domain_id = $1 OR domain_id IS NULL ORDER BY term_code ASC"
            )
            .bind(did)
            .fetch_all(pool)
            .await?
        } else {
            sqlx::query_as::<_, BusinessTerm>("SELECT * FROM business_terms ORDER BY term_code ASC")
                .fetch_all(pool)
                .await?
        };

        Ok(terms)
    }

    pub async fn create_business_term(
        pool: &PgPool,
        req: CreateBusinessTermRequest,
    ) -> Result<BusinessTerm, sqlx::Error> {
        let id = Uuid::new_v4();
        let now = Utc::now();

        let term = sqlx::query_as::<_, BusinessTerm>(
            r#"
            INSERT INTO business_terms (
                id, term_code, term_name, description, domain_id, data_type,
                synonyms, abbreviation, sensitivity_level, created_at
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
            RETURNING *
            "#,
        )
        .bind(id)
        .bind(req.term_code)
        .bind(req.term_name)
        .bind(req.description)
        .bind(req.domain_id)
        .bind(req.data_type)
        .bind(req.synonyms)
        .bind(req.abbreviation)
        .bind(req.sensitivity_level)
        .bind(now)
        .fetch_one(pool)
        .await?;

        Ok(term)
    }

    pub async fn get_masking_policies(
        pool: &PgPool,
        domain_id: Option<Uuid>,
    ) -> Result<Vec<ColumnMaskingPolicy>, sqlx::Error> {
        let policies = if let Some(did) = domain_id {
            sqlx::query_as::<_, ColumnMaskingPolicy>(
                "SELECT * FROM column_masking_policy WHERE domain_id = $1 OR domain_id IS NULL ORDER BY created_at DESC"
            )
            .bind(did)
            .fetch_all(pool)
            .await?
        } else {
            sqlx::query_as::<_, ColumnMaskingPolicy>(
                "SELECT * FROM column_masking_policy ORDER BY created_at DESC",
            )
            .fetch_all(pool)
            .await?
        };

        Ok(policies)
    }

    pub async fn create_masking_policy(
        pool: &PgPool,
        req: CreateMaskingPolicyRequest,
        created_by: &str,
    ) -> Result<ColumnMaskingPolicy, sqlx::Error> {
        let id = Uuid::new_v4();
        let now = Utc::now();

        let policy = sqlx::query_as::<_, ColumnMaskingPolicy>(
            r#"
            INSERT INTO column_masking_policy (
                id, domain_id, field_key, target_type, target_id,
                masking_action, is_active, description, created_by,
                created_at, updated_at
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $10)
            RETURNING *
            "#,
        )
        .bind(id)
        .bind(req.domain_id)
        .bind(req.field_key)
        .bind(req.target_type)
        .bind(req.target_id)
        .bind(req.masking_action)
        .bind(req.is_active)
        .bind(req.description)
        .bind(created_by)
        .bind(now)
        .fetch_one(pool)
        .await?;

        Ok(policy)
    }
}
