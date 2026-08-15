package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDelegationDto {
    private UUID id;
    private String delegatorUserId;
    private String delegatorUserName;
    private String delegateeUserId;
    private String delegateeUserName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
