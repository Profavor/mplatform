package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorEnableRequest {
    private String secret;
    private String code;
    private String type; // "TOTP", "EMAIL"
}
