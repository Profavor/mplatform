package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.repository.ApprovalStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalEscalationService {

    private final ApprovalStepRepository approvalStepRepository;

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public int scanAndEscalate() {
        LocalDateTime now = LocalDateTime.now();
        List<ApprovalStep> expiredSteps = approvalStepRepository.findByStatusAndSlaDueAtBeforeAndIsEscalatedFalse("PENDING", now);

        if (expiredSteps.isEmpty()) {
            return 0;
        }

        log.info("Found {} approval steps exceeding SLA deadline for escalation.", expiredSteps.size());

        for (ApprovalStep step : expiredSteps) {
            String originalAssignee = step.getAssigneeId() != null ? step.getAssigneeId() : "N/A";
            step.setEscalatedFromUserId(originalAssignee);
            step.setIsEscalated(true);
            step.setEscalatedAt(now);

            // Escalate to ADMIN or Lead role
            step.setAssigneeRole("ROLE_ADMIN");

            String escalationNote = String.format("[SLA 만료 자동 에스컬레이션] 기한 초과로 관리자(ROLE_ADMIN)에게 자동 이관되었습니다. (원 결재자: %s)", originalAssignee);
            if (step.getComment() == null || step.getComment().isBlank()) {
                step.setComment(escalationNote);
            } else {
                step.setComment(step.getComment() + "\n" + escalationNote);
            }

            approvalStepRepository.save(step);
            log.info("Escalated approval step {} from {} to ROLE_ADMIN", step.getId(), originalAssignee);
        }

        return expiredSteps.size();
    }
}
