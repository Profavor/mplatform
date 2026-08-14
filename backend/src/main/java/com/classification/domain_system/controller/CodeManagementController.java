package com.classification.domain_system.controller;

import com.classification.domain_system.dto.CodeDetailRequest;
import com.classification.domain_system.dto.CodeGroupRequest;
import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.service.CodeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/code-groups")
@RequiredArgsConstructor
public class CodeManagementController {

    private final CodeManagementService codeManagementService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<com.classification.domain_system.dto.CodeGroupResponse> createGroup(@RequestBody CodeGroupRequest request) {
        CodeGroup group = codeManagementService.createGroup(request);
        return ResponseEntity.ok(com.classification.domain_system.dto.CodeGroupResponse.from(group));
    }

    @GetMapping
    public ResponseEntity<List<com.classification.domain_system.dto.CodeGroupResponse>> getGroups() {
        List<CodeGroup> groups = codeManagementService.getGroups();
        List<com.classification.domain_system.dto.CodeGroupResponse> response = groups.stream()
                .map(com.classification.domain_system.dto.CodeGroupResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<com.classification.domain_system.dto.CodeGroupResponse>> getGroupsPaged(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "groupCode,asc") String sort) {
        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortParams[0]));
        org.springframework.data.domain.Page<CodeGroup> pageResult = codeManagementService.getGroupsPaged(keyword, pageable);
        return ResponseEntity.ok(pageResult.map(com.classification.domain_system.dto.CodeGroupResponse::from));
    }

    @GetMapping("/{groupCode}")
    public ResponseEntity<com.classification.domain_system.dto.CodeGroupResponse> getGroupByCode(@PathVariable String groupCode) {
        CodeGroup group = codeManagementService.getGroupByCode(groupCode);
        return ResponseEntity.ok(com.classification.domain_system.dto.CodeGroupResponse.from(group));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<com.classification.domain_system.dto.CodeGroupResponse> updateGroup(@PathVariable UUID id, @RequestBody CodeGroupRequest request) {
        CodeGroup group = codeManagementService.updateGroup(id, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.CodeGroupResponse.from(group));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        codeManagementService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/details")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<com.classification.domain_system.dto.CodeDetailResponse> createDetail(@PathVariable UUID groupId, @RequestBody CodeDetailRequest request) {
        CodeDetail detail = codeManagementService.createDetail(groupId, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.CodeDetailResponse.from(detail));
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<List<com.classification.domain_system.dto.CodeDetailResponse>> getDetailsByGroup(@PathVariable UUID groupId) {
        List<CodeDetail> details = codeManagementService.getDetailsByGroup(groupId);
        List<com.classification.domain_system.dto.CodeDetailResponse> response = details.stream()
                .map(com.classification.domain_system.dto.CodeDetailResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/details/{detailId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<com.classification.domain_system.dto.CodeDetailResponse> updateDetail(@PathVariable UUID detailId, @RequestBody CodeDetailRequest request) {
        CodeDetail detail = codeManagementService.updateDetail(detailId, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.CodeDetailResponse.from(detail));
    }

    @DeleteMapping("/details/{detailId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> deleteDetail(@PathVariable UUID detailId) {
        codeManagementService.deleteDetail(detailId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/code/{groupCode}/details")
    public ResponseEntity<List<com.classification.domain_system.dto.CodeDetailResponse>> getActiveDetailsByGroupCode(@PathVariable String groupCode) {
        List<CodeDetail> details = codeManagementService.getActiveDetailsByGroupCode(groupCode);
        List<com.classification.domain_system.dto.CodeDetailResponse> response = details.stream()
                .map(com.classification.domain_system.dto.CodeDetailResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<List<com.classification.domain_system.dto.CodeExportDto>> exportCodes() {
        return ResponseEntity.ok(codeManagementService.exportCodes());
    }

    @PostMapping("/import")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> importCodes(@RequestBody List<com.classification.domain_system.dto.CodeExportDto> importData) {
        codeManagementService.importCodes(importData);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/dump-seed")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> dumpSeedFiles() {
        codeManagementService.dumpCodeStateToSeedFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-seed")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> syncSeedFiles() {
        codeManagementService.syncCodes();
        return ResponseEntity.ok().build();
    }
}
