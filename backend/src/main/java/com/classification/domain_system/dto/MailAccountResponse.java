package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailAccountResponse {
    private String email;
    private String userId;
    private String userName;
    private String quotaUsed;
    private String quotaLimit;
    private boolean isActive;
}
