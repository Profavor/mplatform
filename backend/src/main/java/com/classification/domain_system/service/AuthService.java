package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final com.classification.domain_system.repository.LoginLogRepository loginLogRepository;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final SseNotificationService sseNotificationService;

    public void register(String username, String password, String role) {
        register(username, password, role, "Asia/Seoul");
    }

    public void register(String username, String password, String role, String timezone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        // 회원가입 시 무분별한 관리자 생성 방지: 일반 회원가입은 무조건 ROLE_USER 지정
        user.setRole("ROLE_USER");
        user.setTimezone(timezone != null && !timezone.trim().isEmpty() ? timezone : "Asia/Seoul");
        
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.findByUsername(username.trim()).isPresent();
    }

    public String login(String username, String password, String ipAddress) {
        return login(username, password, ipAddress, null);
    }

    @org.springframework.transaction.annotation.Transactional
    public String login(String username, String password, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 1세션 생성을 위한 newSessionId
        String newSessionId = java.util.UUID.randomUUID().toString();
        if (user.getId() != null) {
            Map<String, Object> logoutEvent = Map.of(
                    "eventType", "FORCE_LOGOUT",
                    "title", "세션 종료",
                    "message", "다른 기기/브라우저에서 로그인되어 현재 세션이 종료되었습니다."
            );
            if (sseNotificationService != null) {
                try {
                    sseNotificationService.sendNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
            if (webSocketPublisher != null) {
                try {
                    webSocketPublisher.publishNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
        }
        user.setActiveSessionId(newSessionId);
        userRepository.saveAndFlush(user);

        // 로그인 이력 기록
        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        return jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId(), newSessionId);
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> loginWithTokens(String username, String password, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String newSessionId = java.util.UUID.randomUUID().toString();
        if (user.getId() != null) {
            Map<String, Object> logoutEvent = Map.of(
                    "eventType", "FORCE_LOGOUT",
                    "title", "세션 종료",
                    "message", "다른 기기/브라우저에서 로그인되어 현재 세션이 종료되었습니다."
            );
            if (sseNotificationService != null) {
                try {
                    sseNotificationService.sendNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
            if (webSocketPublisher != null) {
                try {
                    webSocketPublisher.publishNotification(user.getId(), logoutEvent);
                } catch (Exception ignored) {}
            }
        }
        user.setActiveSessionId(newSessionId);
        userRepository.saveAndFlush(user);

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, newSessionId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr);

        Map<String, String> map = new HashMap<>();
        map.put("token", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public Map<String, String> refreshTokens(String refreshToken) {
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found for refresh token");
        }

        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String currentActiveSessionId = user.getActiveSessionId();
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr, currentActiveSessionId);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole(), userIdStr);

        Map<String, String> map = new HashMap<>();
        map.put("token", newAccessToken);
        map.put("refreshToken", newRefreshToken);
        return map;
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public org.springframework.data.domain.Page<com.classification.domain_system.entity.LoginLog> getLoginLogs(org.springframework.data.domain.Pageable pageable) {
        return loginLogRepository.findAll(pageable);
    }
}
