package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String companyName;
    private Boolean termsAgreed;
    private String timezone;
}
