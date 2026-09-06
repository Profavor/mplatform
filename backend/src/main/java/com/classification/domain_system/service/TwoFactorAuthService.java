package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.mail.MailSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2단계 인증(2FA/TOTP/이메일 OTP) 코어 서비스 (RFC 6238 TOTP 표준 구현)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTE_LENGTH = 20; // 160 bits
    private static final int TIME_STEP_SECONDS = 30;
    private static final int OTP_DIGITS = 6;
    private static final int EMAIL_OTP_TTL_MINUTES = 5;

    private final UserRepository userRepository;
    private final MailSendService mailSendService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    // 이메일 OTP 인메모리 캐시 (username -> EmailOtpEntry)
    private final Map<String, EmailOtpEntry> emailOtpCache = new ConcurrentHashMap<>();

    private record EmailOtpEntry(String code, LocalDateTime expiresAt) {}

    /**
     * RFC 4648 표준 Base32 시크릿 키 생성 (160 bits / 32 글자)
     */
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * Google Authenticator 등 OTP 앱 등록용 표준 URL 생성
     */
    public String getOtpAuthUrl(String username, String secret) {
        String issuer = "MDM Platform";
        try {
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    encodedIssuer, username, secret, encodedIssuer);
        } catch (Exception e) {
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    issuer, username, secret, issuer);
        }
    }

    /**
     * 현재 시간 기준 및 시간차(±1 step, 30초 이전/이후) 내의 TOTP 코드 검증
     */
    public boolean verifyTotpCode(String secret, String code) {
        if (secret == null || code == null || code.trim().length() != OTP_DIGITS) {
            return false;
        }

        try {
            long currentWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;

            // ±1 step 허용 (시간 동기화 오차 대응)
            for (int offset = -1; offset <= 1; offset++) {
                String generatedCode = generateTotpCodeForWindow(secret, currentWindow + offset);
                if (MessageDigest.isEqual(generatedCode.getBytes(StandardCharsets.UTF_8), code.trim().getBytes(StandardCharsets.UTF_8))) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
        }
        return false;
    }

    /**
     * 특정 시간 윈도우에 대한 TOTP 6자리 코드 계산 (RFC 6238 HMAC-SHA1)
     */
    public String generateTotpCodeForWindow(String secret, long window) {
        try {
            byte[] keyBytes = decodeBase32(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(window).array();

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            // Dynamic Truncation (RFC 4226)
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    /**
     * 1회용 백업 복구 코드 N개 생성 (8자리 대문자/숫자)
     */
    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 혼동 문자(0, O, 1, I) 제외

        while (codes.size() < count) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
            }
            String code = sb.toString();
            if (!codes.contains(code)) {
                codes.add(code);
            }
        }
        return codes;
    }

    /**
     * 백업 코드들을 BCrypt 해시하여 DB 저장용 문자열(콤마 구분)로 변환
     */
    public String hashAndStoreBackupCodes(List<String> plainCodes) {
        if (plainCodes == null || plainCodes.isEmpty()) {
            return "";
        }
        List<String> hashedList = new ArrayList<>();
        for (String code : plainCodes) {
            hashedList.add(passwordEncoder.encode(code.trim().toUpperCase()));
        }
        return String.join(",", hashedList);
    }

    /**
     * 백업 코드로 1회 로그인 검증 및 소진 (사용한 코드는 DB에서 즉시 제거)
     */
    @Transactional
    public boolean verifyAndConsumeBackupCode(User user, String inputCode) {
        if (user == null || user.getBackupCodes() == null || inputCode == null) {
            return false;
        }

        String raw = user.getBackupCodes().trim();
        if (raw.isEmpty()) {
            return false;
        }

        List<String> hashedCodes = new ArrayList<>(Arrays.asList(raw.split(",")));
        String normalizedInput = inputCode.trim().toUpperCase();

        for (int i = 0; i < hashedCodes.size(); i++) {
            String hashed = hashedCodes.get(i).trim();
            if (passwordEncoder.matches(normalizedInput, hashed)) {
                // 일치하는 백업 코드 소진 (제거)
                hashedCodes.remove(i);
                user.setBackupCodes(String.join(",", hashedCodes));
                userRepository.save(user);
                log.info("Backup code consumed successfully for user: {}, remaining codes: {}", user.getUsername(), hashedCodes.size());
                return true;
            }
        }
        return false;
    }

    /**
     * 이메일 6자리 OTP 생성, 캐싱(5분) 및 발송
     */
    public String sendEmailOtp(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required for Email OTP");
        }

        int randomNum = 100000 + secureRandom.nextInt(900000);
        String code = String.valueOf(randomNum);

        // 5분 만료 캐싱
        emailOtpCache.put(user.getUsername(), new EmailOtpEntry(code, LocalDateTime.now().plusMinutes(EMAIL_OTP_TTL_MINUTES)));

        String subject = "[MDM Platform] 로그인 2차 인증번호 안내 (" + code + ")";
        String htmlBody = String.format("""
            <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 520px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px;">
                <h2 style="color: #1e293b; margin-bottom: 8px;">로그인 2차 인증 안내</h2>
                <p style="color: #64748b; font-size: 14px; margin-top: 0;">귀하의 계정 로그인을 위한 2차 인증코드입니다. 5분 이내에 입력해주세요.</p>
                <div style="background-color: #f1f5f9; border-radius: 8px; padding: 18px; text-align: center; margin: 24px 0;">
                    <span style="font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #1565c0;">%s</span>
                </div>
                <p style="color: #94a3b8; font-size: 12px; margin: 0;">본인이 요청하지 않은 경우 즉시 관리자에게 문의하거나 비밀번호를 변경하세요.</p>
            </div>
            """, code);

        try {
            mailSendService.sendMail("noreply@mplatform.com", List.of(user.getEmail()), null, null, subject, htmlBody, null);
            log.info("Email OTP sent to user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to send Email OTP to user: {}", user.getUsername(), e);
            throw new RuntimeException("이메일 인증번호 발송에 실패했습니다.", e);
        }

        return code;
    }

    /**
     * 이메일 OTP 검증 (성공 시 1회 소진)
     */
    public boolean verifyEmailOtp(String username, String code) {
        if (username == null || code == null) {
            return false;
        }

        EmailOtpEntry entry = emailOtpCache.get(username);
        if (entry == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            emailOtpCache.remove(username);
            return false;
        }

        if (MessageDigest.isEqual(entry.code().getBytes(StandardCharsets.UTF_8), code.trim().getBytes(StandardCharsets.UTF_8))) {
            emailOtpCache.remove(username); // 1회 소비
            return true;
        }

        return false;
    }

    /**
     * 사용자 권한 및 유예기간에 따른 2FA 필수 여부 판단
     */
    public boolean isTwoFactorRequired(User user) {
        if (user == null) {
            return false;
        }

        // 1. 이미 자발적 또는 강제로 2FA가 활성화된 계정
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return true;
        }

        // 2. 관리자 역할군 체크
        String role = user.getRole();
        boolean isAdminRole = role != null && (
                role.equals("ROLE_ADMIN") ||
                role.equals("ADMIN") ||
                role.equals("SYSTEM_ADMIN") ||
                role.equals("ROLE_SYSTEM_ADMIN") ||
                role.equals("ORG_ADMIN") ||
                role.equals("ROLE_ORG_ADMIN") ||
                role.equals("DATA_STEWARD")
        );

        if (isAdminRole) {
            // 7일 유예기간이 설정되어 있고 아직 지나지 않았다면 필수 아님 (스킵 허용)
            if (user.getTwoFactorGraceUntil() != null && LocalDateTime.now().isBefore(user.getTwoFactorGraceUntil())) {
                return false;
            }
            // 유예기간이 만료되었거나 미설정된 관리자는 무조건 필수
            return true;
        }

        return false;
    }

    // --- Base32 Utility (RFC 4648) ---

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;

        while (next < data.length) {
            buffer <<= 8;
            buffer |= (data[next++] & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                result.append(BASE32_CHARS.charAt(index));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32_CHARS.charAt(index));
        }

        return result.toString();
    }

    private byte[] decodeBase32(String base32) {
        String upper = base32.toUpperCase().replaceAll("[= ]", "");
        byte[] bytes = new byte[upper.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Illegal character in Base32 string: " + c);
            }
            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[count++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }

        return bytes;
    }
}
