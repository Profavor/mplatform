use crate::error::AppError;
use crate::models::dq::{
    BatchValidateRequest, BatchValidateResponse, CreateDqRuleRequest, DqRule, DqScanResult,
    DqViolation, DqViolationItem, RuleWithField,
};
use crate::repositories::dq_repo::DqRepository;
use crate::repositories::record_repo::RecordRepository;
use regex::Regex;
use serde_json::json;
use sqlx::PgPool;
use uuid::Uuid;

pub struct DqService;

impl DqService {
    pub async fn get_rules(pool: &PgPool, domain_id: Uuid) -> Result<Vec<DqRule>, AppError> {
        DqRepository::find_rules_by_domain(pool, domain_id).await
    }

    pub async fn create_rule(pool: &PgPool, req: CreateDqRuleRequest) -> Result<DqRule, AppError> {
        DqRepository::create_rule(pool, req).await
    }

    pub async fn get_violations_for_record(
        pool: &PgPool,
        record_id: Uuid,
    ) -> Result<Vec<DqViolation>, AppError> {
        DqRepository::find_violations_by_record(pool, record_id).await
    }

    pub async fn batch_validate(
        pool: &PgPool,
        req: BatchValidateRequest,
    ) -> Result<BatchValidateResponse, AppError> {
        let rules = DqRepository::find_rules_with_fields_by_domain(pool, req.domain_id).await?;
        let total = req.records.len();
        let mut violations = Vec::new();
        let mut invalid_set = std::collections::HashSet::new();

        for (idx, record) in req.records.iter().enumerate() {
            let data = match record.get("data") {
                Some(d) => d,
                None => record,
            };

            for rule in &rules {
                if let Some(err_msg) = Self::evaluate_rule(rule, data) {
                    invalid_set.insert(idx);
                    let actual_val = data
                        .get(&rule.field_key)
                        .map(|v| v.to_string())
                        .unwrap_or_default();

                    violations.push(DqViolationItem {
                        record_index: idx,
                        field_key: rule.field_key.clone(),
                        rule_type: rule.rule_type.clone(),
                        severity: rule.severity.clone(),
                        message: err_msg,
                        actual_value: Some(actual_val),
                    });
                }
            }
        }

        let invalid_count = invalid_set.len();
        let valid_count = total.saturating_sub(invalid_count);

        Ok(BatchValidateResponse {
            total_records: total,
            valid_records: valid_count,
            invalid_records: invalid_count,
            violations,
        })
    }

