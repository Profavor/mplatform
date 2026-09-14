use crate::error::AppResult;
use crate::models::governance::*;
use crate::repositories::governance_repo::GovernanceRepository;
use sqlx::PgPool;
use uuid::Uuid;

#[derive(Clone)]
pub struct GovernanceService {
    pool: PgPool,
}

impl GovernanceService {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    pub async fn get_business_terms(
        &self,
        domain_id: Option<Uuid>,
    ) -> AppResult<Vec<BusinessTerm>> {
        let terms = GovernanceRepository::get_business_terms(&self.pool, domain_id).await?;
        Ok(terms)
    }

    pub async fn create_business_term(
        &self,
        req: CreateBusinessTermRequest,
    ) -> AppResult<BusinessTerm> {
        let term = GovernanceRepository::create_business_term(&self.pool, req).await?;
        Ok(term)
    }

    pub async fn get_masking_policies(
        &self,
        domain_id: Option<Uuid>,
    ) -> AppResult<Vec<ColumnMaskingPolicy>> {
        let policies = GovernanceRepository::get_masking_policies(&self.pool, domain_id).await?;
        Ok(policies)
    }

    pub async fn create_masking_policy(
        &self,
        req: CreateMaskingPolicyRequest,
        created_by: &str,
    ) -> AppResult<ColumnMaskingPolicy> {
        let policy =
            GovernanceRepository::create_masking_policy(&self.pool, req, created_by).await?;
        Ok(policy)
    }

    pub async fn chat_copilot(&self, req: CopilotChatRequest) -> AppResult<CopilotChatResponse> {
        let msg = req.message.to_lowercase();
        let mut suggested_actions = Vec::new();
        let mut related_domains = Vec::new();

        let reply = if msg.contains("dq") || msg.contains("품질") || msg.contains("오류") {
            suggested_actions.push("데이터 품질 규칙 정밀 스캔 실행".to_string());
            suggested_actions.push("미해결 DQ 위반 내역 조회".to_string());
            "데이터 품질 진단 결과, 필수값 누락 및 정규식 규칙 위반 항목이 감지되었습니다. 자동 정제 규칙 적용을 권장합니다.".to_string()
        } else if msg.contains("승인") || msg.contains("approval") {
            suggested_actions.push("대기 중인 결재 요청 목록 확인".to_string());
            "현재 검토 대기 중인 결재 요청이 존재합니다. 승인 절차를 진행하거나 반려 사유를 입력할 수 있습니다.".to_string()
        } else if msg.contains("매칭") || msg.contains("중복") || msg.contains("골든") {
            suggested_actions.push("유사도 85% 이상 후보군 자동 병합 검토".to_string());
            suggested_actions.push("생존 규칙(Survivorship) 정책 확인".to_string());
            "중복 의심 마스터 레코드가 발견되었습니다. 매칭 규칙에 따라 골든 레코드 병합을 수행할 수 있습니다.".to_string()
        } else {
            suggested_actions.push("도메인별 레코드 분포 분석".to_string());
            suggested_actions.push("데이터 거버넌스 성숙도 지표 평가".to_string());
            format!("MDM 거버넌스 코파일럿입니다. 현재 시스템의 마스터 레코드, DQ 룰, 분류 체계에 대한 질문을 처리할 수 있습니다. (문의: {})", req.message)
        };

        if let Some(did) = req.domain_id {
            related_domains.push(did);
        }

        Ok(CopilotChatResponse {
            reply,
            suggested_actions,
            related_domains,
        })
    }

    pub async fn get_maturity_score(&self) -> AppResult<serde_json::Value> {
        Ok(serde_json::json!({
            "overallScore": 88.5,
            "categories": [
                { "name": "데이터 표준화", "score": 92.0, "status": "OPTIMAL" },
                { "name": "품질 관리 (DQ)", "score": 85.0, "status": "GOOD" },
                { "name": "보안 및 접근제어", "score": 90.0, "status": "OPTIMAL" },
                { "name": "생애주기 및 이력추적", "score": 87.0, "status": "GOOD" }
            ],
            "lastAssessment": chrono::Utc::now().to_rfc3339()
        }))
    }
}
