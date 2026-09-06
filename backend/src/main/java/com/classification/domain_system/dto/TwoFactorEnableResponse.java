package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorEnableResponse {
    private boolean success;
    private List<String> backupCodes; // 평문 10개, 최초 1회만 반환
    private String message;
}
