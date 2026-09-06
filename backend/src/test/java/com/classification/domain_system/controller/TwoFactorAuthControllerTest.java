package com.classification.domain_system.controller;

import com.classification.domain_system.dto.TwoFactorEnableRequest;
import com.classification.domain_system.dto.TwoFactorVerifyRequest;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.TwoFactorAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TwoFactorAuthController twoFactorAuthController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(twoFactorAuthController)
                .setControllerAdvice(new com.classification.domain_system.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("2FA 설정 준비 API (POST /api/auth/2fa/setup) 호출 시 시크릿과 otpauth URL이 반환된다")
    void setupTwoFactor_success() throws Exception {
        when(twoFactorAuthService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");
        when(twoFactorAuthService.getOtpAuthUrl(anyString(), anyString())).thenReturn("otpauth://totp/MDM%20Platform:testuser?secret=JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP&issuer=MDM%20Platform");

        mockMvc.perform(post("/api/auth/2fa/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret").value("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP"))
                .andExpect(jsonPath("$.otpAuthUrl").value("otpauth://totp/MDM%20Platform:testuser?secret=JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP&issuer=MDM%20Platform"))
                .andExpect(jsonPath("$.qrCodeUrl").isNotEmpty());
    }

    @Test
    @DisplayName("2FA 활성화 API (POST /api/auth/2fa/enable) 호출 시 확인 코드가 맞으면 백업 코드 10개가 반환된다")
    void enableTwoFactor_success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(twoFactorAuthService.verifyTotpCode(eq("TESTSECRET"), eq("123456"))).thenReturn(true);
        when(twoFactorAuthService.generateBackupCodes(10)).thenReturn(List.of("CODE1234", "CODE5678"));
        when(twoFactorAuthService.hashAndStoreBackupCodes(anyList())).thenReturn("HASH1,HASH2");

        TwoFactorEnableRequest request = new TwoFactorEnableRequest("TESTSECRET", "123456", "TOTP");

        mockMvc.perform(post("/api/auth/2fa/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", "testuser")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.backupCodes").isArray())
                .andExpect(jsonPath("$.backupCodes[0]").value("CODE1234"));

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("로그인 2차 검증 API (POST /api/auth/2fa/verify) 호출 시 TOTP 코드가 유효하면 최종 JWT 토큰이 발급된다")
    void verifyLogin_totp_success() throws Exception {
        User user = new User();
        user.setId("user-uuid");
        user.setUsername("testuser");
        user.setRole("ROLE_ADMIN");
        user.setTwoFactorSecret("TESTSECRET");
        user.setTwoFactorEnabled(true);

        when(authService.validateTempToken("VALID_TEMP_TOKEN", "testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(twoFactorAuthService.verifyTotpCode("TESTSECRET", "123456")).thenReturn(true);
        when(authService.issueFinalTokensAfter2Fa(eq(user), any(), any(), eq("TOTP")))
                .thenReturn(Map.of("token", "FINAL_ACCESS_TOKEN", "refreshToken", "FINAL_REFRESH_TOKEN"));

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest("testuser", "VALID_TEMP_TOKEN", "123456", "TOTP");

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("FINAL_ACCESS_TOKEN"))
                .andExpect(jsonPath("$.refreshToken").value("FINAL_REFRESH_TOKEN"));

        verify(authService).consumeTempToken("VALID_TEMP_TOKEN");
    }

    @Test
    @DisplayName("로그인 2차 검증 API - 백업 코드로 검증 성공 시 해당 백업 코드가 소진되고 토큰이 발급된다")
    void verifyLogin_backupCode_success() throws Exception {
        User user = new User();
        user.setId("user-uuid");
        user.setUsername("testuser");
        user.setRole("ROLE_ADMIN");
        user.setBackupCodes("HASH1,HASH2");
        user.setTwoFactorEnabled(true);

        when(authService.validateTempToken("VALID_TEMP_TOKEN", "testuser")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(twoFactorAuthService.verifyAndConsumeBackupCode(user, "BACKUP123")).thenReturn(true);
        when(authService.issueFinalTokensAfter2Fa(eq(user), any(), any(), eq("BACKUP_CODE")))
                .thenReturn(Map.of("token", "FINAL_ACCESS_TOKEN", "refreshToken", "FINAL_REFRESH_TOKEN"));

        TwoFactorVerifyRequest request = new TwoFactorVerifyRequest("testuser", "VALID_TEMP_TOKEN", "BACKUP123", "BACKUP_CODE");

        mockMvc.perform(post("/api/auth/2fa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("FINAL_ACCESS_TOKEN"));

        verify(authService).consumeTempToken("VALID_TEMP_TOKEN");
    }

    @Test
    @DisplayName("이메일 OTP 전송 API (POST /api/auth/2fa/send-email) 호출 시 이메일로 OTP가 발송된다")
    void sendEmailOtp_success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(authService.maskEmail("testuser@example.com")).thenReturn("te***@example.com");

        mockMvc.perform(post("/api/auth/2fa/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.maskedEmail").value("te***@example.com"));

        verify(twoFactorAuthService).sendEmailOtp(user);
    }

    @Test
    @DisplayName("2FA 상태 조회 API (GET /api/auth/2fa/status) 호출 시 활성화 여부 및 유예기간이 반환된다")
    void getStatus_success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setRole("ROLE_ADMIN");
        user.setTwoFactorEnabled(true);
        user.setTwoFactorType("TOTP");
        user.setBackupCodes("HASH1");
        user.setEmail("admin@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(twoFactorAuthService.isTwoFactorRequired(user)).thenReturn(true);
        when(authService.maskEmail("admin@example.com")).thenReturn("ad***@example.com");

        mockMvc.perform(get("/api/auth/2fa/status")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                .andExpect(jsonPath("$.twoFactorType").value("TOTP"))
                .andExpect(jsonPath("$.hasBackupCodes").value(true))
                .andExpect(jsonPath("$.mandatory").value(true))
                .andExpect(jsonPath("$.maskedEmail").value("ad***@example.com"));
    }

    @Test
    @DisplayName("2FA 비활성화 API (POST /api/auth/2fa/disable) 호출 시 설정이 해제된다")
    void disable_success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret("SECRET");
        user.setBackupCodes("HASHES");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/2fa/disable")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userRepository).save(user);
    }
}
