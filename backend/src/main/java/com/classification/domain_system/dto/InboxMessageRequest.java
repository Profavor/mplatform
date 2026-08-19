package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboxMessageRequest {
    private String subject;
    private String body;
    private String importance;
    private List<String> toRecipients;
    private List<String> ccRecipients;
    private List<String> bccRecipients;
    private List<UUID> attachmentIds;
    private UUID parentMessageId;
    private String messageType;
    private UUID relatedApprovalId;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isDraft")
    private Boolean isDraft;

    public boolean isDraft() {
        return Boolean.TRUE.equals(this.isDraft);
    }

    public Boolean getIsDraft() {
        return this.isDraft;
    }

    @com.fasterxml.jackson.annotation.JsonSetter("draft")
    public void setDraftAlias(Boolean draft) {
        this.isDraft = draft;
    }
}
