package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.TeamRepository;
import com.classification.domain_system.service.RoleInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrganizationControllerTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RoleInitializer roleInitializer;

    @Mock
    private com.classification.domain_system.repository.RoleRepository roleRepository;

    @Mock
    private com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    @Mock
    private com.classification.domain_system.repository.UserRepository userRepository;

    @Mock
    private com.classification.domain_system.repository.DomainPermissionRepository domainPermissionRepository;

    @Mock
    private com.classification.domain_system.repository.DomainAccessRequestRepository domainAccessRequestRepository;

    @InjectMocks
    private OrganizationController organizationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("성공 - 부서 수정 API (PUT)")
    void testUpdateDepartment() {
        UUID orgId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();
        UUID parentDeptId = UUID.randomUUID();

        Department existing = new Department();
        existing.setId(deptId);
        existing.setOrganizationId(orgId);
        existing.setName("기존 부서명");
        existing.setDescription("기존 설명");

        Department updateReq = new Department();
        updateReq.setName("수정된 부서명");
        updateReq.setDescription("수정된 설명");
        updateReq.setParentDepartmentId(parentDeptId);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(existing));
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Department> response = organizationController.updateDepartment(orgId, deptId, updateReq);

        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("수정된 부서명");
        assertThat(response.getBody().getDescription()).isEqualTo("수정된 설명");
        assertThat(response.getBody().getParentDepartmentId()).isEqualTo(parentDeptId);
    }

    @Test
    @DisplayName("성공 - 부서 삭제 API (DELETE)")
    void testDeleteDepartment() {
        UUID orgId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();

        Department dept = new Department();
        dept.setId(deptId);
        dept.setOrganizationId(orgId);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));

        ResponseEntity<Void> response = organizationController.deleteDepartment(orgId, deptId);

        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NO_CONTENT);
        verify(departmentRepository, times(1)).delete(dept);
    }

    @Test
    @DisplayName("성공 - 조직 삭제 API (DELETE)")
    void testDeleteOrganization() {
        UUID orgId = UUID.randomUUID();
        com.classification.domain_system.entity.Organization targetOrg = new com.classification.domain_system.entity.Organization();
        targetOrg.setId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(targetOrg));

        ResponseEntity<Void> response = organizationController.deleteOrganization(orgId);

        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NO_CONTENT);
        verify(organizationRepository, times(1)).delete(targetOrg);
    }
}
