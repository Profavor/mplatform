package com.classification.domain_system.security;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.UserRole;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;
    private DepartmentRepository departmentRepository;
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userRoleRepository = Mockito.mock(UserRoleRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        departmentRepository = Mockito.mock(DepartmentRepository.class);
        permissionService = new PermissionService(userRepository, userRoleRepository, roleRepository, departmentRepository);
    }

    @Test
    @DisplayName("domain:* 와일드카드 권한 소지 시 domain:read, domain:write 권한 자동 확장 검증")
    void testWildcardPermissionExpansion() {
        String username = "manager";
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setRole("USER");

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("DOMAIN_MANAGER");
        role.setPermissions(Set.of("domain:*", "node:read"));

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(List.of(userRole));
        when(roleRepository.findAllById(Set.of(role.getId()))).thenReturn(List.of(role));

        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, user.getRole());
        Set<String> authStrings = new HashSet<>();
        authorities.forEach(a -> authStrings.add(a.getAuthority()));

        assertTrue(authStrings.contains("domain:*"));
        assertTrue(authStrings.contains("node:read"));
        assertFalse(authStrings.contains("node:write"));
    }

    @Test
    @DisplayName("DB Role 마스터에 설정된 ROLE_ADMIN의 permissions(*)가 동적으로 수집되는지 검증")
    void testAdminRolePermissions() {
        String username = "adminUser";
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setRole("ROLE_ADMIN");

        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(Set.of("*"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(roleRepository.findAll()).thenReturn(List.of(adminRole));

        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, "ROLE_ADMIN");
        Set<String> authStrings = new HashSet<>();
        authorities.forEach(a -> authStrings.add(a.getAuthority()));

        assertTrue(authStrings.contains("ROLE_ADMIN"));
        assertTrue(authStrings.contains("*"), "DB Role 마스터에 설정된 '*' 권한이 수집되어야 합니다");
    }

    @Test
    @DisplayName("부서에 부여된 역할이 사용자의 GrantedAuthority에 반영되는지 검증")
    void testDepartmentRolesReflectedInAuthorities() {
        String username = "deptUser";
        UUID deptId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setRole("ROLE_USER");
        user.setDepartmentId(deptId);

        Department dept = new Department();
        dept.setId(deptId);
        dept.setName("데이터관리부");
        dept.setRoles(Set.of("ROLE_DATA_STEWARD", "DOMAIN_EDITOR"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));

        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, "ROLE_USER");
        Set<String> authStrings = new HashSet<>();
        authorities.forEach(a -> authStrings.add(a.getAuthority()));

        // 부서 역할이 반영되었는지 검증
        assertTrue(authStrings.contains("ROLE_DATA_STEWARD"), "부서에 부여된 ROLE_DATA_STEWARD가 권한에 포함되어야 합니다");
        assertTrue(authStrings.contains("ROLE_DOMAIN_EDITOR"), "ROLE_ 접두사 없이 입력된 DOMAIN_EDITOR도 ROLE_DOMAIN_EDITOR로 정규화되어 포함되어야 합니다");
        assertTrue(authStrings.contains("ROLE_USER"), "사용자 기본 역할 ROLE_USER가 유지되어야 합니다");
    }

    @Test
    @DisplayName("부서가 없는 사용자는 부서 역할 없이 정상 동작하는지 검증")
    void testNoDepartmentUserWorksNormally() {
        String username = "noDeptUser";

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setRole("ROLE_USER");
        user.setDepartmentId(null); // 부서 미배정

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, "ROLE_USER");
        Set<String> authStrings = new HashSet<>();
        authorities.forEach(a -> authStrings.add(a.getAuthority()));

        assertTrue(authStrings.contains("ROLE_USER"));
    }

    @Test
    @DisplayName("USER 역할 보유 시 Role 마스터에 설정된 domain:read, node:read 등의 세부 권한이 자동 수집되는지 검증")
    void testUserMasterRolePermissionsAdded() {
        String username = "normalUser";

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setRole("USER");

        Role userMasterRole = new Role();
        userMasterRole.setId(UUID.randomUUID());
        userMasterRole.setName("USER");
        userMasterRole.setPermissions(Set.of("domain:read", "node:read", "record:read", "dq:read", "org:read"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(roleRepository.findAll()).thenReturn(List.of(userMasterRole));

        Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, "USER");
        Set<String> authStrings = new HashSet<>();
        authorities.forEach(a -> authStrings.add(a.getAuthority()));

        assertTrue(authStrings.contains("ROLE_USER"));
        assertTrue(authStrings.contains("domain:read"), "Role 마스터에 설정된 domain:read 권한이 포함되어야 합니다");
        assertTrue(authStrings.contains("node:read"), "Role 마스터에 설정된 node:read 권한이 포함되어야 합니다");
        assertTrue(authStrings.contains("record:read"), "Role 마스터에 설정된 record:read 권한이 포함되어야 합니다");
    }
}
