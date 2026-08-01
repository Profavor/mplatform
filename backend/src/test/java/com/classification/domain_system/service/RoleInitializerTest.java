package com.classification.domain_system.service;

import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RoleInitializerTest {

    private RoleRepository roleRepository;
    private OrganizationRepository organizationRepository;
    private RoleInitializer roleInitializer;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        roleInitializer = new RoleInitializer(roleRepository, organizationRepository);
    }

    @Test
    @DisplayName("성공 - 역할이 전무한 조직 전달 시 8개 표준 기본 시스템 역할 자동 생성")
    void testCreateDefaultRolesForOrg_WhenNoRolesExist() {
        UUID orgId = UUID.randomUUID();
        given(roleRepository.findByOrganizationIdAndName(eq(orgId), any())).willReturn(Optional.empty());

        roleInitializer.createDefaultRolesForOrg(orgId);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(8)).save(roleCaptor.capture());
        
        List<Role> savedRoles = roleCaptor.getAllValues();
        assertThat(savedRoles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ORG_ADMIN", "DATA_STEWARD", "DOMAIN_EDITOR", "DQ_MANAGER", "INTEGRATION", "VIEWER", "ROLE_USER");
    }

    @Test
    @DisplayName("성공 - 일부 역할만 존재하는 조직 전달 시 없는 기본 역할만 추가 생성")
    void testCreateDefaultRolesForOrg_WhenPartialRolesExist() {
        UUID orgId = UUID.randomUUID();

        Role existingRole = new Role();
        existingRole.setId(UUID.randomUUID());
        existingRole.setOrganizationId(orgId);
        existingRole.setName("ROLE_USER");
        existingRole.setPermissions(new HashSet<>(Set.of("domain:read", "node:read", "record:read")));

        given(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("ROLE_USER"))).willReturn(Optional.of(existingRole));
        given(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("USER"))).willReturn(Optional.of(existingRole));

        roleInitializer.createDefaultRolesForOrg(orgId);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(roleCaptor.capture());

        List<Role> savedRoles = roleCaptor.getAllValues();
        // ROLE_USER 외 7개의 미존재 표준 역할이 새로 save 됨
        assertThat(savedRoles).extracting(Role::getName)
                .contains("ROLE_ADMIN", "ORG_ADMIN", "DATA_STEWARD", "DOMAIN_EDITOR", "DQ_MANAGER", "INTEGRATION", "VIEWER");
    }

    @Test
    @DisplayName("성공 - 이미 존재하는 역할에 표준 퍼미션 일부가 누락된 경우 기존 퍼미션 보존하며 누락분 추가")
    void testCreateDefaultRolesForOrg_WhenRoleExistsWithMissingPermissions() {
        UUID orgId = UUID.randomUUID();

        Role existingAdmin = new Role();
        existingAdmin.setId(UUID.randomUUID());
        existingAdmin.setOrganizationId(orgId);
        existingAdmin.setName("ROLE_ADMIN");
        existingAdmin.setPermissions(new HashSet<>(Set.of("admin:read"))); // '*' 가 누락됨

        given(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("ROLE_ADMIN"))).willReturn(Optional.of(existingAdmin));

        roleInitializer.createDefaultRolesForOrg(orgId);

        assertThat(existingAdmin.getPermissions()).contains("*", "admin:read", "admin:write");
        verify(roleRepository, atLeastOnce()).save(existingAdmin);
    }

    @Test
    @DisplayName("성공 - 전체 조직 대상 동기화 메서드 실행 시 모든 조직의 역할 및 퍼미션 동기화")
    void testSyncDefaultRolesForAllOrganizations() {
        Organization org1 = new Organization();
        org1.setId(UUID.randomUUID());
        Organization org2 = new Organization();
        org2.setId(UUID.randomUUID());

        given(organizationRepository.findAll()).willReturn(List.of(org1, org2));

        roleInitializer.syncDefaultRolesForAllOrganizations();

        verify(roleRepository, times(16)).save(any(Role.class)); // org1(8개) + org2(8개)
    }
}

