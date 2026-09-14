use crate::error::AppError;
use crate::models::domain::{Domain, DomainResponse};
use crate::models::record::Record;
use crate::models::search::GlobalSearchResponse;
use sqlx::PgPool;

pub struct SearchService;

impl SearchService {
    pub async fn global_search(
        pool: &PgPool,
        query: &str,
    ) -> Result<GlobalSearchResponse, AppError> {
        let clean = query.trim();
        let exact_pattern = format!("%{clean}%");

        // 1. Search domains
        let domains = sqlx::query_as::<_, Domain>(
            r#"
            SELECT id, name, description, icon, domain_type, specialized_category,
                   auto_dq_scan_enabled, current_sequence, description_field_id,
                   detail_layout_config, display_name_field_id, identifier_field_id,
                   image_field_id, numbering_pattern, organization_id, sort_order,
                   created_at, updated_at
            FROM domain
            WHERE name::text ILIKE $1 OR description::text ILIKE $1
            ORDER BY sort_order ASC
            LIMIT 10
            "#,
        )
        .bind(&exact_pattern)
        .fetch_all(pool)
        .await?;

        // 2. Acronym expansion & tokenization (e.g. LG <-> 엘지, SK <-> 에스케이)
        let mut alt_query = clean.to_string();
        let acronym_mappings = [
            ("LG", "엘지"),
            ("lg", "엘지"),
            ("Lg", "엘지"),
            ("엘지", "LG"),
            ("SK", "에스케이"),
            ("sk", "에스케이"),
            ("에스케이", "SK"),
            ("CJ", "씨제이"),
            ("cj", "씨제이"),
            ("씨제이", "CJ"),
            ("GS", "지에스"),
            ("gs", "지에스"),
            ("지에스", "GS"),
            ("KT", "케이티"),
            ("kt", "케이티"),
            ("케이티", "KT"),
            ("HD", "에이치디"),
            ("hd", "에이치디"),
            ("에이치디", "HD"),
            ("LS", "엘에스"),
            ("ls", "엘에스"),
            ("엘에스", "LS"),
        ];

        for (from, to) in acronym_mappings {
            if alt_query.contains(from) {
                alt_query = alt_query.replace(from, to);
                break;
            }
        }
        let alt_pattern = format!("%{alt_query}%");

        // Sub-tokenization (by whitespace or ASCII/Hangul script boundary)
        let mut tokens: Vec<String> = clean.split_whitespace().map(|s| s.to_string()).collect();

        if tokens.len() == 1 {
            let s = &tokens[0];
            let mut split_idx = 0;
            let mut prev_ascii: Option<bool> = None;
            for (idx, ch) in s.char_indices() {
                let is_ascii = ch.is_ascii_alphanumeric();
                if let Some(prev) = prev_ascii {
                    if prev != is_ascii {
                        split_idx = idx;
                        break;
                    }
                }
                prev_ascii = Some(is_ascii);
            }
            if split_idx > 0 && split_idx < s.len() {
                let (p1, p2) = s.split_at(split_idx);
                tokens = vec![p1.to_string(), p2.to_string()];
            }
        }

        let token1_pattern = tokens
            .get(0)
            .map(|t| format!("%{t}%"))
            .unwrap_or_else(|| exact_pattern.clone());
        let token2_pattern = tokens
            .get(1)
            .map(|t| format!("%{t}%"))
            .unwrap_or_else(|| exact_pattern.clone());

        // 3. Search records across JSONB data + JOIN classification_node & domain
        let records = sqlx::query_as::<_, Record>(
            r#"
            SELECT r.id, r.node_id, r.data, r.searchable_data, r.status, r.version, r.source_system,
                   r.merged_into_record_id, r.approval_request_id, r.created_at, r.updated_at,
                   jsonb_build_object(
                       'id', n.id,
                       'name', n.name,
                       'path', n.path,
                       'domain', jsonb_build_object('id', d.id, 'name', d.name)
                   ) as node
            FROM record r
            LEFT JOIN classification_node n ON r.node_id = n.id
            LEFT JOIN domain d ON n.domain_id = d.id
            WHERE r.status = 'ACTIVE'
              AND (
                  r.data::text ILIKE $1 OR r.searchable_data::text ILIKE $1
                  OR r.data::text ILIKE $2 OR r.searchable_data::text ILIKE $2
                  OR (
                      (r.data::text ILIKE $3 OR r.searchable_data::text ILIKE $3)
                      AND (r.data::text ILIKE $4 OR r.searchable_data::text ILIKE $4)
                  )
              )
            ORDER BY (
                CASE 
                    WHEN r.data->>'stock_name' ILIKE $1 THEN 0 
                    WHEN r.data->>'stock_name' ILIKE $2 THEN 1 
                    WHEN r.data::text ILIKE $1 THEN 2 
                    WHEN r.data::text ILIKE $2 THEN 3 
                    ELSE 4 
                END
            ), r.created_at DESC
            LIMIT 20
            "#,
        )
        .bind(&exact_pattern)
        .bind(&alt_pattern)
        .bind(&token1_pattern)
        .bind(&token2_pattern)
        .fetch_all(pool)
        .await?;

        let domain_responses: Vec<DomainResponse> =
            domains.into_iter().map(DomainResponse::from).collect();
        let total_hits = domain_responses.len() + records.len();

        Ok(GlobalSearchResponse {
            query: query.to_string(),
            total_hits,
            total_elements: total_hits,
            domains: domain_responses,
            content: records.clone(),
            records,
        })
    }
}
