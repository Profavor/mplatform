package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.mail.MailSendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailSendService mailSendService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private TwoFactorAuthService twoFactorAuthService;

    @BeforeEach
    void setUp() {
        twoFactorAuthService = new TwoFactorAuthService(userRepository, mailSendService, passwordEncoder);
    }

    @Test
    @DisplayName("TOTP 시크릿 키는 32자의 Base32 문자열로 정상 생성된다")
    void generateSecret_shouldReturnValidBase32String() {
        String secret = twoFactorAuthService.generateSecret();

        assertThat(secret).isNotNull();
        assertThat(secret).hasSize(32);
        // Base32 RFC 4648 characters: A-Z, 2-7
        assertThat(secret).matches("^[A-Z2-7]+$");
    }

    @Test
    @DisplayName("OTP Auth URL이 올바른 규격(otpauth://totp/...)으로 생성된다")
    void getOtpAuthUrl_shouldReturnValidUrl() {
        String secret = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP";
        String username = "admin@mplatform.com";

        String otpAuthUrl = twoFactorAuthService.getOtpAuthUrl(username, secret);

        assertThat(otpAuthUrl).startsWith("otpauth://totp/");
        assertThat(otpAuthUrl).contains("secret=" + secret);
        assertThat(otpAuthUrl).contains("issuer=MDM%20Platform");
        assertThat(otpAuthUrl).contains(username);
    }

    @Test
    @DisplayName("생성된 TOTP 시크릿 기반으로 유효한 6자리 코드를 생성하고 검증을 통과한다")
    void verifyTotpCode_success() {
        String secret = twoFactorAuthService.generateSecret();
        long currentWindow = System.currentTimeMillis() / 1000L / 30L;
        String currentCode = twoFactorAuthService.generateTotpCodeForWindow(secret, currentWindow);

        boolean isValid = twoFactorAuthService.verifyTotpCode(secret, currentCode);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("시간차(±1 step, 30초 이전/이후) 내의 TOTP 코드도 허용된다")
    void verifyTotpCode_allowDriftWindow() {
        String secret = twoFactorAuthService.generateSecret();
        long currentWindow = System.currentTimeMillis() / 1000L / 30L;
        String pastCode = twoFactorAuthService.generateTotpCodeForWindow(secret, currentWindow - 1);
        String futureCode = twoFactorAuthService.generateTotpCodeForWindow(secret, currentWindow + 1);

        assertThat(twoFactorAuthService.verifyTotpCode(secret, pastCode)).isTrue();
        assertThat(twoFactorAuthService.verifyTotpCode(secret, futureCode)).isTrue();
    }

    @Test
    @DisplayName("잘못된 6자리 TOTP 코드는 검증 실패 처리된다")
    void verifyTotpCode_invalidCode() {
        String secret = twoFactorAuthService.generateSecret();

        boolean isValid = twoFactorAuthService.verifyTotpCode(secret, "000000");

        // 000000이 우연히 맞을 확률은 극도로 낮으므로 통상 false
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("1회용 백업 코드 10개가 고유한 8자리 영숫자로 정상 생성된다")
    void generateBackupCodes_shouldReturnUniqueCodes() {
        List<String> codes = twoFactorAuthService.generateBackupCodes(10);

        assertThat(codes).hasSize(10);
        assertThat(codes).doesNotHaveDuplicates();
        for (String code : codes) {
            assertThat(code).hasSize(8);
            assertThat(code).matches("^[A-Z0-9]+$");
        }
    }

    @Test
    @DisplayName("백업 코드로 인증 성공 시 1회 사용 처리되고 DB에서 차감 소진된다")
    void verifyAndConsumeBackupCode_successAndConsume() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("steward");

        List<String> plainCodes = twoFactorAuthService.generateBackupCodes(3);
        String hashedCodes = twoFactorAuthService.hashAndStoreBackupCodes(plainCodes);
        user.setBackupCodes(hashedCodes);

        // 첫 번째 백업 코드로 검증
        String codeToUse = plainCodes.get(0);
        boolean verified = twoFactorAuthService.verifyAndConsumeBackupCode(user, codeToUse);

        assertThat(verified).isTrue();
        // 저장 호출 검증
        verify(userRepository, times(1)).save(user);

        // 이미 사용한 코드로 재시도 시 실패해야 함
        boolean reused = twoFactorAuthService.verifyAndConsumeBackupCode(user, codeToUse);
        assertThat(reused).isFalse();
    }

    @Test
    @DisplayName("이메일 OTP 생성 및 발송, 5분 유효기간 내 정상 검증 및 1회 소비 검증")
    void emailOtp_flowSuccess() throws Exception {
        User user = new User();
        user.setUsername("developer");
        user.setEmail("dev@mplatform.com");

        String sentCode = twoFactorAuthService.sendEmailOtp(user);
        assertThat(sentCode).hasSize(6);
        assertThat(sentCode).matches("^[0-9]{6}$");

        // 이메일 발송 호출 확인
        verify(mailSendService, times(1)).sendMail(any(), eq(List.of("dev@mplatform.com")), any(), any(), any(), any(), any());

        // 검증 성공 확인
        boolean isValid = twoFactorAuthService.verifyEmailOtp("developer", sentCode);
        assertThat(isValid).isTrue();

        // 1회 소비 후 재사용 불가 확인
        boolean isReused = twoFactorAuthService.verifyEmailOtp("developer", sentCode);
        assertThat(isReused).isFalse();
    }

    @Test
    @DisplayName("역할별 2FA 필수 정책: 관리자 역할은 7일 유예기간 경과 후 강제 필수, 일반 사용자는 활성화 시 필수")
    void isTwoFactorRequired_policyCheck() {
        // 1. 관리자(ADMIN) - 7일 유예기간 내: 필수 아님 (스킵 가능)
        User adminInGrace = new User();
        adminInGrace.setRole("ROLE_ADMIN");
        adminInGrace.setTwoFactorEnabled(false);
        adminInGrace.setTwoFactorGraceUntil(LocalDateTime.now().plusDays(3));
        assertThat(twoFactorAuthService.isTwoFactorRequired(adminInGrace)).isFalse();

        // 2. 관리자(ADMIN) - 7일 유예기간 경과: 무조건 필수
        User adminGraceExpired = new User();
        adminGraceExpired.setRole("ROLE_ADMIN");
        adminGraceExpired.setTwoFactorEnabled(false);
        adminGraceExpired.setTwoFactorGraceUntil(LocalDateTime.now().minusDays(1));
        assertThat(twoFactorAuthService.isTwoFactorRequired(adminGraceExpired)).isTrue();

        // 3. 일반 사용자(ROLE_USER) - 2FA 활성화 안 함: 필수 아님
        User normalUser = new User();
        normalUser.setRole("ROLE_USER");
        normalUser.setTwoFactorEnabled(false);
        assertThat(twoFactorAuthService.isTwoFactorRequired(normalUser)).isFalse();

        // 4. 일반 사용자(ROLE_USER) - 2FA 활성화 함: 필수
        User normalUserEnabled = new User();
        normalUserEnabled.setRole("ROLE_USER");
        normalUserEnabled.setTwoFactorEnabled(true);
        assertThat(twoFactorAuthService.isTwoFactorRequired(normalUserEnabled)).isTrue();
    }
}
