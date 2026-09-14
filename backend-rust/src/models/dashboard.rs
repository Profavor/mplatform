use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DashboardStats {
    pub total_domains: i64,
    pub pending_approvals: i64,
    pub approved_approvals: i64,
    pub rejected_approvals: i64,
    pub active_records: i64,
    pub pending_matches: i64,
    pub open_dq_violations: i64,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TrendItem {
    pub date: String,
    pub count: i64,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DomainDistributionItem {
    pub domain_id: Uuid,
    pub domain_name: serde_json::Value,
    pub record_count: i64,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DqSeverityItem {
    pub severity: String,
    pub count: i64,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UrgentAlertDto {
    pub record_id: Uuid,
    pub contract_no: String,
    pub building_name: Option<String>,
    pub unit_number: Option<String>,
    pub tenant_name: Option<String>,
    pub tenant_contact: Option<String>,
    pub end_date: Option<String>,
    pub days_remaining: Option<i64>,
    pub risk_type: String,
    pub debt_ratio: f64,
    pub monthly_rent: i64,
    pub deposit_amount: i64,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LeaseSummaryDto {
    pub has_lease_domain: bool,
    pub domain_id: Option<Uuid>,
    pub domain_name: Option<String>,
    pub total_contracts: i64,
    pub expiring_within_30_days: i64,
    pub expired_contracts: i64,
    pub overdue_count: i64,
    pub high_debt_ratio_count: i64,
    pub total_deposit_amount: i64,
    pub total_monthly_rent: i64,
    pub urgent_alerts: Vec<UrgentAlertDto>,
}
