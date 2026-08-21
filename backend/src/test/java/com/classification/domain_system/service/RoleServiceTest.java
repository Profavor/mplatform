package com.classification.domain_system.service;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    @DisplayName("getAllRoles - 전체 역할 목록 조회")
    void getAllRoles_Success() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ROLE_ADMIN");

        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<Role> result = roleService.getAllRoles();

        assertEquals(1, result.size());
        assertEquals("ROLE_ADMIN", result.get(0).getName());
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("getRolesByOrg - 조직별 역할 목록 조회")
    void getRolesByOrg_Success() {
        UUID orgId = UUID.randomUUID();
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setOrganizationId(orgId);

        when(roleRepository.findByOrganizationIdOrOrganizationIdIsNull(orgId)).thenReturn(List.of(role));

        List<Role> result = roleService.getRolesByOrg(orgId);

        assertEquals(1, result.size());
        assertEquals(orgId, result.get(0).getOrganizationId());
        verify(roleRepository).findByOrganizationIdOrOrganizationIdIsNull(orgId);
    }

    @Test
    @DisplayName("createRole - 신규 역할 저장")
    void createRole_Success() {
        Role role = new Role();
        role.setName("ROLE_CUSTOM");

        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role created = roleService.createRole(role);

        assertNotNull(created);
        assertEquals("ROLE_CUSTOM", created.getName());
        verify(roleRepository).save(role);
    }

    @Test
    @DisplayName("updateRole - 기존 역할 수정 성공")
    void updateRole_Success() {
        UUID roleId = UUID.randomUUID();
        Role existing = new Role();
        existing.setId(roleId);
        existing.setName("OLD_NAME");

        Role updated = new Role();
        updated.setName("NEW_NAME");
        updated.setDisplayName("새 역할");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existing));
        when(roleRepository.save(existing)).thenReturn(existing);

        Optional<Role> result = roleService.updateRole(roleId, updated);

        assertTrue(result.isPresent());
        assertEquals("NEW_NAME", result.get().getName());
        assertEquals("새 역할", result.get().getDisplayName());
        verify(roleRepository).save(existing);
    }

    @Test
    @DisplayName("deleteRole - 역할 및 관련 user_roles 삭제")
    void deleteRole_Success() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        boolean deleted = roleService.deleteRole(roleId);

        assertTrue(deleted);
        verify(userRoleRepository).deleteByRoleId(roleId);
        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("deleteRole - 존재하지 않는 역할 삭제 시 false 반환")
    void deleteRole_NotFound() {
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        boolean deleted = roleService.deleteRole(roleId);

        assertFalse(deleted);
        verify(userRoleRepository, never()).deleteByRoleId(any());
        verify(roleRepository, never()).delete(any());
    }
}
