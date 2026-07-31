package com.classification.domain_system.controller;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

import com.classification.domain_system.dto.AdminUserUpdateDto;
import com.classification.domain_system.dto.SelfUserUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'user:read')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateSelfUser(@RequestBody SelfUserUpdateDto updateReq) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.updateSelfUserInfo(username, updateReq);
        return ResponseEntity.ok(u);
    }

    @Autowired
    private com.classification.domain_system.repository.UserOrgHistoryRepository userOrgHistoryRepository;
    @Autowired
    private com.classification.domain_system.repository.OrganizationRepository organizationRepository;
    @Autowired
    private com.classification.domain_system.repository.DepartmentRepository departmentRepository;
    @Autowired
    private com.classification.domain_system.repository.TeamRepository teamRepository;

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'user:write')")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody AdminUserUpdateDto updateReq) {
        User u = userService.updateAdminUserInfo(id, updateReq);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/{id}/org-history")
    @PreAuthorize("hasPermission(null, 'user:read')")
    public ResponseEntity<List<Map<String, Object>>> getUserOrgHistory(@PathVariable String id) {
        List<com.classification.domain_system.entity.UserOrgHistory> list = userOrgHistoryRepository.findByUserIdOrderByChangedAtDesc(id);
        List<Map<String, Object>> result = list.stream().map(oh -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", oh.getId());
            item.put("userId", oh.getUserId());
            item.put("changedAt", oh.getChangedAt());
            item.put("changedBy", oh.getChangedBy());

            String prevOrg = oh.getPrevOrganizationId() != null ? organizationRepository.findById(oh.getPrevOrganizationId()).map(o -> o.getDisplayName() != null ? o.getDisplayName() : o.getName()).orElse(null) : null;
            String prevDept = oh.getPrevDepartmentId() != null ? departmentRepository.findById(oh.getPrevDepartmentId()).map(com.classification.domain_system.entity.Department::getName).orElse(null) : null;
            String prevTeam = oh.getPrevTeamId() != null ? teamRepository.findById(oh.getPrevTeamId()).map(com.classification.domain_system.entity.Team::getName).orElse(null) : null;

            String newOrg = oh.getNewOrganizationId() != null ? organizationRepository.findById(oh.getNewOrganizationId()).map(o -> o.getDisplayName() != null ? o.getDisplayName() : o.getName()).orElse(null) : null;
            String newDept = oh.getNewDepartmentId() != null ? departmentRepository.findById(oh.getNewDepartmentId()).map(com.classification.domain_system.entity.Department::getName).orElse(null) : null;
            String newTeam = oh.getNewTeamId() != null ? teamRepository.findById(oh.getNewTeamId()).map(com.classification.domain_system.entity.Team::getName).orElse(null) : null;

            item.put("prevOrganizationName", prevOrg);
            item.put("prevDepartmentName", prevDept);
            item.put("prevTeamName", prevTeam);
            item.put("newOrganizationName", newOrg);
            item.put("newDepartmentName", newDept);
            item.put("newTeamName", newTeam);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/map")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getUserMap() {
        List<UserDto> users = userService.getAllUsers();
        Map<String, String> map = new HashMap<>();
        for (UserDto u : users) {
            if (u.id != null) map.put(u.id, u.username != null ? u.username : u.id);
            if (u.username != null) map.put(u.username, u.username);
        }
        return ResponseEntity.ok(map);
    }
    
    public static class UserDto {
        public String id;
        public String username;
        public String role;
        public java.util.UUID organizationId;
        public java.util.UUID departmentId;
        public java.util.UUID teamId;
        public Boolean isActive;
        
        public UserDto(String id, String username, String role) {
            this(id, username, role, null, null, null, true);
        }

        public UserDto(String id, String username, String role, java.util.UUID organizationId, java.util.UUID departmentId, java.util.UUID teamId, Boolean isActive) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.organizationId = organizationId;
            this.departmentId = departmentId;
            this.teamId = teamId;
            this.isActive = isActive;
        }
    }

    @PostMapping("/timezone")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateTimezone(@RequestBody TimezoneRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateTimezone(username, request.getTimezone());
        return ResponseEntity.ok().build();
    }

    public static class TimezoneRequest {
        private String timezone;
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }
}
