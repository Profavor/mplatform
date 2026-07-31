package com.classification.domain_system.service;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.UserRole;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Collection<GrantedAuthority> getAuthoritiesForUser(String username, String roleStr) {
        Set<String> authorities = new HashSet<>();

        if (roleStr != null && !roleStr.isBlank()) {
            for (String r : roleStr.split(",")) {
                String trimmed = r.trim();
                if (!trimmed.isEmpty()) {
                    String roleAuthority = trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
                    authorities.add(roleAuthority);
                }
            }
        }


        // 1. Role 마스터 테이블 기반 권한 확장 (roleStr/user.getRole()과 매칭되는 Role 엔티티 조회)
        Set<String> roleNamesToSearch = new HashSet<>();
        if (roleStr != null && !roleStr.isBlank()) {
            for (String r : roleStr.split(",")) {
                String trimmed = r.trim();
                roleNamesToSearch.add(trimmed);
                if (trimmed.startsWith("ROLE_")) {
                    roleNamesToSearch.add(trimmed.substring(5));
                } else {
                    roleNamesToSearch.add("ROLE_" + trimmed);
                }
            }
        }

        if (username != null && !username.isBlank()) {
            try {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRole() != null && !user.getRole().isBlank()) {
                        String userRole = user.getRole().trim();
                        roleNamesToSearch.add(userRole);
                        if (userRole.startsWith("ROLE_")) {
                            roleNamesToSearch.add(userRole.substring(5));
                        } else {
                            roleNamesToSearch.add("ROLE_" + userRole);
                        }
                    }

                    // UserRole 테이블 기반 역할 조회
                    List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
                    if (userRoles != null && !userRoles.isEmpty()) {
                        Set<UUID> roleIds = userRoles.stream()
                                .map(UserRole::getRoleId)
                                .collect(Collectors.toSet());
                        List<Role> explicitRoles = roleRepository.findAllById(roleIds);
                        for (Role role : explicitRoles) {
                            if (role.getPermissions() != null) {
                                for (String perm : role.getPermissions()) {
                                    if (perm != null && !perm.isBlank()) {
                                        authorities.add(perm.trim());
                                    }
                                }
                            }
                        }
                    }

                    // Department roles (부서 역할) 반영
                    if (user.getDepartmentId() != null) {
                        try {
                            Optional<Department> deptOpt = departmentRepository.findById(user.getDepartmentId());
                            if (deptOpt.isPresent()) {
                                Department dept = deptOpt.get();
                                if (dept.getRoles() != null && !dept.getRoles().isEmpty()) {
                                    for (String deptRole : dept.getRoles()) {
                                        if (deptRole != null && !deptRole.isBlank()) {
                                            String normalizedRole = deptRole.trim().startsWith("ROLE_")
                                                    ? deptRole.trim() : "ROLE_" + deptRole.trim();
                                            authorities.add(normalizedRole);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Failed to load department roles for user: {}", username, e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to load fine-grained permissions for user: {}", username, e);
            }
        }

        // 2. Role 마스터 테이블 전체에서 roleNamesToSearch와 일치하는 Role의 permissions 수집
        try {
            List<Role> allMasterRoles = roleRepository.findAll();
            for (Role role : allMasterRoles) {
                if (role != null && role.getName() != null && roleNamesToSearch.contains(role.getName())) {
                    if (role.getPermissions() != null) {
                        for (String perm : role.getPermissions()) {
                            if (perm != null && !perm.isBlank()) {
                                authorities.add(perm.trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to search master roles by name: {}", e.getMessage());
        }

        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRoles(User user) {
        Set<String> roles = new HashSet<>();
        if (user == null) return roles;

        if (user.getRole() != null && !user.getRole().isBlank()) {
            for (String r : user.getRole().split(",")) {
                String trimmed = r.trim();
                if (!trimmed.isEmpty()) {
                    roles.add(trimmed);
                    if (trimmed.startsWith("ROLE_")) {
                        roles.add(trimmed.substring(5));
                    } else {
                        roles.add("ROLE_" + trimmed);
                    }
                }
            }
        }

        try {
            List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
            if (userRoles != null && !userRoles.isEmpty()) {
                Set<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
                List<Role> explicitRoles = roleRepository.findAllById(roleIds);
                for (Role r : explicitRoles) {
                    if (r.getName() != null && !r.getName().isBlank()) {
                        String name = r.getName().trim();
                        roles.add(name);
                        if (name.startsWith("ROLE_")) {
                            roles.add(name.substring(5));
                        } else {
                            roles.add("ROLE_" + name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch UserRole mappings for user {}", user.getId(), e);
        }

        if (user.getDepartmentId() != null) {
            try {
                departmentRepository.findById(user.getDepartmentId()).ifPresent(dept -> {
                    if (dept.getRoles() != null) {
                        for (String r : dept.getRoles()) {
                            if (r != null && !r.isBlank()) {
                                String trimmed = r.trim();
                                roles.add(trimmed);
                                if (trimmed.startsWith("ROLE_")) {
                                    roles.add(trimmed.substring(5));
                                } else {
                                    roles.add("ROLE_" + trimmed);
                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                log.error("Failed to fetch department roles for user {}", user.getId(), e);
            }
        }

        return roles;
    }
}
