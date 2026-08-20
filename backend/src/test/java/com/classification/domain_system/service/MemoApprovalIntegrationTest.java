package com.classification.domain_system.service;

import com.classification.domain_system.dto.MemoApprovalRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemoApprovalIntegrationTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    void testMemoApprovalRealExecution() {
        MemoApprovalRequest request = MemoApprovalRequest.builder()
                .title("테스트 메모 결재")
                .content("<p>내용입니다</p>")
                .comment("의견")
                .steps(List.of(
                        MemoApprovalRequest.MemoStepItem.builder()
                                .stepOrder(1)
                                .stepType("APPROVAL")
                                .assigneeId("098ad411-bbb8-4395-b600-b4cb65aa05b4")
                                .build()
                ))
                .observerIds(List.of())
                .build();

        ApprovalRequest result = approvalService.requestMemoApproval(request, "8a779052-9ecb-405a-9171-091b58280cf3");
        System.out.println("Result: " + result.getId());
    }
}
