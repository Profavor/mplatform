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
    private final com.classification.domain_system.repository.InboxMessageRepository messageRepository;

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

    @Transactional
    public void onApprovalSubmitted(ApprovalRequest approval) {
        List<String> observers = parseObservers(approval.getObserverIds());
        
        List<String> toList = new ArrayList<>();
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            ApprovalStep firstStep = approval.getSteps().get(0);
            if (firstStep.getAssigneeId() != null) {
                toList.add(firstStep.getAssigneeId());
            }
        }
        
        String subject = "Approval Request Submitted: " + approval.getTargetType();
        String body = "<p>A new approval request has been submitted by " + approval.getRequesterName() + ".</p>";
        
        sendApprovalMessage(subject, body, toList, null, observers, approval, approval.getRequesterId());
    }

    @Transactional
    public void onApprovalApproved(ApprovalRequest approval, ApprovalStep step) {
        List<String> toList = new ArrayList<>();
        toList.add(approval.getRequesterId());
        
        // Find next step
        ApprovalStep nextStep = null;
        if (approval.getSteps() != null) {
            for (ApprovalStep s : approval.getSteps()) {
                if (s.getStepOrder() != null && step.getStepOrder() != null && 
                    s.getStepOrder() > step.getStepOrder()) {
                    if (nextStep == null || s.getStepOrder() < nextStep.getStepOrder()) {
                        nextStep = s;
                    }
                }
            }
        }
        
        if (nextStep != null && nextStep.getAssigneeId() != null) {
            toList.add(nextStep.getAssigneeId());
        }
        
        String subject = "Approval Step Approved: " + approval.getTargetType();
        String body = "<p>Your approval step was approved by " + step.getAssigneeName() + ".</p>";
        
        sendApprovalMessage(subject, body, toList, null, null, approval, step.getAssigneeId());
    }

    @Transactional
    public void onApprovalRejected(ApprovalRequest approval, ApprovalStep step) {
        List<String> toList = new ArrayList<>();
        toList.add(approval.getRequesterId());
        
        String subject = "Approval Request Rejected: " + approval.getTargetType();
        String body = "<p>Your approval request was rejected by " + step.getAssigneeName() + ".</p>" +
                      "<p>Reason: " + step.getComment() + "</p>";
                      
        sendApprovalMessage(subject, body, toList, null, null, approval, step.getAssigneeId());
    }

    @Transactional
    public void onApprovalCompleted(ApprovalRequest approval) {
        List<String> toList = new ArrayList<>();
        toList.add(approval.getRequesterId());
        
        List<String> ccList = parseObservers(approval.getObserverIds());
        
        String subject = "Approval Request Completed: " + approval.getTargetType();
        String body = "<p>The approval request has been fully completed.</p>";
                      
        sendApprovalMessage(subject, body, toList, ccList, null, approval, "SYSTEM");
    }
    
    private void sendApprovalMessage(String subject, String body, List<String> to, List<String> cc, List<String> bcc, ApprovalRequest approval, String senderId) {
        InboxMessageRequest request = InboxMessageRequest.builder()
                .subject(subject)
                .body(body)
                .importance("HIGH")
                .toRecipients(to != null ? to : new ArrayList<>())
                .ccRecipients(cc != null ? cc : new ArrayList<>())
                .bccRecipients(bcc != null ? bcc : new ArrayList<>())
                .isDraft(false)
                .build();
                
        var response = inboxService.sendMessage(request, senderId);
        
        messageRepository.findById(response.getId()).ifPresent(msg -> {
            msg.setMessageType("APPROVAL_NOTICE");
            msg.setRelatedApprovalId(approval.getId());
            messageRepository.save(msg);
        });
    }
}
