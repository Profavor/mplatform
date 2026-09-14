use crate::error::AppError;
use crate::models::domain::{Domain, DomainRequest};
use sqlx::PgPool;
use uuid::Uuid;

pub struct DomainRepository;

impl DomainRepository {
    pub async fn find_all(pool: &PgPool) -> Result<Vec<Domain>, AppError> {
        let domains = sqlx::query_as::<_, Domain>(
            r#"
            SELECT id, name, description, icon, domain_type, specialized_category,
                   auto_dq_scan_enabled, current_sequence, description_field_id,
                   detail_layout_config, display_name_field_id, identifier_field_id,
                   image_field_id, numbering_pattern, organization_id, sort_order,
                   created_at, updated_at
            FROM domain
            ORDER BY sort_order ASC, created_at ASC
            "#,
        )
        .fetch_all(pool)
        .await?;

        Ok(domains)
    }

    pub async fn find_by_id(pool: &PgPool, id: Uuid) -> Result<Option<Domain>, AppError> {
        let domain = sqlx::query_as::<_, Domain>(
            r#"
            SELECT id, name, description, icon, domain_type, specialized_category,
                   auto_dq_scan_enabled, current_sequence, description_field_id,
                   detail_layout_config, display_name_field_id, identifier_field_id,
                   image_field_id, numbering_pattern, organization_id, sort_order,
                   created_at, updated_at
            FROM domain
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(pool)
        .await?;

        Ok(domain)
    }

    pub async fn create(pool: &PgPool, req: DomainRequest) -> Result<Domain, AppError> {
        let id = Uuid::new_v4();
        let domain = sqlx::query_as::<_, Domain>(
            r#"
            INSERT INTO domain (
                id, name, description, icon, domain_type, specialized_category,
                auto_dq_scan_enabled, current_sequence, sort_order, numbering_pattern,
                identifier_field_id, display_name_field_id, description_field_id, image_field_id,
                created_at, updated_at
            )
            VALUES (
                $1, $2, $3, $4, $5, $6,
                COALESCE($7, false), 0, COALESCE($8, 0), $9,
                $10, $11, $12, $13,
                NOW(), NOW()
            )
            RETURNING
                id, name, description, icon, domain_type, specialized_category,
                auto_dq_scan_enabled, current_sequence, description_field_id,
                detail_layout_config, display_name_field_id, identifier_field_id,
                image_field_id, numbering_pattern, organization_id, sort_order,
                created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(req.name)
        .bind(req.description)
        .bind(req.icon)
        .bind(req.domain_type)
        .bind(req.specialized_category)
        .bind(req.auto_dq_scan_enabled)
        .bind(req.sort_order)
        .bind(req.numbering_pattern)
        .bind(req.identifier_field_id)
        .bind(req.display_name_field_id)
        .bind(req.description_field_id)
        .bind(req.image_field_id)
        .fetch_one(pool)
        .await?;

        Ok(domain)
    }

    pub async fn update(pool: &PgPool, id: Uuid, req: DomainRequest) -> Result<Domain, AppError> {
        let domain = sqlx::query_as::<_, Domain>(
            r#"
            UPDATE domain
            SET name = $2,
                description = COALESCE($3, description),
                icon = COALESCE($4, icon),
                domain_type = COALESCE($5, domain_type),
                specialized_category = COALESCE($6, specialized_category),
                auto_dq_scan_enabled = COALESCE($7, auto_dq_scan_enabled),
                sort_order = COALESCE($8, sort_order),
                numbering_pattern = $9,
                identifier_field_id = $10,
                display_name_field_id = $11,
                description_field_id = $12,
                image_field_id = $13,
                updated_at = NOW()
            WHERE id = $1
            RETURNING
                id, name, description, icon, domain_type, specialized_category,
                auto_dq_scan_enabled, current_sequence, description_field_id,
                detail_layout_config, display_name_field_id, identifier_field_id,
                image_field_id, numbering_pattern, organization_id, sort_order,
                created_at, updated_at
            "#,
        )
        .bind(id)
        .bind(req.name)
        .bind(req.description)
        .bind(req.icon)
        .bind(req.domain_type)
        .bind(req.specialized_category)
        .bind(req.auto_dq_scan_enabled)
        .bind(req.sort_order)
        .bind(req.numbering_pattern)
        .bind(req.identifier_field_id)
        .bind(req.display_name_field_id)
        .bind(req.description_field_id)
        .bind(req.image_field_id)
        .fetch_optional(pool)
        .await?
        .ok_or_else(|| AppError::NotFound(format!("Domain not found: {id}")))?;

        Ok(domain)
    }
}
