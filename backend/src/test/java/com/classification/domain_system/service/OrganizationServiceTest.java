package com.classification.domain_system.service;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Team;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private RoleInitializer roleInitializer;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainPermissionRepository domainPermissionRepository;
    @Mock
    private DomainAccessRequestRepository domainAccessRequestRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private UUID orgId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(orgId);
        organization.setName("test-org");
        organization.setDisplayName("테스트 조직");
    }

    @Test
    @DisplayName("조직 전체 목록 조회 성공")
    void getAllOrganizations_success() {
        given(organizationRepository.findAll()).willReturn(List.of(organization));

        List<Organization> result = organizationService.getAllOrganizations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test-org");
    }

    @Test
    @DisplayName("조직 신규 등록 시 기본 역할이 초기화된다")
    void createOrganization_success() {
        given(organizationRepository.save(any(Organization.class))).willReturn(organization);

        Organization created = organizationService.createOrganization(organization);

        assertThat(created).isNotNull();
        verify(organizationRepository).save(organization);
        verify(roleInitializer).createDefaultRolesForOrg(orgId);
    }

    @Test
    @DisplayName("조직 삭제 시 소속 팀, 부서, 역할, 사용자 및 권한이 정리되고 조직이 삭제된다")
    void deleteOrganization_success() {
        given(organizationRepository.findById(orgId)).willReturn(Optional.of(organization));
        given(teamRepository.findByOrganizationId(orgId)).willReturn(List.of());
        given(departmentRepository.findByOrganizationId(orgId)).willReturn(List.of());
        given(roleRepository.findByOrganizationId(orgId)).willReturn(List.of());
        given(userRepository.findByOrganizationId(orgId)).willReturn(List.of());

        boolean deleted = organizationService.deleteOrganization(orgId);

        assertThat(deleted).isTrue();
        verify(organizationRepository).delete(organization);
    }
}
