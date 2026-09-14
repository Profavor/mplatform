use sqlx::PgPool;
use uuid::Uuid;

pub struct DqScanJob;

impl DqScanJob {
    pub const LOCK_ID: i64 = 100_003;

    /// Runs periodic Data Quality scan across active domains.
    pub async fn run(pool: &PgPool) -> Result<u64, sqlx::Error> {
        let domains: Vec<(Uuid,)> = sqlx::query_as(
            r#"
            SELECT id
            FROM domain
            WHERE auto_dq_scan_enabled = true
            "#,
        )
        .fetch_all(pool)
        .await?;

        let mut scanned = 0;
        for (domain_id,) in domains {
            let total_records: i64 = sqlx::query_scalar(
                r#"
                SELECT COUNT(*)::int8
                FROM record r
                JOIN classification_node n ON r.node_id = n.id
                WHERE n.domain_id = $1
                "#,
            )
            .bind(domain_id)
            .fetch_one(pool)
            .await
            .unwrap_or(0);

            let violations: i64 = sqlx::query_scalar(
                r#"
                SELECT COUNT(*)::int8
                FROM dq_violation v
                JOIN dq_rule r ON v.dq_rule_id = r.id
                WHERE r.domain_id = $1 AND v.resolved = false
                "#,
            )
            .bind(domain_id)
            .fetch_one(pool)
            .await
            .unwrap_or(0);

            let score = if total_records > 0 {
                let valid = (total_records - violations).max(0) as f64;
                ((valid / total_records as f64) * 1000.0).round() / 10.0
            } else {
                100.0
            };

            let snap_id = Uuid::new_v4();
            let _ = sqlx::query(
                r#"
                INSERT INTO dq_score_snapshot (id, domain_id, recorded_at, scan_type, score, total_records, total_violations)
                VALUES ($1, $2, NOW(), 'SCHEDULED', $3, $4, $5)
                "#,
            )
            .bind(snap_id)
            .bind(domain_id)
            .bind(score)
            .bind(total_records)
            .bind(violations)
            .execute(pool)
            .await;

            scanned += 1;
        }

        if scanned > 0 {
            tracing::info!("🔍 [DQ Scheduled Scan Batch] Completed DQ scan for {} domains", scanned);
        }
        Ok(scanned)
    }
}
