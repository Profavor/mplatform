package com.classification.domain_system.security;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 인증된 사용자 정보를 추출하는 공통 유틸리티.
 * 컨트롤러/서비스 전반에 중복된 getCurrentUserId 패턴을 통합합니다.
 */
@Component
public class SecurityUtils {

    private final AuthContext authContext;

    public SecurityUtils(AuthContext authContext) {
        this.authContext = authContext;
    }

    /**
     * 현재 인증된 사용자 ID를 반환합니다. 인증 정보가 없으면 null을 반환합니다.
     */
    public String getCurrentUserId() {
        // 1. AuthContext에서 우선 조회
        if (authContext != null && authContext.getUserId() != null && !authContext.getUserId().isBlank()) {
            return authContext.getUserId();
        }
        // 2. SecurityContextHolder fallback
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            String name = auth.getName();
            if (!name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
                return name;
            }
        }
        return null;
    }

    /**
     * 현재 인증된 사용자 ID를 반환합니다. 인증 정보가 없으면 BusinessException을 발생시킵니다.
     */
    public String getCurrentUserIdOrThrow() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED, "인증된 사용자 정보가 존재하지 않습니다."
            );
        }
        return userId;
    }
}
