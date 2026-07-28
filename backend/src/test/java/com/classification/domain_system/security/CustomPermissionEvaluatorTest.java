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
    @DisplayName("단일 권한(record:read) 보유 시 해당 리소스/액션 검증")
    void testSinglePermissionMatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user1", null, List.of(new SimpleGrantedAuthority("record:read"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, "record", "read"));
        assertFalse(permissionEvaluator.hasPermission(auth, "record", "write"));
        assertFalse(permissionEvaluator.hasPermission(auth, "record", "delete"));
    }

    @Test
    @DisplayName("리소스타입 와일드카드(record:*) 보유 시 record 하위 모든 액션 통과 및 타 리소스 거부")
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
    @DisplayName("전체 와일드카드(*:* 또는 *) 보유 시 모든 리소스 및 액션 통과")
    void testSuperWildcardMatch() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("*:*"))
        );

        assertTrue(permissionEvaluator.hasPermission(auth, "record", "write"));
        assertTrue(permissionEvaluator.hasPermission(auth, "log", "export"));
        assertTrue(permissionEvaluator.hasPermission(auth, "domain", "delete"));
        assertTrue(permissionEvaluator.hasPermission(auth, "dq", "read"));
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
    @DisplayName("DB 전역 와일드카드(*) Authority 보유 시 조직/도메인 생성/수정/삭제 권한 통과 검증")
    void testAdminRoleAuthorityMatch() {
        Authentication authWildcard = new UsernamePasswordAuthenticationToken(
                "admin1", null, List.of(new SimpleGrantedAuthority("*"))
        );

        assertTrue(permissionEvaluator.hasPermission(authWildcard, null, "org:write"));
        assertTrue(permissionEvaluator.hasPermission(authWildcard, null, "admin:write"));
        assertTrue(permissionEvaluator.hasPermission(authWildcard, "organization", "write"));
    }
}