    pub async fn scan_domain(pool: &PgPool, domain_id: Uuid) -> Result<DqScanResult, AppError> {
        let rules = DqRepository::find_rules_with_fields_by_domain(pool, domain_id).await?;
        let records: Vec<(Uuid, Option<serde_json::Value>)> = sqlx::query_as(
            r#"
            SELECT r.id, r.data
            FROM record r
            JOIN classification_node cn ON r.node_id = cn.id
            WHERE cn.domain_id = $1 AND (r.status = 'ACTIVE' OR r.status IS NULL)
            LIMIT 2000
            "#,
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        let mut violation_count = 0;
        let scanned = records.len();

        for (record_id, data_opt) in records {
            if let Some(ref data) = data_opt {
                let _ = DqRepository::delete_violations_for_record(pool, record_id).await;

                for rule in &rules {
                    if let Some(err_msg) = Self::evaluate_rule(rule, data) {
                        let actual_val = data
                            .get(&rule.field_key)
                            .map(|v| v.to_string())
                            .unwrap_or_default();

                        let _ = DqRepository::insert_violation(
                            pool,
                            record_id,
                            Some(rule.id),
                            &rule.field_key,
                            &rule.severity,
                            json!({ "ko": err_msg, "en": err_msg }),
                            Some(&actual_val),
                        )
                        .await;

                        violation_count += 1;
                    }
                }
            }
        }

        Ok(DqScanResult {
            scanned_records: scanned,
            violation_count,
            total_rules: rules.len(),
            status: "COMPLETED".to_string(),
        })
    }

    pub fn evaluate_rule(rule: &RuleWithField, data: &serde_json::Value) -> Option<String> {
        let value = data.get(&rule.field_key);

        match rule.rule_type.as_str() {
            "NOT_NULL" => {
                if value.is_none()
                    || value.unwrap().is_null()
                    || (value.unwrap().is_string()
                        && value.unwrap().as_str().unwrap().trim().is_empty())
                {
                    return Some(format!(
                        "필수 필드 [{}] 값이 누락되었습니다.",
                        rule.field_key
                    ));
                }
            }
            "LENGTH" => {
                if let Some(val) = value {
                    if let Some(s) = val.as_str() {
                        let len = s.chars().count();
                        if let Some(ref params) = rule.params {
                            if let Some(min) = params.get("min").and_then(|v| v.as_u64()) {
                                if len < min as usize {
                                    return Some(format!(
                                        "[{}] 길이({})가 최소 길이({}) 미만입니다.",
                                        rule.field_key, len, min
                                    ));
                                }
                            }
                            if let Some(max) = params.get("max").and_then(|v| v.as_u64()) {
                                if len > max as usize {
                                    return Some(format!(
                                        "[{}] 길이({})가 최대 길이({})를 초과합니다.",
                                        rule.field_key, len, max
                                    ));
                                }
                            }
                        }
                    }
                }
            }
            "RANGE" => {
                if let Some(val) = value {
                    if let Some(num) = val.as_f64() {
                        if let Some(ref params) = rule.params {
                            if let Some(min) = params.get("min").and_then(|v| v.as_f64()) {
                                if num < min {
                                    return Some(format!(
                                        "[{}] 값({})이 최소값({}) 미만입니다.",
                                        rule.field_key, num, min
                                    ));
                                }
                            }
                            if let Some(max) = params.get("max").and_then(|v| v.as_f64()) {
                                if num > max {
                                    return Some(format!(
                                        "[{}] 값({})이 최대값({})을 초과합니다.",
                                        rule.field_key, num, max
                                    ));
                                }
                            }
                        }
                    }
                }
            }
            "ENUM" => {
                if let Some(val) = value {
                    if let Some(s) = val.as_str() {
                        if let Some(ref params) = rule.params {
                            if let Some(allowed) =
                                params.get("allowedValues").and_then(|v| v.as_array())
                            {
                                let valid = allowed.iter().any(|a| a.as_str() == Some(s));
                                if !valid {
                                    return Some(format!(
                                        "[{}] 값('{}')이 허용된 목록에 존재하지 않습니다.",
                                        rule.field_key, s
                                    ));
                                }
                            }
                        }
                    }
                }
            }
            "REGEX" => {
                if let Some(val) = value {
                    if let Some(s) = val.as_str() {
                        if let Some(ref params) = rule.params {
                            if let Some(pattern) = params.get("pattern").and_then(|v| v.as_str()) {
                                if let Ok(re) = Regex::new(pattern) {
                                    if !re.is_match(s) {
                                        return Some(format!(
                                            "[{}] 값('{}')이 정규식 패턴과 일치하지 않습니다.",
                                            rule.field_key, s
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "BUSINESS_NO_CHECKSUM" => {
                if let Some(val) = value {
                    if let Some(s) = val.as_str() {
                        if let Some(err) = Self::validate_business_no(s) {
                            return Some(err);
                        }
                    }
                }
            }
            "CORPORATE_NO_CHECKSUM" => {
                if let Some(val) = value {
                    if let Some(s) = val.as_str() {
                        if let Some(err) = Self::validate_corporate_no(s) {
                            return Some(err);
                        }
                    }
                }
            }
            _ => {}
        }

        None
    }

    pub fn validate_business_no(input: &str) -> Option<String> {
        let clean: String = input.chars().filter(|c| c.is_ascii_digit()).collect();
        if clean.len() != 10 {
            return Some("사업자등록번호는 10자리 숫자여야 합니다.".to_string());
        }

        let weights = [1, 3, 7, 1, 3, 7, 1, 3, 5];
        let digits: Vec<u32> = clean.chars().map(|c| c.to_digit(10).unwrap()).collect();

        let mut sum = 0;
        for i in 0..8 {
            sum += digits[i] * weights[i];
        }

        let d8 = digits[8];
        sum += (d8 * 5) / 10;
        sum += (d8 * 5) % 10;

        let check_digit = (10 - (sum % 10)) % 10;
        if check_digit != digits[9] {
            return Some("유효하지 않은 사업자등록번호 체크섬입니다.".to_string());
        }

        None
    }

    pub fn validate_corporate_no(input: &str) -> Option<String> {
        let clean: String = input.chars().filter(|c| c.is_ascii_digit()).collect();
        if clean.len() != 13 {
            return Some("법인등록번호는 13자리 숫자여야 합니다.".to_string());
        }

        let weights = [1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];
        let digits: Vec<u32> = clean.chars().map(|c| c.to_digit(10).unwrap()).collect();

        let mut sum = 0;
        for i in 0..12 {
            sum += digits[i] * weights[i];
        }

        let check_digit = (10 - (sum % 10)) % 10;
        if check_digit != digits[12] {
            return Some("유효하지 않은 법인등록번호 체크섬입니다.".to_string());
        }

        None
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_business_no_checksum() {
        // Valid business number test: 104-86-45676 (checksum 6)
        assert!(DqService::validate_business_no("1048645676").is_none());
        // Invalid check digit
        assert!(DqService::validate_business_no("1048645679").is_some());
        // Invalid length
        assert!(DqService::validate_business_no("12345").is_some());
    }

    #[test]
    fn test_corporate_no_checksum() {
        // Invalid length
        assert!(DqService::validate_corporate_no("123456").is_some());
    }
}
