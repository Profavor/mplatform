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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class RoleInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private RoleInitializer roleInitializer;

    @BeforeEach
    void setUp() {
        roleInitializer = new RoleInitializer(roleRepository, organizationRepository, objectMapper);
    }

    @Test
    @DisplayName("성공 - 역할이 전무한 조직 전달 시 표준 기본 시스템 역할 자동 생성")
    void testCreateDefaultRolesForOrg_WhenNoRolesExist() {
        UUID orgId = UUID.randomUUID();
        given(roleRepository.findByOrganizationIdAndName(eq(orgId), anyString())).willReturn(Optional.empty());

        roleInitializer.createDefaultRolesForOrg(orgId);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeast(8)).save(roleCaptor.capture());
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

        org.mockito.BDDMockito.lenient().when(roleRepository.findByOrganizationIdAndName(eq(orgId), anyString())).thenReturn(Optional.empty());
        org.mockito.BDDMockito.lenient().when(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("ROLE_USER"))).thenReturn(Optional.of(existingRole));
        org.mockito.BDDMockito.lenient().when(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("USER"))).thenReturn(Optional.of(existingRole));

        roleInitializer.createDefaultRolesForOrg(orgId);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(roleCaptor.capture());
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

        org.mockito.BDDMockito.lenient().when(roleRepository.findByOrganizationIdAndName(eq(orgId), anyString())).thenReturn(Optional.empty());
        org.mockito.BDDMockito.lenient().when(roleRepository.findByOrganizationIdAndName(eq(orgId), eq("ROLE_ADMIN"))).thenReturn(Optional.of(existingAdmin));

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

        verify(roleRepository, atLeast(16)).save(any(Role.class)); // org1(8개) + org2(8개)
    }
}

