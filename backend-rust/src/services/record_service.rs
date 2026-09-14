use std::collections::HashMap;
use serde_json::Value;
use crate::error::AppError;
use crate::models::field_definition::FieldDefinition;
use crate::models::record::{CreateRecordRequest, PageResponse, Record};
use crate::repositories::record_repo::{DynamicRecordQuery, RecordRepository};
use crate::services::data_masking_service::DataMaskingService;
use crate::services::field_encryption_service::FieldEncryptionService;
use sqlx::PgPool;
use uuid::Uuid;

pub struct RecordService;

impl RecordService {
    pub async fn prepare_record_for_read(
        pool: &PgPool,
        record: &mut Record,
        can_unmask: bool,
    ) -> Result<(), AppError> {
        if let Some(data) = record.data.as_mut() {
            let fields = crate::handlers::field_definition::fetch_effective_fields(pool, record.node_id).await?;
            DataMaskingService::mask_json_data(data, &fields, can_unmask);
        }
        Ok(())
    }

    pub async fn prepare_records_for_read(
        pool: &PgPool,
        records: &mut [Record],
        can_unmask: bool,
    ) -> Result<(), AppError> {
        let mut fields_cache: HashMap<Uuid, Vec<FieldDefinition>> = HashMap::new();
        for rec in records.iter_mut() {
            if let Some(data) = rec.data.as_mut() {
                let fields = match fields_cache.get(&rec.node_id) {
                    Some(f) => f,
                    None => {
                        let f = crate::handlers::field_definition::fetch_effective_fields(pool, rec.node_id).await?;
                        fields_cache.entry(rec.node_id).or_insert(f)
                    }
                };
                DataMaskingService::mask_json_data(data, fields, can_unmask);
            }
        }
        Ok(())
    }

    pub async fn process_data_for_save(
        pool: &PgPool,
        node_id: Uuid,
        mut data: Value,
        encryption_service: &FieldEncryptionService,
    ) -> Result<Value, AppError> {
        let fields = crate::handlers::field_definition::fetch_effective_fields(pool, node_id).await?;
        if let Some(obj) = data.as_object_mut() {
            for fd in fields {
                if fd.is_encrypted == Some(true) {
                    let field_key = &fd.field_key;
                    let matched_key = obj.keys().find(|k| k.eq_ignore_ascii_case(field_key)).cloned();
                    if let Some(mk) = matched_key {
                        if let Some(val_str) = obj.get(&mk).and_then(|v| v.as_str()) {
                            if !val_str.trim().is_empty() && !FieldEncryptionService::is_vault_encrypted(val_str) {
                                let encrypted = encryption_service.encrypt(val_str).await?;
                                let blind_index = encryption_service.generate_blind_index(val_str).await?;
                                let pattern = fd.masking_pattern.as_deref().unwrap_or("GENERIC");
                                let mask_val = if pattern.eq_ignore_ascii_case("EMAIL") || field_key.to_lowercase().contains("email") {
                                    DataMaskingService::mask_email(val_str)
                                } else if DataMaskingService::is_specific_masking_pattern(Some(pattern)) {
                                    DataMaskingService::mask_by_pattern(pattern, val_str)
                                } else {
                                    DataMaskingService::mask_email(val_str)
                                };

                                obj.insert(mk, Value::String(encrypted));
                                obj.insert(format!("_idx_{}", field_key), Value::String(blind_index));
                                obj.insert(format!("_mask_{}", field_key), Value::String(mask_val));
                            }
                        }
                    }
                }
            }
        }
        Ok(data)
    }

    pub async fn find_dynamic(
        pool: &PgPool,
        q: &DynamicRecordQuery,
    ) -> Result<PageResponse<Record>, AppError> {
        let mut response = RecordRepository::find_dynamic(pool, q).await?;
        Self::prepare_records_for_read(pool, &mut response.content, false).await?;
        Ok(response)
    }

    pub async fn get_records(
        pool: &PgPool,
        node_id: Option<Uuid>,
        status: Option<&str>,
        page: i64,
        size: i64,
    ) -> Result<PageResponse<Record>, AppError> {
        let mut response = RecordRepository::find_paginated(pool, node_id, status, page, size).await?;
        Self::prepare_records_for_read(pool, &mut response.content, false).await?;
        Ok(response)
    }

    pub async fn get_record_by_id(pool: &PgPool, id: Uuid) -> Result<Record, AppError> {
        let mut record = RecordRepository::find_by_id(pool, id)
            .await?
            .ok_or_else(|| AppError::NotFound(format!("Record not found: {id}")))?;
        Self::prepare_record_for_read(pool, &mut record, false).await?;
        Ok(record)
    }

    pub async fn create_record(
        pool: &PgPool,
        req: CreateRecordRequest,
        actor: &str,
    ) -> Result<Record, AppError> {
        let record_id = Uuid::new_v4();
        let record = RecordRepository::insert(
            pool,
            record_id,
            req.node_id,
            req.data.clone(),
            req.source_system.as_deref(),
        )
        .await?;

        // Write version 1 history for hash-chain audit ledger
        let _ = RecordRepository::insert_history(
            pool,
            record.id,
            1,
            "CREATE",
            actor,
            None,
            Some(req.data),
            req.source_system.as_deref(),
        )
        .await?;

        let mut masked_record = record;
        Self::prepare_record_for_read(pool, &mut masked_record, false).await?;
        Ok(masked_record)
    }
}
