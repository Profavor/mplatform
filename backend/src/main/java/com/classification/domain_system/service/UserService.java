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

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getRole(), u.getOrganizationId(), u.getDepartmentId(), u.getTeamId(), u.getIsActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    public com.classification.domain_system.entity.User updateAdminUserInfo(String userId, AdminUserUpdateDto dto) {
        com.classification.domain_system.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getOrganizationId() != null) user.setOrganizationId(dto.getOrganizationId());
        user.setDepartmentId(dto.getDepartmentId());
        if (dto.getTeamId() != null) user.setTeamId(dto.getTeamId());
        if (dto.getIsActive() != null) user.setIsActive(dto.getIsActive());
        return userRepository.save(user);
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
}
