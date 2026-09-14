use sqlx::PgPool;

pub struct ApprovalEscalationJob;

impl ApprovalEscalationJob {
    pub const LOCK_ID: i64 = 100_001;

    /// Runs the approval SLA escalation batch.
    /// Finds pending steps where sla_due_at < NOW() and escalates to ROLE_ADMIN.
    pub async fn run(pool: &PgPool) -> Result<u64, sqlx::Error> {
        let query = r#"
            UPDATE approval_step
            SET is_escalated = true,
                escalated_at = NOW(),
                escalated_from_user_id = COALESCE(assignee_id, 'N/A'),
                assignee_role = 'ROLE_ADMIN',
                comment = CASE 
                    WHEN comment IS NULL OR comment = '' THEN '[SLA 만료 자동 에스컬레이션] 기한 초과로 관리자(ROLE_ADMIN)에게 자동 이관되었습니다.'
                    ELSE comment || E'\n[SLA 만료 자동 에스컬레이션] 기한 초과로 관리자(ROLE_ADMIN)에게 자동 이관되었습니다.'
                END,
                updated_at = NOW()
            WHERE status = 'PENDING'
              AND sla_due_at < NOW()
              AND (is_escalated IS FALSE OR is_escalated IS NULL)
        "#;
        let res = sqlx::query(query).execute(pool).await?;
        let rows = res.rows_affected();
        if rows > 0 {
            tracing::info!("🔔 [Approval Escalation Batch] Escalated {} pending steps exceeding SLA", rows);
        }
        Ok(rows)
    }
}
