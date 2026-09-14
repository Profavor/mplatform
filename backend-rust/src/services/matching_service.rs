use crate::error::AppError;
use crate::models::matching::{
    MatchCandidate, MatchingRule, MergeRequest, MergeResult, SurvivorshipRule,
};
use crate::repositories::matching_repo::MatchingRepository;
use crate::repositories::record_repo::RecordRepository;
use sqlx::PgPool;
use uuid::Uuid;

pub struct MatchingService;

impl MatchingService {
    pub async fn get_matching_rules(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<MatchingRule>, AppError> {
        MatchingRepository::find_matching_rules(pool, domain_id).await
    }

    pub async fn get_candidates(
        pool: &PgPool,
        domain_id: Option<Uuid>,
        status: Option<&str>,
    ) -> Result<Vec<MatchCandidate>, AppError> {
        MatchingRepository::find_candidates(pool, domain_id, status).await
    }

    pub async fn get_survivorship_rules(
        pool: &PgPool,
        domain_id: Uuid,
    ) -> Result<Vec<SurvivorshipRule>, AppError> {
        MatchingRepository::find_survivorship_rules(pool, domain_id).await
    }

    pub async fn execute_merge(
        pool: &PgPool,
        req: MergeRequest,
        actor: &str,
    ) -> Result<MergeResult, AppError> {
        let survivor = RecordRepository::find_by_id(pool, req.survivor_record_id)
            .await?
            .ok_or_else(|| {
                AppError::NotFound(format!(
                    "Survivor record not found: {}",
                    req.survivor_record_id
                ))
            })?;

        let mut golden_data = survivor.data.unwrap_or(serde_json::json!({}));

        // Merge attributes from merged records
        for &mid in &req.merged_record_ids {
            if let Some(merged_rec) = RecordRepository::find_by_id(pool, mid).await? {
                if let Some(mdata) = merged_rec.data {
                    if let (Some(g_obj), Some(m_obj)) =
                        (golden_data.as_object_mut(), mdata.as_object())
                    {
                        for (k, v) in m_obj {
                            // Fill missing/null fields from secondary records
                            if !g_obj.contains_key(k)
                                || g_obj.get(k).map(|x| x.is_null()).unwrap_or(true)
                            {
                                g_obj.insert(k.clone(), v.clone());
                            }
                        }
                    }
                }
            }
        }

        // Apply custom manual overrides if provided
        if let Some(overrides) = req.custom_overrides {
            if let (Some(g_obj), Some(o_obj)) = (golden_data.as_object_mut(), overrides.as_object())
            {
                for (k, v) in o_obj {
                    g_obj.insert(k.clone(), v.clone());
                }
            }
        }

        // Persist golden merge
        MatchingRepository::merge_into_golden(
            pool,
            req.survivor_record_id,
            &req.merged_record_ids,
            golden_data.clone(),
        )
        .await?;

        // Write history for hash-chain audit ledger
        let _ = RecordRepository::insert_history(
            pool,
            survivor.id,
            survivor.version + 1,
            "MERGE",
            actor,
            None,
            Some(golden_data),
            None,
            None,
        )
        .await;

        Ok(MergeResult {
            golden_record_id: survivor.id,
            merged_count: req.merged_record_ids.len(),
            status: "MERGED".to_string(),
        })
    }

    pub async fn execute_unmerge(
        pool: &PgPool,
        record_id: Uuid,
        actor: &str,
    ) -> Result<(), AppError> {
        MatchingRepository::unmerge(pool, record_id).await?;

        // Write history for unmerge
        let _ = RecordRepository::insert_history(
            pool, record_id, 1, "UNMERGE", actor, None, None, None, None,
        )
        .await;

        Ok(())
    }
}
