use std::collections::HashMap;
use regex::Regex;
use serde_json::Value;

use crate::models::field_definition::FieldDefinition;

pub struct DataMaskingService;

impl DataMaskingService {
    pub fn is_specific_masking_pattern(pattern: Option<&str>) -> bool {
        let p = match pattern {
            Some(s) if !s.trim().is_empty() => s.to_uppercase(),
            _ => return false,
        };
        if p == "NONE" || p == "OFF" || p == "GENERIC" || p == "DEFAULT" {
            return false;
        }
        p.contains("RRN")
            || p.contains("RESIDENT")
            || p.contains("JUMIN")
            || p.contains("SSN")
            || p.contains("PHONE")
            || p.contains("MOBILE")
            || p.contains("TEL")
            || p.contains("EMAIL")
            || p.contains("MAIL")
            || p.contains("CARD")
            || p.contains("ACCOUNT")
            || p.contains("BANK")
    }

    pub fn mask_by_pattern(pattern: &str, val: &str) -> String {
        if val.trim().is_empty() {
            return val.to_string();
        }
        let upper = pattern.to_uppercase();
        if upper.contains("RRN")
            || upper.contains("RESIDENT")
            || upper.contains("JUMIN")
            || upper.contains("SSN")
        {
            return Self::mask_rrn(val);
        }
        if upper.contains("PHONE") || upper.contains("MOBILE") || upper.contains("TEL") {
            return Self::mask_phone(val);
        }
        if upper.contains("EMAIL") || upper.contains("MAIL") {
            return Self::mask_email(val);
        }
        if upper.contains("CARD") || upper.contains("ACCOUNT") || upper.contains("BANK") {
            return Self::mask_card(val);
        }
        val.to_string()
    }

    pub fn mask_email(email: &str) -> String {
        if !email.contains('@') {
            return email.to_string();
        }
        let parts: Vec<&str> = email.splitn(2, '@').collect();
        let name = parts[0];
        let domain = parts[1];
        if name.len() <= 1 {
            format!("{}***@{}", name, domain)
        } else {
            let first_char = name.chars().next().unwrap_or('*');
            format!("{}***@{}", first_char, domain)
        }
    }

    pub fn mask_phone(phone: &str) -> String {
        let re = Regex::new(r"(\d{2,3})[-.]?(\d{3,4})[-.]?(\d{4})").unwrap();
        re.replace_all(phone, "$1-****-$3").to_string()
    }

    pub fn mask_rrn(rrn: &str) -> String {
        let re = Regex::new(r"(\d{6})[-.]?(\d)[0-9]{6}").unwrap();
        re.replace_all(rrn, "$1-$2******").to_string()
    }

    pub fn mask_card(card: &str) -> String {
        let re = Regex::new(r"(\d{4})[-.]?(\d{4})[-.]?(\d{4})[-.]?(\d{4})").unwrap();
        re.replace_all(card, "$1-****-****-$4").to_string()
    }

