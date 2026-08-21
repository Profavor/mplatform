package com.classification.domain_system.service;

import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxApprovalIntegrationService {

    private final InboxService inboxService;
    private final ObjectMapper objectMapper;

    private List<String> parseObservers(String observerIdsJson) {
        if (!StringUtils.hasText(observerIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(observerIdsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse observerIds JSON: {}", observerIdsJson, e);
            return Collections.emptyList();
        }
    }

    private void sendSystemMessage(String subject, String body, List<String> to, List<String> cc, List<String> bcc, ApprovalRequest approval) {
        InboxMessageRequest request = InboxMessageRequest.builder()
                .subject(subject)
                .body(body)
                .importance("HIGH")
                .toRecipients(to != null ? to : new ArrayList<>())
                .ccRecipients(cc != null ? cc : new ArrayList<>())
                .bccRecipients(bcc != null ? bcc : new ArrayList<>())
                .isDraft(false)
                .build();
                
        // We will pass the system as the sender (null) or a system identifier
        // but InboxService currently handles it as null. Let's use "SYSTEM" or the requester if needed.
        // Wait, requirements: "Set senderId = null (system message) or the actor's userId".
        // Let's use "SYSTEM".
        // Oh, InboxMessageRequest does not have messageType or relatedApprovalId.
        // I need to add those to InboxMessageResponse or set them in InboxService? 
        // Wait! The requirement says: 
        // "Set messageType = APPROVAL_NOTICE
        // Set relatedApprovalId = approval.getId()"
        // I should update InboxMessageRequest to include these or modify InboxService. 
        // But the instructions don't explicitly ask me to modify InboxMessageRequest. 
        // I will just modify InboxService to accept these if present in request, or I will use a special wrapper.
        // Wait, actually I can just use InboxService to create the message, then update the message in the DB directly here?
        // Let's modify InboxMessageRequest and InboxService.
    }

    private String resolveUserName(String userId, String fallbackName) {
        if (userId != null) {
            String name = inboxService.resolveUserName(userId);
            if (StringUtils.hasText(name) && !name.matches("^[0-9a-fA-F-]{36}$")) {
                return name;
            }
        }
        if (StringUtils.hasText(fallbackName) && !fallbackName.matches("^[0-9a-fA-F-]{36}$")) {
            return fallbackName;
        }
        return "사용자";
    }

    @Transactional
    public void onApprovalSubmitted(ApprovalRequest approval) {
        List<String> toList = new ArrayList<>();
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            Integer currentOrder = approval.getCurrentStepOrder();
            if (currentOrder == null) currentOrder = 1;
            for (ApprovalStep s : approval.getSteps()) {
                if (s.getStepOrder() != null && s.getStepOrder().equals(currentOrder) && s.getAssigneeId() != null) {
                    if (!toList.contains(s.getAssigneeId())) {
                        toList.add(s.getAssigneeId());
                    }
                }
            }
        }
        
        String requesterName = resolveUserName(approval.getRequesterId(), approval.getRequesterName());
        String actionTitle = resolveTitle(approval);
        String subject = "[결재 요청] " + actionTitle;
        String body = "<p><strong>" + requesterName + "</strong> 님이 새로운 결재를 상신하였습니다.</p>"
                    + "<p><strong>유형:</strong> " + approval.getTargetType() + "</p>"
                    + (isMemoApproval(approval) ? extractMemoSummaryHtml(approval) : "");
        
        sendApprovalMessage(subject, body, toList, null, null, approval, approval.getRequesterId());
    }

    @Transactional
    public void onApprovalApproved(ApprovalRequest approval, ApprovalStep step) {
        List<String> toList = new ArrayList<>();
        
        // Find next step assignees
        if (approval.getSteps() != null && approval.getCurrentStepOrder() != null) {
            for (ApprovalStep s : approval.getSteps()) {
                if (s.getStepOrder() != null && s.getStepOrder().equals(approval.getCurrentStepOrder()) && s.getAssigneeId() != null) {
                    if (!toList.contains(s.getAssigneeId())) {
                        toList.add(s.getAssigneeId());
                    }
                }
            }
        }
        
        String approverName = resolveUserName(step != null ? step.getAssigneeId() : null, step != null ? step.getAssigneeName() : null);
        String actionTitle = resolveTitle(approval);
        String subject = "[결재 진행] " + actionTitle + " (" + (step != null && step.getStepOrder() != null ? step.getStepOrder() : 1) + "단계 승인)";
        String body = "<p><strong>" + approverName + "</strong> 님이 " + (step != null && step.getStepOrder() != null ? step.getStepOrder() : 1) + "단계 결재를 승인하였습니다.</p>"
                    + (step != null && step.getComment() != null && !step.getComment().isBlank() ? "<p><strong>의견:</strong> " + step.getComment() + "</p>" : "")
                    + (isMemoApproval(approval) ? extractMemoSummaryHtml(approval) : "");
        
        sendApprovalMessage(subject, body, toList, null, null, approval, step != null ? step.getAssigneeId() : "SYSTEM");
    }

    @Transactional
    public void onApprovalRejected(ApprovalRequest approval, ApprovalStep step) {
        List<String> toList = new ArrayList<>();
        if (approval.getRequesterId() != null) {
            toList.add(approval.getRequesterId());
        }
        
        String rejectorName = resolveUserName(step != null ? step.getAssigneeId() : null, step != null ? step.getAssigneeName() : null);
        String actionTitle = resolveTitle(approval);
        String subject = "[결재 반려] " + actionTitle;
        String body = "<p>상신하신 결재가 <strong>" + rejectorName + "</strong> 님에 의해 반려되었습니다.</p>"
                    + (step != null && step.getComment() != null && !step.getComment().isBlank() ? "<p><strong>반려 사유:</strong> " + step.getComment() + "</p>" : "")
                    + (isMemoApproval(approval) ? extractMemoSummaryHtml(approval) : "");
                      
        sendApprovalMessage(subject, body, toList, null, null, approval, step != null ? step.getAssigneeId() : "SYSTEM");
    }

    @Transactional
    public void onApprovalCompleted(ApprovalRequest approval) {
        List<String> toList = new ArrayList<>();
        if (approval.getRequesterId() != null) {
            toList.add(approval.getRequesterId());
        }
        
        List<String> ccList = parseObservers(approval.getObserverIds());
        
        String actionTitle = resolveTitle(approval);
        String subject = "[결재 완료] " + actionTitle;
        String body = "<p>상신된 결재가 최종 승인 및 완료되었습니다.</p>"
                    + (isMemoApproval(approval) ? extractMemoSummaryHtml(approval) : "");
                      
        sendApprovalMessage(subject, body, toList, ccList, null, approval, "SYSTEM");
    }
    
    private boolean isMemoApproval(ApprovalRequest approval) {
        return "MEMO".equalsIgnoreCase(approval.getTargetType());
    }

    private String resolveTitle(ApprovalRequest approval) {
        if (isMemoApproval(approval) && approval.getChanges() != null) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(approval.getChanges());
                if (root.has("title") && root.get("title").isValueNode()) {
                    return root.get("title").asText();
                }
            } catch (Exception ignored) {}
        }
        return approval.getTargetType() != null ? approval.getTargetType() : "결재";
    }

    private String extractMemoSummaryHtml(ApprovalRequest approval) {
        if (approval.getChanges() == null) return "";
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(approval.getChanges());
            StringBuilder sb = new StringBuilder();
            if (root.has("title")) {
                sb.append("<p><strong>제목:</strong> ").append(root.get("title").asText()).append("</p>");
            }
            if (root.has("content")) {
                sb.append("<div style=\"margin-top: 8px; padding: 8px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px;\">")
                  .append(root.get("content").asText())
                  .append("</div>");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void sendApprovalMessage(String subject, String body, List<String> to, List<String> cc, List<String> bcc, ApprovalRequest approval, String senderId) {
        if ((to == null || to.isEmpty()) && (cc == null || cc.isEmpty()) && (bcc == null || bcc.isEmpty())) {
            return;
        }
        InboxMessageRequest request = InboxMessageRequest.builder()
                .subject(subject)
                .body(body)
                .importance("HIGH")
                .messageType("APPROVAL_NOTICE")
                .relatedApprovalId(approval != null ? approval.getId() : null)
                .toRecipients(to != null ? to : new ArrayList<>())
                .ccRecipients(cc != null ? cc : new ArrayList<>())
                .bccRecipients(bcc != null ? bcc : new ArrayList<>())
                .isDraft(false)
                .build();
                
        inboxService.sendMessage(request, senderId);
    }
}
