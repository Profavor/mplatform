package com.classification.domain_system.service;

import com.classification.domain_system.dto.RejectionAnalyticsDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RejectionAnalyticsService {

    private final ApprovalRequestRepository approvalRequestRepository;

    @Transactional(readOnly = true)
    public RejectionAnalyticsDto.RejectionAnalysisResponse analyzeRejections() {
        List<ApprovalRequest> rejectedRequests = approvalRequestRepository.findByStatus("REJECTED");

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("필수값 누락", 0);
        categoryCounts.put("데이터 포맷 오류", 0);
        categoryCounts.put("중복 데이터 의심", 0);
        categoryCounts.put("권한/증빙 미비", 0);
        categoryCounts.put("기타 사유", 0);

        for (ApprovalRequest req : rejectedRequests) {
            String combinedText = (req.getReason() != null ? req.getReason() : "");
            if (req.getSteps() != null) {
                for (ApprovalStep s : req.getSteps()) {
                    if (s.getComment() != null) combinedText += " " + s.getComment();
                }
            }

            String category = classifyReason(combinedText);
            categoryCounts.put(category, categoryCounts.get(category) + 1);
        }

        int total = rejectedRequests.size();
        List<RejectionAnalyticsDto.RejectionCategoryCount> topCategories = new ArrayList<>();

        Map<String, String> guides = Map.of(
                "필수값 누락", "신청 전 필수 필드(식별자, 담당자 등)가 모두 채워져 있는지 사전 검증을 실행하세요.",
                "데이터 포맷 오류", "전화번호, 사업자번호, 이메일 등의 정규식 유효성을 확인 후 제출하세요.",
                "중복 데이터 의심", "기존 등록된 마스터 레코드와 유사도 검색(Fuzzy/골든 레코드)을 먼저 진행하세요.",
                "권한/증빙 미비", "필요한 부서 합의 및 증빙 첨부 파일이 누락되지 않았는지 확인하세요.",
                "기타 사유", "결재권자의 상세 반려 코멘트를 확인하고 보완하세요."
        );

        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            double pct = total > 0 ? Math.round((entry.getValue() * 100.0 / total) * 10.0) / 10.0 : 0.0;
            topCategories.add(RejectionAnalyticsDto.RejectionCategoryCount.builder()
                    .category(entry.getKey())
                    .count(entry.getValue())
                    .percentage(pct)
                    .guide(guides.getOrDefault(entry.getKey(), ""))
                    .build());
        }

        List<String> checklist = List.of(
                "1. 필수 입력 필드 누락 여부 확인",
                "2. 전화번호/이메일/코드 규격 형식 유효성 점검",
                "3. 기존 데이터 중복 여부 사전 조회",
                "4. 결재선 지정 롤 및 대결자 상태 확인"
        );

        return RejectionAnalyticsDto.RejectionAnalysisResponse.builder()
                .totalRejections(total)
                .topCategories(topCategories)
                .recommendedChecklist(checklist)
                .summary(String.format("총 %d건의 과거 반려 이력을 분석하여 주요 원인 통계 및 재신청 가이드를 도출했습니다.", total))
                .build();
    }

    private String classifyReason(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("누락") || lower.contains("필수") || lower.contains("empty") || lower.contains("required")) {
            return "필수값 누락";
        }
        if (lower.contains("포맷") || lower.contains("형식") || lower.contains("format") || lower.contains("invalid") || lower.contains("오류")) {
            return "데이터 포맷 오류";
        }
        if (lower.contains("중복") || lower.contains("duplicate") || lower.contains("이미")) {
            return "중복 데이터 의심";
        }
        if (lower.contains("증빙") || lower.contains("권한") || lower.contains("첨부") || lower.contains("permission")) {
            return "권한/증빙 미비";
        }
        return "기타 사유";
    }
}
