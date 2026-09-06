package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorStatusResponse {
    private boolean twoFactorEnabled;
    private String twoFactorType;
    private boolean mandatory;
    private LocalDateTime graceUntil;
    private int remainingBackupCodesCount;
    private String maskedEmail;
    private String role;
    private Long gracePeriodRemainingDays;
    private boolean hasBackupCodes;
}
