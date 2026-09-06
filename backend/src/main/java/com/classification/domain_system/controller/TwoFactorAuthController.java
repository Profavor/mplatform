package com.classification.domain_system.controller;

import com.classification.domain_system.dto.TwoFactorEnableRequest;
import com.classification.domain_system.dto.TwoFactorEnableResponse;
import com.classification.domain_system.dto.TwoFactorSetupResponse;
import com.classification.domain_system.dto.TwoFactorStatusResponse;
import com.classification.domain_system.dto.TwoFactorVerifyRequest;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.TwoFactorAuthService;
import com.classification.domain_system.utils.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/setup")
    public ResponseEntity<TwoFactorSetupResponse> setup(
            @RequestParam(value = "username", required = false) String usernameParam,
            Authentication authentication
    ) {
        String username = resolveUsername(usernameParam, authentication);
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User authentication is required for 2FA setup");
        }

        String secret = twoFactorAuthService.generateSecret();
        String otpAuthUrl = twoFactorAuthService.getOtpAuthUrl(secret, username);

        TwoFactorSetupResponse response = TwoFactorSetupResponse.builder()
                .secret(secret)
                .otpAuthUrl(otpAuthUrl)
                .qrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable")
    public ResponseEntity<TwoFactorEnableResponse> enable(
            @RequestParam(value = "username", required = false) String usernameParam,
            @RequestBody TwoFactorEnableRequest request,
            Authentication authentication
    ) {
        String username = resolveUsername(usernameParam, authentication);
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User authentication is required");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User not found")
        );

        if (!twoFactorAuthService.verifyTotpCode(request.getSecret(), request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Invalid 2FA verification code");
        }

        List<String> backupCodes = twoFactorAuthService.generateBackupCodes(10);
        String hashedBackupCodes = twoFactorAuthService.hashAndStoreBackupCodes(backupCodes);

        user.setTwoFactorSecret(request.getSecret());
        user.setTwoFactorEnabled(true);
        user.setTwoFactorType(request.getType() != null && !request.getType().isBlank() ? request.getType() : "TOTP");
        user.setBackupCodes(hashedBackupCodes);
        user.setTwoFactorGraceUntil(null);
        userRepository.save(user);

        TwoFactorEnableResponse response = TwoFactorEnableResponse.builder()
                .success(true)
                .backupCodes(backupCodes)
                .message("Two-factor authentication enabled successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(
            @RequestBody TwoFactorVerifyRequest request,
            HttpServletRequest httpServletRequest
    ) {
        if (request.getUsername() == null || request.getTempToken() == null || request.getCode() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Username, tempToken, and verification code are required");
        }

        if (!authService.validateTempToken(request.getTempToken(), request.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid or expired temporary 2FA token");
        }

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() ->
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User not found")
        );

        String authType = request.getType() != null ? request.getType().toUpperCase() : "TOTP";
        boolean isValid = false;

        if ("TOTP".equals(authType)) {
            isValid = twoFactorAuthService.verifyTotpCode(user.getTwoFactorSecret(), request.getCode());
        } else if ("EMAIL".equals(authType)) {
            isValid = twoFactorAuthService.verifyEmailOtp(user.getUsername(), request.getCode());
        } else if ("BACKUP_CODE".equals(authType)) {
            isValid = twoFactorAuthService.verifyAndConsumeBackupCode(user, request.getCode());
        }

        if (!isValid) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid 2FA verification code");
        }

        authService.consumeTempToken(request.getTempToken());
        String clientIp = ClientIpUtil.getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        Map<String, String> tokens = authService.issueFinalTokensAfter2Fa(user, clientIp, userAgent, authType);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, Object>> sendEmailOtp(
            @RequestParam(value = "username", required = false) String usernameParam,
            @RequestParam(value = "tempToken", required = false) String tempTokenParam,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        String username = resolveUsername(usernameParam, authentication);
        if (username == null && body != null) {
            username = body.get("username");
        }
        if (username == null && tempTokenParam != null) {
            var info = authService.getTempTokenInfo(tempTokenParam);
            if (info != null) {
                username = info.getUsername();
            }
        }

        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Username or valid tempToken is required");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User not found")
        );

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "User does not have a registered email address");
        }

        twoFactorAuthService.sendEmailOtp(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent to email",
                "maskedEmail", authService.maskEmail(user.getEmail())
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<TwoFactorStatusResponse> getStatus(
            @RequestParam(value = "username", required = false) String usernameParam,
            Authentication authentication
    ) {
        String username = resolveUsername(usernameParam, authentication);
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Authentication required");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User not found")
        );

        Long graceDays = null;
        if (user.getTwoFactorGraceUntil() != null && LocalDateTime.now().isBefore(user.getTwoFactorGraceUntil())) {
            graceDays = Duration.between(LocalDateTime.now(), user.getTwoFactorGraceUntil()).toDays();
        }

        int remainingBackupCodes = 0;
        if (user.getBackupCodes() != null && !user.getBackupCodes().isBlank()) {
            remainingBackupCodes = user.getBackupCodes().split(",").length;
        }

        TwoFactorStatusResponse response = TwoFactorStatusResponse.builder()
                .twoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
                .twoFactorType(user.getTwoFactorType())
                .hasBackupCodes(remainingBackupCodes > 0)
                .remainingBackupCodesCount(remainingBackupCodes)
                .role(user.getRole())
                .mandatory(twoFactorAuthService.isTwoFactorRequired(user))
                .graceUntil(user.getTwoFactorGraceUntil())
                .gracePeriodRemainingDays(graceDays)
                .maskedEmail(authService.maskEmail(user.getEmail()))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disable(
            @RequestParam(value = "username", required = false) String usernameParam,
            Authentication authentication
    ) {
        String username = resolveUsername(usernameParam, authentication);
        if (username == null || username.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Authentication required");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.INVALID_CREDENTIALS, "User not found")
        );

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setBackupCodes(null);
        user.setTwoFactorType(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Two-factor authentication disabled"));
    }

    private String resolveUsername(String param, Authentication auth) {
        if (param != null && !param.isBlank()) {
            return param.trim();
        }
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }
}
