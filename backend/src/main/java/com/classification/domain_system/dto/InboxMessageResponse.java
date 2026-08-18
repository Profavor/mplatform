package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxMessageResponse {
    private UUID id;
    private UUID recipientId;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String subject;
    private String body;
    private String importance;
    private String messageType;
    private UUID parentMessageId;
    private UUID rootMessageId;
    private UUID relatedApprovalId;
    
    @JsonProperty("isDraft")
    private boolean isDraft;
    
    @JsonProperty("isRead")
    private boolean isRead;
    
    @JsonProperty("isStarred")
    private boolean isStarred;
    
    private String folder;
    private boolean hasAttachments;
    private int attachmentCount;
    private int recipientCount;
    private int threadCount;
    private List<RecipientInfo> toRecipients;
    private List<RecipientInfo> ccRecipients;
    private List<AttachmentInfo> attachments;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipientInfo {
        private String userId;
        private String name;
        private String email;
        private String recipientType;
        
        @JsonProperty("isRead")
        private boolean isRead;
        
        private LocalDateTime readAt;
        
        @JsonProperty("isRecalled")
        private boolean isRecalled;
        
        private LocalDateTime recalledAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentInfo {
        private UUID id;
        private String fileName;
        private long fileSize;
        private String contentType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecallResultResponse {
        private UUID messageId;
        private int totalRecipients;
        private int recalledBeforeReadCount;
        private int recalledAfterReadCount;
        private int externalCount;
        private List<RecipientRecallDetail> details;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class RecipientRecallDetail {
            private String userId;
            private String name;
            private String email;
            private String recipientType;
            
            @JsonProperty("wasRead")
            private boolean wasRead;
            
            private LocalDateTime readAt;
            
            @JsonProperty("isRecalled")
            private boolean isRecalled;
            
            private String status; // "RECALLED_BEFORE_READ", "RECALLED_AFTER_READ", "EXTERNAL_UNRECALLABLE"
        }
    }
}
