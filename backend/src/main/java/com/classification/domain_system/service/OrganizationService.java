package com.classification.domain_system.service;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    List<Organization> getAllOrganizations();

    Optional<Organization> getOrganization(UUID id);

    Organization createOrganization(Organization org);

    Optional<Organization> updateOrganization(UUID id, Organization req);

    boolean deleteOrganization(UUID id);

    List<Department> getDepartments(UUID orgId);

    Department createDepartment(UUID orgId, Department dept);

    Optional<Department> updateDepartment(UUID orgId, UUID deptId, Department deptReq);

    boolean deleteDepartment(UUID orgId, UUID deptId);

    List<Team> getTeams(UUID orgId);

    Team createTeam(UUID orgId, Team team);
}
