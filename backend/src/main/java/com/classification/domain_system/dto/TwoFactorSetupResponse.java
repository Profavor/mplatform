package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponse {
    private String secret;
    private String otpAuthUrl;
    private String qrCodeUrl;
}
