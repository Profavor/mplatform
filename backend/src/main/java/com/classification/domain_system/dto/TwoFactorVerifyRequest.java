package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequest {
    private String username;
    private String tempToken; // 1차 로그인 성공 후 발급된 5분 임시 토큰
    private String code;      // 6자리 TOTP/이메일 코드 또는 8자리 백업 코드
    private String type;      // "TOTP", "EMAIL", "BACKUP_CODE"
}
