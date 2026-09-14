use crate::models::dashboard::*;
use sqlx::{PgPool, Row};
use uuid::Uuid;
use chrono::Utc;

pub struct DashboardRepository;

impl DashboardRepository {
    pub async fn get_stats(pool: &PgPool) -> Result<DashboardStats, sqlx::Error> {
        let total_domains: i64 = sqlx::query_scalar("SELECT count(*) FROM domain")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let pending_approvals: i64 = sqlx::query_scalar("SELECT count(*) FROM approval_request WHERE status = 'PENDING'")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let approved_approvals: i64 = sqlx::query_scalar("SELECT count(*) FROM approval_request WHERE status = 'APPROVED'")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let rejected_approvals: i64 = sqlx::query_scalar("SELECT count(*) FROM approval_request WHERE status = 'REJECTED'")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let active_records: i64 = sqlx::query_scalar("SELECT count(*) FROM record WHERE status = 'ACTIVE'")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let pending_matches: i64 = sqlx::query_scalar("SELECT count(*) FROM match_candidate WHERE status = 'PENDING'")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        let open_dq_violations: i64 = sqlx::query_scalar("SELECT count(*) FROM dq_violation WHERE resolved = false")
            .fetch_one(pool)
            .await
            .unwrap_or(0);

        Ok(DashboardStats {
            total_domains,
            pending_approvals,
            approved_approvals,
            rejected_approvals,
            active_records,
            pending_matches,
            open_dq_violations,
        })
    }

