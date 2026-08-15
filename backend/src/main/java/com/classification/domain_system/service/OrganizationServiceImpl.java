package com.classification.domain_system.service;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.entity.Team;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final RoleInitializer roleInitializer;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final DomainPermissionRepository domainPermissionRepository;
    private final DomainAccessRequestRepository domainAccessRequestRepository;

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public Optional<Organization> getOrganization(UUID id) {
        return organizationRepository.findById(id);
    }

    @Override
    @Transactional
    public Organization createOrganization(Organization org) {
        if (org.getEmailDomain() != null) {
            org.setEmailDomain(org.getEmailDomain().trim().replaceAll("^@", ""));
        }
        Organization saved = organizationRepository.save(org);
        roleInitializer.createDefaultRolesForOrg(saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Optional<Organization> updateOrganization(UUID id, Organization req) {
        return organizationRepository.findById(id)
                .map(existing -> {
                    existing.setDisplayName(req.getDisplayName());
                    existing.setDescription(req.getDescription());
                    existing.setIcon(req.getIcon());
                    if (req.getEmailDomain() != null) {
                        existing.setEmailDomain(req.getEmailDomain().trim().replaceAll("^@", ""));
                    }
                    return organizationRepository.save(existing);
                });
    }

    @Override
    @Transactional
    public boolean deleteOrganization(UUID id) {
        return organizationRepository.findById(id)
                .map(org -> {
                    List<Team> teams = teamRepository.findByOrganizationId(id);
                    if (teams != null && !teams.isEmpty()) {
                        teamRepository.deleteAll(teams);
                    }
                    List<Department> depts = departmentRepository.findByOrganizationId(id);
                    if (depts != null && !depts.isEmpty()) {
                        departmentRepository.deleteAll(depts);
                    }
                    List<Role> roles = roleRepository.findByOrganizationId(id);
                    if (roles != null && !roles.isEmpty()) {
                        for (Role r : roles) {
                            userRoleRepository.deleteByRoleId(r.getId());
                        }
                        roleRepository.deleteAll(roles);
                    }
                    List<User> users = userRepository.findByOrganizationId(id);
                    if (users != null && !users.isEmpty()) {
                        for (User u : users) {
                            userRoleRepository.deleteByUserId(u.getId());
                            domainPermissionRepository.deleteByUserId(u.getId());
                            domainAccessRequestRepository.deleteByUserId(u.getId());
                        }
                        userRepository.deleteAll(users);
                    }

                    organizationRepository.delete(org);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<Department> getDepartments(UUID orgId) {
        return departmentRepository.findByOrganizationId(orgId);
    }

    @Override
    @Transactional
    public Department createDepartment(UUID orgId, Department dept) {
        dept.setOrganizationId(orgId);
        if (dept.getRoles() != null && !dept.getRoles().isEmpty()) {
            Set<String> clean = dept.getRoles().stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toSet());
            dept.setRoles(clean);
        }
        return departmentRepository.save(dept);
    }

    @Override
    @Transactional
    public Optional<Department> updateDepartment(UUID orgId, UUID deptId, Department deptReq) {
        return departmentRepository.findById(deptId)
                .map(existing -> {
                    existing.setName(deptReq.getName());
                    existing.setDescription(deptReq.getDescription());
                    existing.setParentDepartmentId(deptReq.getParentDepartmentId());
                    existing.setIcon(deptReq.getIcon());
                    if (deptReq.getRoles() != null) {
                        Set<String> clean = deptReq.getRoles().stream()
                                .filter(r -> r != null && !r.trim().isEmpty())
                                .map(String::trim)
                                .collect(Collectors.toSet());
                        existing.getRoles().clear();
                        existing.getRoles().addAll(clean);
                    }
                    return departmentRepository.save(existing);
                });
    }

    @Override
    @Transactional
    public boolean deleteDepartment(UUID orgId, UUID deptId) {
        return departmentRepository.findById(deptId)
                .map(dept -> {
                    List<Department> allDepts = departmentRepository.findByOrganizationId(orgId);
                    for (Department d : allDepts) {
                        if (deptId.equals(d.getParentDepartmentId())) {
                            d.setParentDepartmentId(dept.getParentDepartmentId());
                            departmentRepository.save(d);
                        }
                    }
                    List<Team> teams = teamRepository.findByDepartmentId(deptId);
                    if (teams != null && !teams.isEmpty()) {
                        teamRepository.deleteAll(teams);
                    }
                    departmentRepository.delete(dept);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public List<Team> getTeams(UUID orgId) {
        return teamRepository.findByOrganizationId(orgId);
    }

    @Override
    @Transactional
    public Team createTeam(UUID orgId, Team team) {
        team.setOrganizationId(orgId);
        return teamRepository.save(team);
    }
}
