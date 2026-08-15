package com.classification.domain_system.service;

import com.classification.domain_system.controller.UserController.UserDto;
import com.classification.domain_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.classification.domain_system.dto.AdminUserUpdateDto;
import com.classification.domain_system.dto.SelfUserUpdateDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final com.classification.domain_system.repository.UserOrgHistoryRepository userOrgHistoryRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.classification.domain_system.service.FieldEncryptionService fieldEncryptionService;
    private final com.classification.domain_system.repository.OrganizationRepository organizationRepository;
    private final com.classification.domain_system.repository.DepartmentRepository departmentRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        // Cache to avoid N+1 issues
        java.util.Map<java.util.UUID, String> orgCache = new java.util.HashMap<>();
        java.util.Map<java.util.UUID, String> deptCache = new java.util.HashMap<>();

        return userRepository.findAll().stream()
                .map(u -> {
                    String orgName = null;
                    if (u.getOrganizationId() != null) {
                        orgName = orgCache.computeIfAbsent(u.getOrganizationId(), id -> 
                            organizationRepository.findById(id).map(o -> o.getDisplayName() != null ? o.getDisplayName() : o.getName()).orElse(null));
                    }
                    String deptName = null;
                    if (u.getDepartmentId() != null) {
                        deptName = deptCache.computeIfAbsent(u.getDepartmentId(), id -> 
                            departmentRepository.findById(id).map(com.classification.domain_system.entity.Department::getName).orElse(null));
                    }
                    return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.getOrganizationId(), u.getDepartmentId(), u.getTeamId(), u.getIsActive(), u.getMustChangePassword(), orgName, deptName);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public com.classification.domain_system.entity.User updateAdminUserInfo(String userId, AdminUserUpdateDto dto) {
        com.classification.domain_system.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        java.util.UUID prevOrgId = user.getOrganizationId();
        java.util.UUID prevDeptId = user.getDepartmentId();
        java.util.UUID prevTeamId = user.getTeamId();

        boolean orgChanged = false;
        if (dto.getOrganizationId() != null && !java.util.Objects.equals(prevOrgId, dto.getOrganizationId())) {
            user.setOrganizationId(dto.getOrganizationId());
            orgChanged = true;
        }
        if (!java.util.Objects.equals(prevDeptId, dto.getDepartmentId())) {
            user.setDepartmentId(dto.getDepartmentId());
            orgChanged = true;
        }
        if (dto.getTeamId() != null && !java.util.Objects.equals(prevTeamId, dto.getTeamId())) {
            user.setTeamId(dto.getTeamId());
            orgChanged = true;
        }
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getIsActive() != null) user.setIsActive(dto.getIsActive());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        com.classification.domain_system.entity.User savedUser = userRepository.save(user);

        if (orgChanged) {
            String currentOperator = "SYSTEM";
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    currentOperator = auth.getName();
                }
            } catch (Exception e) {
                // fallback
            }

            com.classification.domain_system.entity.UserOrgHistory hist = new com.classification.domain_system.entity.UserOrgHistory();
            hist.setUserId(userId);
            hist.setPrevOrganizationId(prevOrgId);
            hist.setPrevDepartmentId(prevDeptId);
            hist.setPrevTeamId(prevTeamId);
            hist.setNewOrganizationId(savedUser.getOrganizationId());
            hist.setNewDepartmentId(savedUser.getDepartmentId());
            hist.setNewTeamId(savedUser.getTeamId());
            hist.setChangedBy(currentOperator);
            userOrgHistoryRepository.save(hist);
        }

        return savedUser;
    }

    @Transactional
    public com.classification.domain_system.entity.User updateSelfUserInfo(String username, SelfUserUpdateDto dto) {
        com.classification.domain_system.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (dto != null && dto.getTimezone() != null) {
            user.setTimezone(dto.getTimezone());
        }
        return userRepository.save(user);
    }

    @Transactional
    public com.classification.domain_system.entity.User updateUserInfo(String userId, com.classification.domain_system.entity.User updateReq) {
        AdminUserUpdateDto dto = new AdminUserUpdateDto();
        dto.setRole(updateReq.getRole());
        dto.setOrganizationId(updateReq.getOrganizationId());
        dto.setDepartmentId(updateReq.getDepartmentId());
        dto.setTeamId(updateReq.getTeamId());
        dto.setIsActive(updateReq.getIsActive());
        dto.setEmail(updateReq.getEmail());
        return updateAdminUserInfo(userId, dto);
    }

    @Transactional
    public com.classification.domain_system.entity.User findByUsername(String username) {
        com.classification.domain_system.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return user;
    }

    public java.util.List<com.classification.domain_system.entity.User> getAllUsersEntity() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.classification.domain_system.entity.User> searchUsers(String search, org.springframework.data.domain.Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        String keyword = search.trim();
        return userRepository.findByUsernameContainingIgnoreCaseOrRoleContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Transactional
    public void updateTimezone(String username, String timezone) {
        com.classification.domain_system.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setTimezone(timezone);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(String userId, String role) {
        com.classification.domain_system.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public String createAdminUser(String username, String email, String role, java.util.UUID organizationId, java.util.UUID departmentId) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Resolve email fallback with Organization email domain
        String resolvedEmail = email != null ? email.trim() : "";
        if (resolvedEmail.isEmpty()) {
            String orgDomain = null;
            if (organizationId != null) {
                orgDomain = organizationRepository.findById(organizationId)
                        .map(com.classification.domain_system.entity.Organization::getEmailDomain)
                        .filter(d -> d != null && !d.trim().isEmpty())
                        .map(d -> d.trim().replaceAll("^@", ""))
                        .orElse(null);
            }
            if (orgDomain != null && !orgDomain.isEmpty()) {
                resolvedEmail = username + "@" + orgDomain;
            } else {
                resolvedEmail = username + "@example.com";
            }
        }

        // Generate temporary password
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        com.classification.domain_system.entity.User user = new com.classification.domain_system.entity.User();
        user.setUsername(username);
        user.setEmail(resolvedEmail);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setEncryptedTempPassword(fieldEncryptionService.encrypt(tempPassword));
        user.setRole(role != null && !role.trim().isEmpty() ? role : "ROLE_USER");
        user.setOrganizationId(organizationId);
        user.setDepartmentId(departmentId);
        user.setTimezone("Asia/Seoul");
        user.setMustChangePassword(true);
        
        userRepository.save(user);
        
        // Sync to Keycloak
        try {
            keycloakAdminService.createUser(username, tempPassword, resolvedEmail, username);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UserService.class).error("Failed to sync user creation to Keycloak: {}", username, e);
        }
        
        return tempPassword;
    }

    @Transactional
    public void changePassword(String userIdentifier, String oldPassword, String newPassword) {
        com.classification.domain_system.entity.User user = userRepository.findByUsername(userIdentifier)
                .or(() -> userRepository.findById(userIdentifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userIdentifier));
                
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setEncryptedTempPassword(null);
        userRepository.save(user);
        
        // Sync to Keycloak
        try {
            keycloakAdminService.resetPassword(user.getUsername(), newPassword);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UserService.class).error("Failed to sync password change to Keycloak: {}", user.getUsername(), e);
        }
    }

    @Transactional
    public void deleteUser(String userId) {
        com.classification.domain_system.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Remove history
        userOrgHistoryRepository.deleteByUserId(userId);
        
        try {
            String username = user.getUsername();
            userRepository.delete(user);
            
            // Sync to Keycloak
            try {
                keycloakAdminService.deleteUser(username);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(UserService.class).error("Failed to sync user deletion to Keycloak: {}", username, e);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException("Cannot delete user because linked data (such as records or approval history) exists. (Data integrity protection)", e);
        }
    }
}