    pub async fn get_approval_trends(pool: &PgPool) -> Result<Vec<TrendItem>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            WITH dates AS (
                SELECT to_char(d::date, 'YYYY-MM-DD') AS dt
                FROM generate_series(CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE, '1 day'::interval) d
            ),
            counts AS (
                SELECT to_char(created_at, 'YYYY-MM-DD') AS dt, count(*) AS cnt
                FROM approval_request
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY to_char(created_at, 'YYYY-MM-DD')
            )
            SELECT dates.dt, COALESCE(counts.cnt, 0) AS cnt
            FROM dates
            LEFT JOIN counts ON dates.dt = counts.dt
            ORDER BY dates.dt ASC
            "#
        )
        .fetch_all(pool)
        .await?;

        let trends = rows.into_iter().map(|r| TrendItem {
            date: r.get::<String, _>("dt"),
            count: r.get::<i64, _>("cnt"),
        }).collect();

        Ok(trends)
    }

    pub async fn get_dq_trends(pool: &PgPool) -> Result<Vec<TrendItem>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            WITH dates AS (
                SELECT to_char(d::date, 'YYYY-MM-DD') AS dt
                FROM generate_series(CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE, '1 day'::interval) d
            ),
            counts AS (
                SELECT to_char(checked_at, 'YYYY-MM-DD') AS dt, count(*) AS cnt
                FROM dq_violation
                WHERE checked_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY to_char(checked_at, 'YYYY-MM-DD')
            )
            SELECT dates.dt, COALESCE(counts.cnt, 0) AS cnt
            FROM dates
            LEFT JOIN counts ON dates.dt = counts.dt
            ORDER BY dates.dt ASC
            "#
        )
        .fetch_all(pool)
        .await?;

        let trends = rows.into_iter().map(|r| TrendItem {
            date: r.get::<String, _>("dt"),
            count: r.get::<i64, _>("cnt"),
        }).collect();

        Ok(trends)
    }

    pub async fn get_domain_distribution(pool: &PgPool) -> Result<Vec<DomainDistributionItem>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            SELECT d.id, d.name, count(r.id) as record_count
            FROM domain d
            LEFT JOIN classification_axis a ON d.id = a.domain_id
            LEFT JOIN classification_node n ON a.id = n.axis_id
            LEFT JOIN record r ON n.id = r.node_id
            GROUP BY d.id, d.name
            ORDER BY record_count DESC
            LIMIT 20
            "#
        )
        .fetch_all(pool)
        .await?;

        let items = rows.into_iter().map(|r| DomainDistributionItem {
            domain_id: r.get::<Uuid, _>("id"),
            domain_name: r.get::<serde_json::Value, _>("name"),
            record_count: r.get::<i64, _>("record_count"),
        }).collect();

        Ok(items)
    }

    pub async fn get_dq_severity_distribution(pool: &PgPool) -> Result<Vec<DqSeverityItem>, sqlx::Error> {
        let rows = sqlx::query(
            r#"
            SELECT COALESCE(severity, 'UNKNOWN') as severity, count(*) as count
            FROM dq_violation
            WHERE resolved = false
            GROUP BY severity
            ORDER BY count DESC
            "#
        )
        .fetch_all(pool)
        .await?;

        let items = rows.into_iter().map(|r| DqSeverityItem {
            severity: r.get::<String, _>("severity"),
            count: r.get::<i64, _>("count"),
        }).collect();

        Ok(items)
    }

    pub async fn get_lease_summary(pool: &PgPool, org_id: Option<Uuid>) -> Result<LeaseSummaryDto, sqlx::Error> {
        // Find lease contract domain
        let domain_row = if let Some(oid) = org_id {
            sqlx::query("SELECT id, name FROM domain WHERE specialized_category = 'LEASE_CONTRACT' AND organization_id = $1 LIMIT 1")
                .bind(oid)
                .fetch_optional(pool)
                .await?
        } else {
            sqlx::query("SELECT id, name FROM domain WHERE specialized_category = 'LEASE_CONTRACT' LIMIT 1")
                .fetch_optional(pool)
                .await?
        };

        let (domain_id, domain_name_str) = match domain_row {
            Some(r) => {
                let id: Uuid = r.get("id");
                let name_val: serde_json::Value = r.get("name");
                let name_str = name_val.get("ko").and_then(|v| v.as_str()).unwrap_or("부동산 임대차 마스터").to_string();
                (id, name_str)
            },
            None => {
                return Ok(LeaseSummaryDto {
                    has_lease_domain: false,
                    domain_id: None,
                    domain_name: None,
                    total_contracts: 0,
                    expiring_within_30_days: 0,
                    expired_contracts: 0,
                    overdue_count: 0,
                    high_debt_ratio_count: 0,
                    total_deposit_amount: 0,
                    total_monthly_rent: 0,
                    urgent_alerts: Vec::new(),
                });
            }
        };

        // Fetch records in lease domain
        let rows = sqlx::query(
            r#"
            SELECT r.id, r.data
            FROM record r
            JOIN classification_node n ON r.node_id = n.id
            JOIN classification_axis a ON n.axis_id = a.id
            WHERE a.domain_id = $1
            "#
        )
        .bind(domain_id)
        .fetch_all(pool)
        .await?;

        let total_contracts = rows.len() as i64;
        let mut expiring_within_30_days = 0i64;
        let mut expired_contracts = 0i64;
        let mut overdue_count = 0i64;
        let mut high_debt_ratio_count = 0i64;
        let mut total_deposit_amount = 0i64;
        let mut total_monthly_rent = 0i64;

        let today = Utc::now().date_naive();
        let mut alerts = Vec::new();

        for r in rows {
            let rid: Uuid = r.get("id");
            let data_val: serde_json::Value = r.get("data");

            let contract_no = data_val.get("contract_no").and_then(|v| v.as_str()).unwrap_or("").to_string();
            let building_name = data_val.get("building_name").and_then(|v| v.as_str()).map(|s| s.to_string());
            let unit_number = data_val.get("unit_number").and_then(|v| v.as_str()).map(|s| s.to_string());
            let tenant_name = data_val.get("tenant_name").and_then(|v| v.as_str()).map(|s| s.to_string());
            let tenant_contact = data_val.get("tenant_contact").and_then(|v| v.as_str()).map(|s| s.to_string());
            let end_date_str = data_val.get("contract_end_date").and_then(|v| v.as_str()).map(|s| s.to_string());
            let contract_status = data_val.get("contract_status").and_then(|v| v.as_str()).unwrap_or("");

            let deposit = data_val.get("deposit_amount").and_then(|v| v.as_i64()).unwrap_or(0);
            let rent = data_val.get("monthly_rent").and_then(|v| v.as_i64()).unwrap_or(0);
            let debt_ratio = data_val.get("debt_ratio").and_then(|v| v.as_f64()).unwrap_or(0.0);

            total_deposit_amount += deposit;
            total_monthly_rent += rent;

            let is_overdue = contract_status.eq_ignore_ascii_case("OVERDUE");
            if is_overdue { overdue_count += 1; }

            let is_high_debt = debt_ratio >= 80.0;
            if is_high_debt { high_debt_ratio_count += 1; }

            let mut days_remaining: Option<i64> = None;
            let mut is_expiring_soon = false;
            let mut is_expired = false;

            if let Some(ref ed_str) = end_date_str {
                if let Ok(ed) = chrono::NaiveDate::parse_from_str(&ed_str[..10.min(ed_str.len())], "%Y-%m-%d") {
                    let diff = (ed - today).num_days();
                    days_remaining = Some(diff);
                    if diff < 0 {
                        expired_contracts += 1;
                        is_expired = true;
                    } else if diff <= 30 {
                        expiring_within_30_days += 1;
                        is_expiring_soon = true;
                    }
                }
            }

            if is_expiring_soon || is_overdue || is_high_debt || is_expired {
                let risk_type = if is_overdue {
                    "OVERDUE"
                } else if is_expiring_soon {
                    "EXPIRING_SOON"
                } else if is_high_debt {
                    "HIGH_DEBT"
                } else {
                    "EXPIRED"
                };

                alerts.push(UrgentAlertDto {
                    record_id: rid,
                    contract_no: if contract_no.is_empty() { format!("LEASE-{}", &rid.to_string()[..8]) } else { contract_no },
                    building_name,
                    unit_number,
                    tenant_name,
                    tenant_contact,
                    end_date: end_date_str,
                    days_remaining,
                    risk_type: risk_type.to_string(),
                    debt_ratio,
                    monthly_rent: rent,
                    deposit_amount: deposit,
                });
            }
        }

        alerts.sort_by(|a, b| {
            if a.risk_type == "OVERDUE" && b.risk_type != "OVERDUE" {
                std::cmp::Ordering::Less
            } else if a.risk_type != "OVERDUE" && b.risk_type == "OVERDUE" {
                std::cmp::Ordering::Greater
            } else {
                a.days_remaining.cmp(&b.days_remaining)
            }
        });

        alerts.truncate(10);

        Ok(LeaseSummaryDto {
            has_lease_domain: true,
            domain_id: Some(domain_id),
            domain_name: Some(domain_name_str),
            total_contracts,
            expiring_within_30_days,
            expired_contracts,
            overdue_count,
            high_debt_ratio_count,
            total_deposit_amount,
            total_monthly_rent,
            urgent_alerts: alerts,
        })
    }
}
