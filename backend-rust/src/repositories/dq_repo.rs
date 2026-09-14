use crate::error::AppError;
use crate::models::dq::{CreateDqRuleRequest, DqRule, DqViolation};
use sqlx::PgPool;
use uuid::Uuid;

pub struct DqRepository;

impl DqRepository {
    pub async fn find_rules_by_domain(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<DqRule>, AppError> {
        let rules = sqlx::query_as::<_, DqRule>(
            r#"
            SELECT id, domain_id, node_id, field_definition_id, rule_type,
                   severity, is_active, sort_order, params, message,
                   created_at, updated_at
            FROM dq_rule
            WHERE (domain_id = $1 OR domain_id IS NULL) AND is_active = true
            ORDER BY sort_order ASC
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        Ok(rules)
    }

    pub async fn find_rules_with_fields_by_domain(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<crate::models::dq::RuleWithField>, AppError> {
        let rules = sqlx::query_as::<_, crate::models::dq::RuleWithField>(
            r#"
            SELECT r.id, r.domain_id, r.node_id, r.field_definition_id,
                   f.field_key, r.rule_type, r.severity, r.is_active,
                   r.sort_order, r.params, r.message
            FROM dq_rule r
            JOIN field_definition f ON r.field_definition_id = f.id
            WHERE (r.domain_id = $1 OR r.domain_id IS NULL) AND r.is_active = true
            ORDER BY r.sort_order ASC
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        Ok(rules)
    }

    pub async fn create_rule(
        pool: &PgPool,
        req: CreateDqRuleRequest,
    ) -> Result<DqRule, AppError> {
        let id = Uuid::new_v4();
        let rule = sqlx::query_as::<_, DqRule>(
            r#"
            INSERT INTO dq_rule (
                id, domain_id, node_id, field_definition_id, rule_type,
                severity, is_active, sort_order, params, message,
                created_at, updated_at
            )
            VALUES (
                $1, $2, $3, $4, $5,
                $6, COALESCE($7, true), COALESCE($8, 0), $9, $10,
                NOW(), NOW()
            )
            RETURNING
                id, domain_id, node_id, field_definition_id, rule_type,
                severity, is_active, sort_order, params, message,
                created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(req.domain_id)
        .bind(req.node_id)
        .bind(req.field_definition_id)
        .bind(req.rule_type)
        .bind(req.severity)
        .bind(req.is_active)
        .bind(req.sort_order)
        .bind(req.params)
        .bind(req.message)
        .fetch_one(pool)
        .await?;

        Ok(rule)
    }

    pub async fn find_violations_by_record(
        pool: &PgPool,
        record_id: Uuid,
    ) -> Result<Vec<DqViolation>, AppError> {
        let violations = sqlx::query_as::<_, DqViolation>(
            r#"
            SELECT id, record_id, dq_rule_id, field_key, severity,
                   message, actual_value, resolved, checked_at
            FROM dq_violation
            WHERE record_id = $1
            ORDER BY checked_at DESC
            "#,
        )
        .bind(record_id)
        .fetch_all(pool)
        .await?;

        Ok(violations)
    }

    pub async fn delete_violations_for_record(
        pool: &PgPool,
        record_id: Uuid,
    ) -> Result<(), AppError> {
        sqlx::query("DELETE FROM dq_violation WHERE record_id = $1")
            .bind(record_id)
            .execute(pool)
            .await?;

        Ok(())
    }

    pub async fn insert_violation(
        pool: &PgPool,
        record_id: Uuid,
        dq_rule_id: Option<Uuid>,
        field_key: &str,
        severity: &str,
        message: serde_json::Value,
        actual_value: Option<&str>,
    ) -> Result<DqViolation, AppError> {
        let id = Uuid::new_v4();
        let violation = sqlx::query_as::<_, DqViolation>(
            r#"
            INSERT INTO dq_violation (
                id, record_id, dq_rule_id, field_key, severity,
                message, actual_value, resolved, checked_at
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, false, NOW())
            RETURNING
                id, record_id, dq_rule_id, field_key, severity,
                message, actual_value, resolved, checked_at
            "#,
        )
        .bind(id)
        .bind(record_id)
        .bind(dq_rule_id)
        .bind(field_key)
        .bind(severity)
        .bind(message)
        .bind(actual_value)
        .fetch_one(pool)
        .await?;

        Ok(violation)
    }
}
