package com.classification.domain_system.controller;

import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/records")
public class RecordHistoryController {

    @Autowired
    private RecordHistoryRepository recordHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    @Autowired
    private com.classification.domain_system.repository.UserOrgHistoryRepository userOrgHistoryRepository;

    @Autowired
    private com.classification.domain_system.repository.OrganizationRepository organizationRepository;

    @Autowired
    private com.classification.domain_system.repository.DepartmentRepository departmentRepository;

    @Autowired
    private com.classification.domain_system.repository.TeamRepository teamRepository;

    @GetMapping("/{id}/history")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<List<Map<String, Object>>> getRecordHistory(@PathVariable UUID id) {
        List<RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByChangedAtDesc(id);

        // Collect unique changedBy IDs and resolve to usernames
        Set<String> userIds = histories.stream()
                .map(RecordHistory::getChangedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Map<String, Object>> userProfileMap = new HashMap<>();
        for (String uid : userIds) {
            userRepository.findById(uid).ifPresent(u -> {
                Map<String, Object> prof = new LinkedHashMap<>();
                prof.put("id", u.getId());
                prof.put("username", u.getUsername());
                prof.put("role", u.getRole());
                prof.put("timezone", u.getTimezone());
                prof.put("organizationName", u.getOrganization() != null ? (u.getOrganization().getDisplayName() != null ? u.getOrganization().getDisplayName() : u.getOrganization().getName()) : null);
                prof.put("departmentName", u.getDepartment() != null ? u.getDepartment().getName() : null);
                prof.put("teamName", u.getTeam() != null ? u.getTeam().getName() : null);
                
                java.util.List<com.classification.domain_system.entity.UserRole> uRoles = userRoleRepository.findByUserId(u.getId());
                java.time.LocalDateTime assignedAt = uRoles.stream()
                        .map(com.classification.domain_system.entity.UserRole::getGrantedAt)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(u.getOrganization() != null ? u.getOrganization().getCreatedAt() : null);
                prof.put("assignedAt", assignedAt);

                userProfileMap.put(uid, prof);
            });
        }

        List<Map<String, Object>> result = histories.stream().map(h -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("recordId", h.getRecordId());
            map.put("changeType", h.getChangeType());
            map.put("changedBy", h.getChangedBy());
            Map<String, Object> prof = userProfileMap.get(h.getChangedBy());
            map.put("changedByName", prof != null && prof.get("username") != null ? prof.get("username") : h.getChangedBy());
            map.put("changedUserProfile", prof);
            map.put("previousData", h.getPreviousData());
            map.put("newData", h.getNewData());
            map.put("approvalRequestId", h.getApprovalRequestId());
            map.put("version", h.getVersion());
            map.put("sourceSystem", h.getSourceSystem());
            map.put("changedAt", h.getChangedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
