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
        user.setRole(role != null && !role.trim().isEmpty() ? role : "ROLE_USER");
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

    public String login(String username, String password, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 로그인 이력 기록
        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        return jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
    }

    public Map<String, String> loginWithTokens(String username, String password, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        com.classification.domain_system.entity.LoginLog log = com.classification.domain_system.entity.LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userAgent(userAgent)
                .clientIp(ipAddress)
                .build();
        loginLogRepository.save(log);

        String userIdStr = user.getId() != null ? user.getId().toString() : null;
        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr);
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
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), userIdStr);
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
