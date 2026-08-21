package com.classification.domain_system.service;

import com.classification.domain_system.dto.InboxFolderCountResponse;
import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.dto.InboxMessageResponse;
import com.classification.domain_system.entity.InboxAttachment;
import com.classification.domain_system.entity.InboxMessage;
import com.classification.domain_system.entity.InboxRecipient;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.InboxAttachmentRepository;
import com.classification.domain_system.repository.InboxMessageRepository;
import com.classification.domain_system.repository.InboxRecipientRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.mail.MailSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxService {

    private final InboxMessageRepository messageRepository;
    private final InboxRecipientRepository recipientRepository;
    private final InboxAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final MailSendService mailSendService;
    private final SseNotificationService sseNotificationService;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public boolean isEmailAddress(String str) {
        if (!StringUtils.hasText(str)) return false;
        return EMAIL_PATTERN.matcher(str).matches();
    }

    public String resolveUserName(String userId) {
        if (userId == null) return "Unknown";
        return userRepository.findById(userId)
                .map(User::getUsername)
                .or(() -> userRepository.findByUsername(userId).map(User::getUsername))
                .orElse(userId != null && userId.matches("^[0-9a-fA-F-]{36}$") ? "사용자" : userId);
    }

    @Transactional
    public InboxMessageResponse sendMessage(InboxMessageRequest request, String senderId) {
        return processMessage(request, senderId, false);
    }

    @Transactional
    public InboxMessageResponse saveDraft(InboxMessageRequest request, String senderId) {
        return processMessage(request, senderId, true);
    }

    private InboxMessageResponse processMessage(InboxMessageRequest request, String senderId, boolean forceDraft) {
        boolean isDraft = forceDraft || request.isDraft();
        
        InboxMessage msg = new InboxMessage();
        msg.setSenderId(senderId);
        String senderEmail = userRepository.findById(senderId)
                .map(User::getEmail)
                .or(() -> userRepository.findByUsername(senderId).map(User::getEmail))
                .orElse(mailSendService != null ? mailSendService.buildEmailAddress(senderId) : senderId + "@mplatform.com");
        msg.setSenderEmail(senderEmail);
        msg.setSubject(StringUtils.hasText(request.getSubject()) ? request.getSubject().trim() : "(제목 없음)");
        msg.setBody(request.getBody());
        msg.setImportance(StringUtils.hasText(request.getImportance()) ? request.getImportance() : "NORMAL");
        msg.setMessageType(StringUtils.hasText(request.getMessageType()) ? request.getMessageType() : "INTERNAL");
        msg.setRelatedApprovalId(request.getRelatedApprovalId());
        msg.setParentMessageId(request.getParentMessageId());
        msg.setIsDraft(isDraft);

        if (request.getParentMessageId() != null) {
            Optional<InboxMessage> parentOpt = messageRepository.findById(request.getParentMessageId());
            if (parentOpt.isPresent()) {
                InboxMessage parent = parentOpt.get();
                msg.setRootMessageId(parent.getRootMessageId() != null ? parent.getRootMessageId() : parent.getId());
            }
        }
        
        if (!isDraft) {
            msg.setSentAt(LocalDateTime.now());
        }

        // Process attachments if they exist (assuming they were uploaded and we just link them)
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<InboxAttachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            for (InboxAttachment att : attachments) {
                att.setMessage(msg);
                msg.addAttachment(att);
            }
        }

        // Sender recipient
        InboxRecipient senderRec = new InboxRecipient();
        senderRec.setMessage(msg);
        senderRec.setUserId(senderId);
        senderRec.setRecipientType("FROM");
        senderRec.setFolder(isDraft ? "DRAFT" : "SENT");
        senderRec.setIsRead(true);
        senderRec.setReadAt(LocalDateTime.now());
        msg.addRecipient(senderRec);

        List<InboxRecipient> externalRecipientsToNotify = new ArrayList<>();
        Set<String> internalUsersToNotify = new LinkedHashSet<>();

        if (!isDraft) {
            processRecipients(msg, request.getToRecipients(), "TO", externalRecipientsToNotify, internalUsersToNotify);
            processRecipients(msg, request.getCcRecipients(), "CC", externalRecipientsToNotify, internalUsersToNotify);
            processRecipients(msg, request.getBccRecipients(), "BCC", externalRecipientsToNotify, internalUsersToNotify);
        }

        msg = messageRepository.save(msg);

        // Send real emails to external addresses with 1x1 tracking pixel
        if (!externalRecipientsToNotify.isEmpty() && mailSendService != null) {
            for (InboxRecipient extRec : externalRecipientsToNotify) {
                try {
                    String body = msg.getBody() != null ? msg.getBody() : "";
                    String pixelTag = extRec.getId() != null
                            ? "<img src=\"/api/inbox/track/open/" + extRec.getId() + "\" width=\"1\" height=\"1\" style=\"display:none;\" alt=\"\" />"
                            : "";
                    String htmlBody = body + pixelTag;
                    mailSendService.sendSimpleMail(senderEmail, extRec.getEmail(), msg.getSubject(), htmlBody);
                } catch (Exception e) {
                    log.error("Failed to send email to " + extRec.getEmail(), e);
                }
            }
        }

        // Notify internal users via SSE & WebSocket
        if (!internalUsersToNotify.isEmpty()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", "INBOX_MESSAGE");
            payload.put("type", "NEW_MESSAGE");
            if (msg.getId() != null) {
                payload.put("messageId", msg.getId().toString());
            }
            payload.put("subject", msg.getSubject());
            payload.put("senderId", senderId);
            payload.put("senderName", resolveUserName(senderId));
            payload.put("senderEmail", msg.getSenderEmail());
            payload.put("folder", "INBOX");
            payload.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : LocalDateTime.now().toString());

            for (String uId : internalUsersToNotify) {
                try {
                    if (sseNotificationService != null) {
                        sseNotificationService.sendNotification(uId, payload);
                    }
                    if (webSocketPublisher != null) {
                        webSocketPublisher.publishNotification(uId, payload);
                    }
                } catch (Exception e) {
                    log.warn("Failed to send notification to user " + uId, e);
                }
            }
        }

        return toResponse(msg, senderRec, senderId);
    }

    private void processRecipients(InboxMessage msg, List<String> recipients, String type, 
                                   List<InboxRecipient> externalRecipients, Set<String> internalUsers) {
        if (recipients == null) return;
        
        for (String rec : recipients) {
            if (!StringUtils.hasText(rec)) continue;
            
            InboxRecipient recipient = new InboxRecipient();
            recipient.setMessage(msg);
            recipient.setRecipientType(type);
            recipient.setFolder("INBOX");
            
            if (isEmailAddress(rec)) {
                Optional<User> matchedUser = userRepository.findByEmail(rec);
                if (matchedUser.isPresent()) {
                    User u = matchedUser.get();
                    String finalUserId = (u != null && StringUtils.hasText(u.getId())) ? u.getId() : rec;
                    recipient.setUserId(finalUserId);
                    recipient.setEmail(u != null ? u.getEmail() : rec);
                    internalUsers.add(finalUserId);
                } else {
                    recipient.setEmail(rec);
                    externalRecipients.add(recipient);
                }
            } else {
                Optional<User> matchedUser = userRepository.findById(rec)
                        .or(() -> userRepository.findByUsername(rec));
                if (matchedUser.isPresent()) {
                    User u = matchedUser.get();
                    String finalUserId = (u != null && StringUtils.hasText(u.getId())) ? u.getId() : rec;
                    recipient.setUserId(finalUserId);
                    recipient.setEmail(u != null ? u.getEmail() : null);
                    internalUsers.add(finalUserId);
                } else {
                    recipient.setUserId(rec);
                    internalUsers.add(rec);
                }
            }
            msg.addRecipient(recipient);
        }
    }

    @Transactional
    public InboxMessageResponse updateDraft(UUID messageId, InboxMessageRequest request, String senderId) {
        InboxMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        
        if (!senderId.equals(msg.getSenderId()) || !msg.getIsDraft()) {
            throw new IllegalArgumentException("Cannot update this message");
        }
        
        msg.setSubject(request.getSubject() != null ? request.getSubject() : "");
        msg.setBody(request.getBody());
        msg.setImportance(StringUtils.hasText(request.getImportance()) ? request.getImportance() : "NORMAL");
        
        // Update attachments and recipients can be complex. In a real system we'd diff them.
        // For now, save basic fields.
        messageRepository.save(msg);
        
        InboxRecipient senderRec = recipientRepository.findFirstByUserIdAndMessageId(senderId, messageId)
                .orElse(null);
                
        return toResponse(msg, senderRec, senderId);
    }

    @Transactional(readOnly = true)
    public Page<InboxMessageResponse> getMessages(String userId, String folder, Pageable pageable, String keyword) {
        Page<InboxRecipient> recipientsPage;
        if ("STARRED".equalsIgnoreCase(folder)) {
            recipientsPage = recipientRepository.findByUserIdAndIsStarredTrueAndIsDeletedFalse(userId, pageable);
        } else if (StringUtils.hasText(keyword)) {
            recipientsPage = recipientRepository.searchByKeyword(userId, folder, keyword, pageable);
        } else {
            recipientsPage = recipientRepository.findByUserIdAndFolderAndIsDeletedFalse(userId, folder, pageable);
        }
        
        List<InboxMessageResponse> content = new ArrayList<>();
        Set<UUID> seenMessageIds = new HashSet<>();
        for (InboxRecipient rec : recipientsPage.getContent()) {
            if (rec != null && rec.getMessage() != null && seenMessageIds.add(rec.getMessage().getId())) {
                content.add(toResponse(rec.getMessage(), rec, userId));
            }
        }
        
        return new PageImpl<>(content, pageable, recipientsPage.getTotalElements());
    }

    @Transactional
    public InboxMessageResponse getMessage(String userId, UUID messageId) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("Message not found or access denied");
        }
        
        InboxRecipient recipient = recipients.stream()
                .filter(r -> !"FROM".equals(r.getRecipientType()) && !r.getIsDeleted())
                .findFirst()
                .orElse(recipients.get(0));
        
        for (InboxRecipient r : recipients) {
            if (!r.getIsRead()) {
                r.setIsRead(true);
                r.setReadAt(LocalDateTime.now());
                recipientRepository.save(r);
            }
        }
        
        return toResponse(recipient.getMessage(), recipient, userId);
    }

    @Transactional
    public InboxMessageResponse replyMessage(String userId, UUID messageId, InboxMessageRequest request) {
        InboxMessage original = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Original message not found"));
                
        request.setParentMessageId(original.getId());
        // set TO as original sender
        request.setToRecipients(List.of(original.getSenderId() != null ? original.getSenderId() : original.getSenderEmail()));
        
        return sendMessage(request, userId);
    }

    @Transactional
    public InboxMessageResponse replyAllMessage(String userId, UUID messageId, InboxMessageRequest request) {
        InboxMessage original = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Original message not found"));
                
        request.setParentMessageId(original.getId());
        
        Set<String> toSet = new HashSet<>();
        if (original.getSenderId() != null) toSet.add(original.getSenderId());
        else if (original.getSenderEmail() != null) toSet.add(original.getSenderEmail());
        
        Set<String> ccSet = new HashSet<>();
        
        for (InboxRecipient rec : original.getRecipients()) {
            if ("TO".equals(rec.getRecipientType())) {
                toSet.add(rec.getUserId() != null ? rec.getUserId() : rec.getEmail());
            } else if ("CC".equals(rec.getRecipientType())) {
                ccSet.add(rec.getUserId() != null ? rec.getUserId() : rec.getEmail());
            }
        }
        
        toSet.remove(userId); // don't reply to self
        ccSet.remove(userId);
        
        request.setToRecipients(new ArrayList<>(toSet));
        request.setCcRecipients(new ArrayList<>(ccSet));
        
        return sendMessage(request, userId);
    }

    @Transactional
    public InboxMessageResponse forwardMessage(String userId, UUID messageId, InboxMessageRequest request) {
        InboxMessage original = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Original message not found"));
                
        request.setParentMessageId(original.getId());
        // For attachments, we would need to duplicate them or link them.
        List<UUID> attachmentIds = original.getAttachments().stream().map(InboxAttachment::getId).collect(Collectors.toList());
        request.setAttachmentIds(attachmentIds);
        
        return sendMessage(request, userId);
    }

    @Transactional
    public void markAsRead(String userId, UUID messageId) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        for (InboxRecipient recipient : recipients) {
            if (!recipient.getIsRead()) {
                recipient.setIsRead(true);
                recipient.setReadAt(LocalDateTime.now());
                recipientRepository.save(recipient);
            }
        }
    }

    @Transactional
    public void markAsUnread(String userId, UUID messageId) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        for (InboxRecipient recipient : recipients) {
            if (recipient.getIsRead()) {
                recipient.setIsRead(false);
                recipient.setReadAt(null);
                recipientRepository.save(recipient);
            }
        }
    }

    @Transactional
    public void bulkMarkAsRead(String userId, List<UUID> messageIds) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageIdIn(userId, messageIds);
        for (InboxRecipient rec : recipients) {
            if (!rec.getIsRead()) {
                rec.setIsRead(true);
                rec.setReadAt(LocalDateTime.now());
            }
        }
        recipientRepository.saveAll(recipients);
    }

    @Transactional
    public void toggleStar(String userId, UUID messageId) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        for (InboxRecipient recipient : recipients) {
            recipient.setIsStarred(!recipient.getIsStarred());
            recipientRepository.save(recipient);
        }
    }

    @Transactional
    public void moveToFolder(String userId, UUID messageId, String folder) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        if (recipients.isEmpty()) return;
        if (recipients.size() == 1) {
            recipients.get(0).setFolder(folder);
            recipientRepository.save(recipients.get(0));
            return;
        }
        for (InboxRecipient recipient : recipients) {
            if ("TRASH".equalsIgnoreCase(folder) || !"FROM".equalsIgnoreCase(recipient.getRecipientType())) {
                recipient.setFolder(folder);
                recipientRepository.save(recipient);
            }
        }
    }

    @Transactional
    public void moveToTrash(String userId, UUID messageId) {
        moveToFolder(userId, messageId, "TRASH");
    }

    @Transactional
    public void bulkMoveToTrash(String userId, List<UUID> messageIds) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageIdIn(userId, messageIds);
        for (InboxRecipient rec : recipients) {
            rec.setFolder("TRASH");
        }
        recipientRepository.saveAll(recipients);
    }

    @Transactional
    public void permanentDelete(String userId, UUID messageId) {
        List<InboxRecipient> recipients = recipientRepository.findByUserIdAndMessageId(userId, messageId);
        for (InboxRecipient recipient : recipients) {
            if ("TRASH".equals(recipient.getFolder())) {
                recipient.setIsDeleted(true);
                recipientRepository.save(recipient);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<InboxFolderCountResponse> getFolderCounts(String userId) {
        List<String> folders = List.of("INBOX", "SENT", "DRAFT", "STARRED", "ARCHIVE", "TRASH");
        return folders.stream().map(folder -> {
            if ("STARRED".equalsIgnoreCase(folder)) {
                long total = recipientRepository.findByUserIdAndIsStarredTrueAndIsDeletedFalse(userId, Pageable.unpaged()).getTotalElements();
                long unread = recipientRepository.countByUserIdAndIsStarredTrueAndIsReadFalseAndIsDeletedFalse(userId);
                return new InboxFolderCountResponse(folder, total, unread);
            }
            long total = recipientRepository.findByUserIdAndFolderAndIsDeletedFalse(userId, folder, Pageable.unpaged()).getTotalElements();
            long unread = recipientRepository.countByUserIdAndFolderAndIsReadFalseAndIsDeletedFalse(userId, folder);
            return new InboxFolderCountResponse(folder, total, unread);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return recipientRepository.countByUserIdAndFolderAndIsReadFalseAndIsDeletedFalse(userId, "INBOX");
    }

    @Transactional(readOnly = true)
    public List<InboxMessageResponse> getThread(String userId, UUID messageId) {
        InboxMessage msg = messageRepository.findById(messageId).orElse(null);
        if (msg == null) return Collections.emptyList();
        
        UUID rootId = msg.getRootMessageId() != null ? msg.getRootMessageId() : msg.getId();
        List<InboxMessage> thread = messageRepository.findByRootMessageIdOrderByCreatedAtAsc(rootId);
        if (thread.isEmpty()) {
            thread = List.of(msg); // fallback
        }
        
        return thread.stream().map(m -> {
            InboxRecipient rec = recipientRepository.findFirstByUserIdAndMessageId(userId, m.getId()).orElse(null);
            return toResponse(m, rec, userId);
        }).collect(Collectors.toList());
    }

    private InboxMessageResponse toResponse(InboxMessage msg, InboxRecipient viewerRecipient, String viewerUserId) {
        InboxMessageResponse res = new InboxMessageResponse();
        res.setId(msg.getId());
        res.setSenderId(msg.getSenderId());
        res.setSenderName(resolveUserName(msg.getSenderId()));
        res.setSenderEmail(msg.getSenderEmail());
        res.setSubject(msg.getSubject());
        res.setBody(msg.getBody());
        res.setImportance(msg.getImportance());
        res.setMessageType(msg.getMessageType() != null ? msg.getMessageType() : "INTERNAL");
        res.setParentMessageId(msg.getParentMessageId());
        res.setRootMessageId(msg.getRootMessageId() != null ? msg.getRootMessageId() : msg.getId());
        res.setRelatedApprovalId(msg.getRelatedApprovalId());
        res.setDraft(Boolean.TRUE.equals(msg.getIsDraft()));
        res.setSentAt(msg.getSentAt());
        res.setCreatedAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now());
        
        if (viewerRecipient != null) {
            res.setRecipientId(viewerRecipient.getId());
            res.setRead(Boolean.TRUE.equals(viewerRecipient.getIsRead()));
            res.setStarred(Boolean.TRUE.equals(viewerRecipient.getIsStarred()));
            res.setFolder(viewerRecipient.getFolder());
        } else {
            res.setFolder(Boolean.TRUE.equals(msg.getIsDraft()) ? "DRAFT" : "SENT");
        }
        
        List<InboxAttachment> attachments = msg.getAttachments();
        if (attachments != null) {
            res.setHasAttachments(!attachments.isEmpty());
            res.setAttachmentCount(attachments.size());
            res.setAttachments(attachments.stream().map(a -> {
                InboxMessageResponse.AttachmentInfo info = new InboxMessageResponse.AttachmentInfo();
                info.setId(a.getId());
                info.setFileName(a.getFileName());
                info.setFileSize(a.getFileSize() != null ? a.getFileSize() : 0);
                info.setContentType(a.getContentType());
                return info;
            }).collect(Collectors.toList()));
        }
        
        List<InboxRecipient> recipients = msg.getRecipients();
        if (recipients != null) {
            res.setRecipientCount(recipients.size());
            
            List<InboxMessageResponse.RecipientInfo> toList = new ArrayList<>();
            List<InboxMessageResponse.RecipientInfo> ccList = new ArrayList<>();
            
            for (InboxRecipient r : recipients) {
                // hide BCC unless sender
                if ("BCC".equals(r.getRecipientType()) && (msg.getSenderId() == null || !msg.getSenderId().equals(viewerUserId))) {
                    continue;
                }
                
                InboxMessageResponse.RecipientInfo info = new InboxMessageResponse.RecipientInfo();
                info.setUserId(r.getUserId());
                info.setEmail(r.getEmail());
                info.setRecipientType(r.getRecipientType());
                info.setName(r.getUserId() != null ? resolveUserName(r.getUserId()) : r.getEmail());
                info.setRead(Boolean.TRUE.equals(r.getIsRead()));
                info.setReadAt(r.getReadAt());
                info.setRecalled(Boolean.TRUE.equals(r.getIsRecalled()));
                info.setRecalledAt(r.getRecalledAt());
                
                if ("TO".equals(r.getRecipientType())) toList.add(info);
                else if ("CC".equals(r.getRecipientType())) ccList.add(info);
                // We might want to add BCC to one of the lists or a separate one if needed.
            }
            res.setToRecipients(toList);
            res.setCcRecipients(ccList);
        }
        
        if (msg.getRootMessageId() != null) {
            res.setThreadCount((int) messageRepository.countByRootMessageId(msg.getRootMessageId()));
        } else {
            res.setThreadCount(1);
        }
        
        return res;
    }

    @Transactional
    public InboxMessageResponse.RecallResultResponse recallMessage(String senderId, UUID messageId) {
        InboxMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (msg.getSenderId() == null || !msg.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("Only the sender can recall this message");
        }

        List<InboxRecipient> recipients = msg.getRecipients();
        List<InboxMessageResponse.RecallResultResponse.RecipientRecallDetail> details = new ArrayList<>();
        int beforeReadCount = 0;
        int afterReadCount = 0;
        int externalCount = 0;
        LocalDateTime now = LocalDateTime.now();

        if (recipients != null) {
            for (InboxRecipient r : recipients) {
                // Ignore sender's own outbound record (FROM)
                if ("FROM".equals(r.getRecipientType())) {
                    continue;
                }

                boolean isExternal = r.getUserId() == null && StringUtils.hasText(r.getEmail());
                if (isExternal) {
                    externalCount++;
                    details.add(InboxMessageResponse.RecallResultResponse.RecipientRecallDetail.builder()
                            .userId(null)
                            .name(r.getEmail())
                            .email(r.getEmail())
                            .recipientType(r.getRecipientType())
                            .wasRead(Boolean.TRUE.equals(r.getIsRead()))
                            .readAt(r.getReadAt())
                            .isRecalled(false)
                            .status("EXTERNAL_UNRECALLABLE")
                            .build());
                } else {
                    boolean wasRead = Boolean.TRUE.equals(r.getIsRead());
                    if (wasRead) {
                        afterReadCount++;
                    } else {
                        beforeReadCount++;
                    }

                    r.setIsRecalled(true);
                    r.setRecalledAt(now);
                    r.setIsDeleted(true);
                    r.setFolder("TRASH");
                    recipientRepository.save(r);

                    details.add(InboxMessageResponse.RecallResultResponse.RecipientRecallDetail.builder()
                            .userId(r.getUserId())
                            .name(resolveUserName(r.getUserId()))
                            .email(r.getEmail())
                            .recipientType(r.getRecipientType())
                            .wasRead(wasRead)
                            .readAt(r.getReadAt())
                            .isRecalled(true)
                            .status(wasRead ? "RECALLED_AFTER_READ" : "RECALLED_BEFORE_READ")
                            .build());
                }
            }
        }

        return InboxMessageResponse.RecallResultResponse.builder()
                .messageId(messageId)
                .totalRecipients(beforeReadCount + afterReadCount + externalCount)
                .recalledBeforeReadCount(beforeReadCount)
                .recalledAfterReadCount(afterReadCount)
                .externalCount(externalCount)
                .details(details)
                .build();
    }

    @Transactional
    public void trackEmailOpen(UUID recipientId) {
        if (recipientId == null) return;
        recipientRepository.findById(recipientId).ifPresent(r -> {
            if (!Boolean.TRUE.equals(r.getIsRead())) {
                r.setIsRead(true);
                r.setReadAt(LocalDateTime.now());
                recipientRepository.save(r);
                log.info("Email open tracked for recipient {} ({})", r.getId(), r.getEmail());
            }
        });
    }
}
