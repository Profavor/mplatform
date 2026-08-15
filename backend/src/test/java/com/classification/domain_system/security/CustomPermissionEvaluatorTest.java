package com.classification.domain_system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomPermissionEvaluatorTest {

    private CustomPermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        permissionEvaluator = new CustomPermissionEvaluator();
    }

    @Test
    @DisplayName("단일 퍼미션(record:read) 보유 시 해당 리소스/액션 검증")
    void testSinglePermissionMatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user1", null, List.of(new SimpleGrantedAuthority("record:read"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, "record", "read"));
        assertFalse(permissionEvaluator.hasPermission(auth, "record", "write"));
        assertFalse(permissionEvaluator.hasPermission(auth, "record", "delete"));
    }

    @Test
    @DisplayName("리소스타입 와일드카드 퍼미션(record:*) 보유 시 record 하위 모든 액션 통과 및 타 리소스 거부")
    void testResourceWildcardMatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user1", null, List.of(new SimpleGrantedAuthority("record:*"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, "record", "read"));
        assertTrue(permissionEvaluator.hasPermission(auth, "record", "write"));
        assertTrue(permissionEvaluator.hasPermission(auth, "record", "delete"));
        assertTrue(permissionEvaluator.hasPermission(auth, "record", "export"));

        assertFalse(permissionEvaluator.hasPermission(auth, "log", "read"));
    }

    @Test
    @DisplayName("전체 와일드카드 퍼미션(*:* 또는 *) 보유 시 모든 리소스 및 액션 통과")
    void testSuperWildcardPermissionMatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "superUser", null, List.of(new SimpleGrantedAuthority("*:*"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, "record", "write"));
        assertTrue(permissionEvaluator.hasPermission(auth, "log", "export"));
        assertTrue(permissionEvaluator.hasPermission(auth, "domain", "delete"));
        assertTrue(permissionEvaluator.hasPermission(auth, "dq", "read"));
        assertTrue(permissionEvaluator.hasPermission(auth, "system", "freshness-heatmap"));
    }

    @Test
    @DisplayName("글로벌 액션 와일드카드 퍼미션(*:read) 보유 시 모든 리소스의 read 액션 허용 및 write 거부")
    void testGlobalActionWildcardPermissionMatch() {
        Authentication readAllAuth = new UsernamePasswordAuthenticationToken(
                "auditor", null, List.of(new SimpleGrantedAuthority("*:read"))
        );

        assertTrue(permissionEvaluator.hasPermission(authAllRead(readAllAuth), "domain", "read"));
        assertTrue(permissionEvaluator.hasPermission(readAllAuth, "system", "read"));
        assertTrue(permissionEvaluator.hasPermission(readAllAuth, "log", "read"));
        assertFalse(permissionEvaluator.hasPermission(readAllAuth, "domain", "write"));
    }

    private Authentication authAllRead(Authentication auth) {
        return auth;
    }

    @Test
    @DisplayName("system과 admin 리소스 간 상호 호환 퍼미션 검증 (admin:read 보유 시 system:read 통과)")
    void testSystemAdminResourceCompatibility() {
        Authentication adminReadAuth = new UsernamePasswordAuthenticationToken(
                "manager", null, List.of(new SimpleGrantedAuthority("admin:read"))
        );

        assertTrue(permissionEvaluator.hasPermission(adminReadAuth, null, "system:read"));
        assertTrue(permissionEvaluator.hasPermission(adminReadAuth, null, "admin:read"));
        assertFalse(permissionEvaluator.hasPermission(adminReadAuth, null, "admin:write"));
    }

    @Test
    @DisplayName("permission 매개변수에 'resource:action' 형식을 직접 전달한 경우 검증")
    void testCombinedPermissionString() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user1", null, List.of(new SimpleGrantedAuthority("dq:read"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, null, "dq:read"));
        assertFalse(permissionEvaluator.hasPermission(auth, null, "dq:write"));
    }

    @Test
    @DisplayName("퍼미션 없는 일반 사용자는 접근 거부")
    void testNoPermissionAccessDenied() {
        Authentication noPermAuth = new UsernamePasswordAuthenticationToken(
                "guest", null, List.of(new SimpleGrantedAuthority("dashboard:read"))
        );

        assertFalse(permissionEvaluator.hasPermission(noPermAuth, null, "admin:read"));
        assertFalse(permissionEvaluator.hasPermission(noPermAuth, null, "domain:read"));
        assertFalse(permissionEvaluator.hasPermission(noPermAuth, "system", "freshness-heatmap"));
    }
}
