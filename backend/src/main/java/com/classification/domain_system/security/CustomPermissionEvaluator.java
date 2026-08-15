package com.classification.domain_system.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 100% 퍼미션(Permission) 기반 권한 평가기
 * - Role 기반 하드코딩이 전혀 없으며, 사용자가 부여받은 세부 퍼미션(GrantedAuthority) 및 와일드카드(*, *:*, resource:*)만으로 인가를 평가합니다.
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        String permissionString = permission.toString().trim();
        String resource;
        String action;

        if (targetDomainObject != null && !targetDomainObject.toString().isBlank()) {
            resource = targetDomainObject.toString().trim().toLowerCase();
            action = permissionString.toLowerCase();
        } else {
            if (permissionString.contains(":")) {
                String[] parts = permissionString.split(":", 2);
                resource = parts[0].trim().toLowerCase();
                action = parts[1].trim().toLowerCase();
            } else {
                return checkAuthority(authentication, permissionString);
            }
        }

        return checkResourceActionPermission(authentication, resource, action);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }
        String resource = targetType != null ? targetType.trim().toLowerCase() : "";
        String action = permission.toString().trim().toLowerCase();
        return checkResourceActionPermission(authentication, resource, action);
    }

    /**
     * 리소스 및 액션에 대해 사용자가 보유한 퍼미션(GrantedAuthority)을 검증합니다.
     */
    private boolean checkResourceActionPermission(Authentication authentication, String resource, String action) {
        String requiredPermission = resource + ":" + action;
        String wildcardResourcePermission = resource + ":*";

        // system 및 admin 리소스 간 호환성 퍼미션 검증
        String altResource = "system".equals(resource) ? "admin" : ("admin".equals(resource) ? "system" : null);
        String altRequired = altResource != null ? altResource + ":" + action : null;
        String altWildcard = altResource != null ? altResource + ":*" : null;

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String auth = authority.getAuthority().trim().toLowerCase();

            // 1. 전역 와일드카드 퍼미션 (*, *:*, *:read, *:write 등)
            if ("*".equals(auth) || "*:*".equals(auth) || auth.startsWith("*:")) {
                if ("*".equals(auth) || "*:*".equals(auth)) {
                    return true;
                }
                String globalAction = auth.substring(2); // *:read -> read
                if ("*".equals(globalAction) || globalAction.equalsIgnoreCase(action)) {
                    return true;
                }
            }

            // 2. 리소스 와일드카드 퍼미션 (예: domain:*, admin:*, system:*, record:*)
            if (wildcardResourcePermission.equalsIgnoreCase(auth) || (altWildcard != null && altWildcard.equalsIgnoreCase(auth))) {
                return true;
            }

            // 3. 단위 리소스:액션 퍼미션 정확 일치 (예: domain:read, admin:read, system:read)
            if (requiredPermission.equalsIgnoreCase(auth) || (altRequired != null && altRequired.equalsIgnoreCase(auth))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 단순 퍼미션 문자열 검증
     */
    private boolean checkAuthority(Authentication authentication, String rawPermission) {
        String targetPerm = rawPermission.trim().toLowerCase();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String auth = authority.getAuthority().trim().toLowerCase();

            // 전역 와일드카드 퍼미션
            if ("*".equals(auth) || "*:*".equals(auth) || auth.startsWith("*:")) {
                return true;
            }
            if (targetPerm.equalsIgnoreCase(auth)) {
                return true;
            }
        }
        return false;
    }
}