    /// Masks record data JSON in place according to field definitions.
    /// 1. Strips internal `_idx_*` keys from output.
    /// 2. For encrypted fields (`is_encrypted == true`), NEVER returns ciphertext;
    ///    returns cached `_mask_<key>` or `"********"`.
    /// 3. For sensitive pattern fields, applies pattern masking if `can_unmask == false`.
    pub fn mask_json_data(data: &mut Value, fields: &[FieldDefinition], can_unmask: bool) {
        let obj = match data.as_object_mut() {
            Some(o) => o,
            None => return,
        };

        // 1. Strip internal _idx_ keys
        obj.retain(|k, _| !k.starts_with("_idx_"));

        // 2. Build field definitions lookup by lowercase field_key
        let mut field_map: HashMap<String, &FieldDefinition> = HashMap::new();
        for fd in fields {
            let l_key = fd.field_key.to_lowercase();
            match field_map.get(&l_key) {
                None => {
                    field_map.insert(l_key, fd);
                }
                Some(existing) => {
                    // Prioritize encrypted field definition
                    if existing.is_encrypted != Some(true) && fd.is_encrypted == Some(true) {
                        field_map.insert(l_key, fd);
                    } else if existing.masking_pattern.is_none() && fd.masking_pattern.is_some() {
                        field_map.insert(l_key, fd);
                    }
                }
            }
        }

        // 3. Extract mask cache map for quick lookup
        let mut mask_cache: HashMap<String, String> = HashMap::new();
        for (k, v) in obj.iter() {
            if let Some(mask_str) = v.as_str() {
                if !mask_str.trim().is_empty() {
                    mask_cache.insert(k.to_lowercase(), mask_str.to_string());
                }
            }
        }

        // 4. Transform fields
        let keys_to_process: Vec<String> = obj.keys().cloned().collect();
        for key in keys_to_process {
            let l_key = key.to_lowercase();
            if let Some(fd) = field_map.get(&l_key) {
                if fd.is_encrypted == Some(true) {
                    // Encrypted field handling: Return _mask_<key> cache or fallback
                    let mask_key_exact = format!("_mask_{}", key).to_lowercase();
                    let mask_val = mask_cache
                        .get(&mask_key_exact)
                        .cloned()
                        .unwrap_or_else(|| "********".to_string());
                    obj.insert(key, Value::String(mask_val));
                } else if Self::is_specific_masking_pattern(fd.masking_pattern.as_deref()) {
                    if !can_unmask {
                        if let Some(val_str) = obj.get(&key).and_then(|v| v.as_str()) {
                            let pattern = fd.masking_pattern.as_deref().unwrap_or("GENERIC");
                            let masked = Self::mask_by_pattern(pattern, val_str);
                            obj.insert(key, Value::String(masked));
                        }
                    }
                }
            }
        }
    }

    /// Masks change payload (which may contain nested before/after/data maps)
    pub fn mask_changes_json(changes: &mut Value, fields: &[FieldDefinition], can_unmask: bool) {
        if let Some(obj) = changes.as_object_mut() {
            for sub_key in &["data", "newData", "previousData", "changes", "before", "after"] {
                if let Some(sub_val) = obj.get_mut(*sub_key) {
                    if sub_val.is_object() {
                        Self::mask_json_data(sub_val, fields, can_unmask);
                    }
                }
            }
            Self::mask_json_data(changes, fields, can_unmask);
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_mask_email() {
        assert_eq!(DataMaskingService::mask_email("rofavor@naver.com"), "r***@naver.com");
        assert_eq!(DataMaskingService::mask_email("a@example.com"), "a***@example.com");
    }

    #[test]
    fn test_mask_phone() {
        assert_eq!(DataMaskingService::mask_phone("010-1234-5678"), "010-****-5678");
        assert_eq!(DataMaskingService::mask_phone("02-123-4567"), "02-****-4567");
    }

    #[test]
    fn test_mask_encrypted_field_uses_mask_cache() {
        let mut data = serde_json::json!({
            "CONTACT_EMAIL": "vault:v1:someciphertext",
            "_mask_CONTACT_EMAIL": "r***@naver.com",
            "_idx_CONTACT_EMAIL": "vault:v1:hmacindex",
            "NAME": "Customer A"
        });

        let fd = FieldDefinition {
            id: uuid::Uuid::new_v4(),
            domain_id: None,
            defined_at_node_id: None,
            field_group_id: None,
            field_key: "CONTACT_EMAIL".to_string(),
            name: None,
            field_type: Some("EMAIL".to_string()),
            required: None,
            is_searchable: None,
            is_read_only: None,
            is_hidden: None,
            is_encrypted: Some(true),
            is_immutable: None,
            is_highlighted: None,
            is_multi_value: None,
            is_table: None,
            is_removed: None,
            is_indexed: None,
            default_value: None,
            options: None,
            hint: None,
            unit: None,
            masking_pattern: Some("EMAIL".to_string()),
            field_order: None,
            grid_width: None,
            table_column_width: None,
            created_at: None,
            updated_at: None,
            field_group: None,
        };

        DataMaskingService::mask_json_data(&mut data, &[fd], false);

        assert_eq!(data["CONTACT_EMAIL"], "r***@naver.com");
        assert!(data.get("_idx_CONTACT_EMAIL").is_none());
        assert_eq!(data["NAME"], "Customer A");
    }
}
