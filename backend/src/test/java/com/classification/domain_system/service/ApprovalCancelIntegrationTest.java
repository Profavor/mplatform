package com.classification.domain_system.service;

import com.classification.domain_system.dto.MemoApprovalRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("prod")
@Transactional
class ApprovalCancelIntegrationTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    @DisplayName("기안자가 진행 중인 결재를 상신 취소하면 상태가 CANCELLED로 변경되고 단계들도 취소된다")
    void testCancelApprovalRequest_Success() {
        String requesterId = "8a779052-9ecb-405a-9171-091b58280cf3";
        String assigneeId = "098ad411-bbb8-4395-b600-b4cb65aa05b4";

        MemoApprovalRequest memoReq = MemoApprovalRequest.builder()
                .title("상신 취소 테스트 결재")
                .content("<p>테스트 본문</p>")
                .comment("기안 의견")
                .steps(List.of(
                        MemoApprovalRequest.MemoStepItem.builder()
                                .stepOrder(1)
                                .stepType("APPROVAL")
                                .assigneeId(assigneeId)
                                .build()
                ))
                .observerIds(List.of("observer-user-id"))
                .build();

        ApprovalRequest created = approvalService.requestMemoApproval(memoReq, requesterId);
        assertThat(created.getStatus()).isEqualTo(ApprovalStatus.PENDING.name());

        // When: 기안자가 상신 취소 실행
        ApprovalRequest cancelled = approvalService.cancelApprovalRequest(created.getId(), requesterId, "기안자의 자진 취소");

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(ApprovalStatus.CANCELLED.name());
        assertThat(cancelled.getReason()).isEqualTo("기안자의 자진 취소");

        ApprovalRequest reloaded = approvalService.getRequestById(created.getId());
        assertThat(reloaded.getStatus()).isEqualTo(ApprovalStatus.CANCELLED.name());
        for (ApprovalStep step : reloaded.getSteps()) {
            if (step.getStepOrder() > 0) {
                assertThat(step.getStatus()).isEqualTo(ApprovalStatus.CANCELLED.name());
            }
        }
    }

    @Test
    @DisplayName("기안자가 아닌 다른 사용자가 상신 취소 시도 시 권한 예외가 발생한다")
    void testCancelApprovalRequest_Unauthorized() {
        String requesterId = "8a779052-9ecb-405a-9171-091b58280cf3";
        String otherUserId = "098ad411-bbb8-4395-b600-b4cb65aa05b4";

        MemoApprovalRequest memoReq = MemoApprovalRequest.builder()
                .title("권한 체크 결재")
                .content("<p>본문</p>")
                .comment("의견")
                .steps(List.of(
                        MemoApprovalRequest.MemoStepItem.builder()
                                .stepOrder(1)
                                .stepType("APPROVAL")
                                .assigneeId(otherUserId)
                                .build()
                ))
                .build();

        ApprovalRequest created = approvalService.requestMemoApproval(memoReq, requesterId);

        // When & Then: 타인이 취소 시도 시 예외
        assertThrows(CustomAccessDeniedException.class, () -> {
            approvalService.cancelApprovalRequest(created.getId(), otherUserId, "불법 취소 시도");
        });
    }

    @Test
    @DisplayName("이미 취소되었거나 PENDING이 아닌 결재는 상신 취소할 수 없다")
    void testCancelApprovalRequest_AlreadyCancelled() {
        String requesterId = "8a779052-9ecb-405a-9171-091b58280cf3";

        MemoApprovalRequest memoReq = MemoApprovalRequest.builder()
                .title("중복 취소 테스트 결재")
                .content("<p>본문</p>")
                .comment("의견")
                .steps(List.of(
                        MemoApprovalRequest.MemoStepItem.builder()
                                .stepOrder(1)
                                .stepType("APPROVAL")
                                .assigneeId("098ad411-bbb8-4395-b600-b4cb65aa05b4")
                                .build()
                ))
                .build();

        ApprovalRequest created = approvalService.requestMemoApproval(memoReq, requesterId);
        approvalService.cancelApprovalRequest(created.getId(), requesterId, "1차 취소");

        // When & Then: 다시 취소 시도 시 BusinessException
        assertThrows(BusinessException.class, () -> {
            approvalService.cancelApprovalRequest(created.getId(), requesterId, "2차 중복 취소");
        });
    }
}
