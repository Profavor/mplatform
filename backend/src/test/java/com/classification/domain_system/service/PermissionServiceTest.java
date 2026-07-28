package com.classification.domain_system.service;

import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    @DisplayName("기본 사용자 역할 부여 시 GrantedAuthority에 역할이 정상 등록된다")
    void getAuthoritiesForUser_registersGrantedAuthorities() {
        // when
        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser("user", "ROLE_USER");

        // then
        boolean hasRole = authorities.stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
        assertTrue(hasRole, "GrantedAuthority에 ROLE_USER가 포함되어야 합니다.");
    }
}
