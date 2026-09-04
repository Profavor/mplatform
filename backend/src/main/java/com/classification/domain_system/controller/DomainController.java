package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.service.DomainService;
import com.classification.domain_system.dto.DomainRequest;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.dto.FieldDefinitionRequest;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.SectorService;
import com.classification.domain_system.service.FieldGroupService;
import com.classification.domain_system.dto.SectorRequest;
import com.classification.domain_system.dto.FieldGroupRequest;
import com.classification.domain_system.entity.Sector;
import com.classification.domain_system.entity.FieldGroup;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
public class DomainController {
    
    private final DomainService domainService;
    private final FieldDefinitionService fieldService;
    private final SectorService sectorService;
    private final FieldGroupService fieldGroupService;
    private final com.classification.domain_system.service.DomainPackageService domainPackageService;
    private final com.classification.domain_system.service.SpecializedDomainTemplateService specializedDomainTemplateService;
    private final com.classification.domain_system.service.StockDataIngestionService stockDataIngestionService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private com.classification.domain_system.service.RecordService recordService;

    public void setRecordService(com.classification.domain_system.service.RecordService recordService) {
        this.recordService = recordService;
    }
    
    @PostMapping
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.DomainResponse> createDomain(@RequestBody DomainRequest request) {
        Domain domain = domainService.createDomain(request);
        return ResponseEntity.ok(com.classification.domain_system.dto.DomainResponse.from(domain));
    }
    
    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<com.classification.domain_system.dto.DomainResponse>> getAllDomains() {
        List<Domain> domains = domainService.getAllDomains();
        List<com.classification.domain_system.dto.DomainResponse> response = domains.stream()
                .map(com.classification.domain_system.dto.DomainResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.DomainResponse> updateDomain(
            @PathVariable UUID id, 
            @RequestBody DomainRequest request) {
        Domain domain = domainService.updateDomain(id, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.DomainResponse.from(domain));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'domain:*')")
    public ResponseEntity<Void> deleteDomain(@PathVariable UUID id) {
        domainService.deleteDomain(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{domainId}/records")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'record:delete') or hasPermission(null, 'domain:*') or hasPermission(null, 'record:*')")
    public ResponseEntity<java.util.Map<String, Object>> resetDomainRecords(@PathVariable UUID domainId) {
        int deletedCount = recordService.resetDomainRecords(domainId);
        return ResponseEntity.ok(java.util.Map.of("success", true, "deletedCount", deletedCount));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<com.classification.domain_system.dto.DomainResponse> getDomain(@PathVariable UUID id) {
        Domain domain = domainService.getDomain(id);
        return ResponseEntity.ok(com.classification.domain_system.dto.DomainResponse.from(domain));
    }
    
    
    @GetMapping("/{domainId}/fields/page")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<PageResponse<com.classification.domain_system.dto.FieldDefinitionResponse>> getDomainFieldsPage(
            @PathVariable UUID domainId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        List<FieldDefinition> allFields = fieldService.getDomainFields(domainId);
        int start = Math.min(page * size, allFields.size());
        int end = Math.min((page + 1) * size, allFields.size());
        List<com.classification.domain_system.dto.FieldDefinitionResponse> content = allFields.subList(start, end)
                .stream()
                .map(com.classification.domain_system.dto.FieldDefinitionResponse::from)
                .toList();
        Page<com.classification.domain_system.dto.FieldDefinitionResponse> p = new PageImpl<>(content, PageRequest.of(page, size), allFields.size());
        return ResponseEntity.ok(PageResponse.of(p));
    }
    
    @GetMapping("/{domainId}/fields")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<com.classification.domain_system.dto.FieldDefinitionResponse>> getDomainFields(@PathVariable UUID domainId) {
        List<com.classification.domain_system.dto.FieldDefinitionResponse> response = fieldService.getDomainFields(domainId)
                .stream()
                .map(com.classification.domain_system.dto.FieldDefinitionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{domainId}/fields")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.FieldDefinitionResponse> addDomainField(
            @PathVariable UUID domainId,
            @RequestBody FieldDefinitionRequest request) {
        FieldDefinition created = fieldService.addDomainField(domainId, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.FieldDefinitionResponse.from(created));
    }
    
    @PutMapping("/{domainId}/fields/{fieldId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.FieldDefinitionResponse> updateDomainField(
            @PathVariable UUID domainId,
            @PathVariable UUID fieldId,
            @RequestBody FieldDefinitionRequest request) {
        FieldDefinition updated = fieldService.updateDomainField(domainId, fieldId, request);
        return ResponseEntity.ok(com.classification.domain_system.dto.FieldDefinitionResponse.from(updated));
    }

    @DeleteMapping("/{domainId}/fields/{fieldId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> deleteDomainField(
            @PathVariable UUID domainId,
            @PathVariable UUID fieldId,
            @RequestParam(required = false) String reason) {
        fieldService.deleteDomainField(domainId, fieldId, false, reason);
        return ResponseEntity.noContent().build();
    }

    // Sectors
    @GetMapping("/{domainId}/sectors")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<Sector>> getSectors(@PathVariable UUID domainId) {
        return ResponseEntity.ok(sectorService.getSectorsByDomain(domainId));
    }

    @PostMapping("/{domainId}/sectors")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Sector> createSector(
            @PathVariable UUID domainId,
            @RequestBody SectorRequest request) {
        return ResponseEntity.ok(sectorService.createSector(domainId, request));
    }

    @PutMapping("/{domainId}/sectors/{sectorId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Sector> updateSector(
            @PathVariable UUID domainId, // for path consistency
            @PathVariable UUID sectorId,
            @RequestBody SectorRequest request) {
        return ResponseEntity.ok(sectorService.updateSector(sectorId, request));
    }

    @DeleteMapping("/{domainId}/sectors/{sectorId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> deleteSector(
            @PathVariable UUID domainId,
            @PathVariable UUID sectorId) {
        sectorService.deleteSector(sectorId);
        return ResponseEntity.ok().build();
    }

    private final com.classification.domain_system.service.dq.DqRuleEngine dqRuleEngine;
    private final com.classification.domain_system.repository.DqRuleRepository dqRuleRepository;
    private final com.classification.domain_system.service.DqScoreSnapshotService dqScoreSnapshotService;

    // FieldGroups
    @GetMapping("/{domainId}/groups")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<FieldGroup>> getGroups(@PathVariable UUID domainId) {
        return ResponseEntity.ok(fieldGroupService.getGroupsByDomain(domainId));
    }

    @PostMapping("/{domainId}/groups")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<FieldGroup> createGroup(
            @PathVariable UUID domainId,
            @RequestBody FieldGroupRequest request) {
        return ResponseEntity.ok(fieldGroupService.createGroup(domainId, request));
    }

    @PutMapping("/{domainId}/groups/{groupId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<FieldGroup> updateGroup(
            @PathVariable UUID domainId,
            @PathVariable UUID groupId,
            @RequestBody FieldGroupRequest request) {
        return ResponseEntity.ok(fieldGroupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{domainId}/groups/{groupId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable UUID domainId,
            @PathVariable UUID groupId) {
        fieldGroupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    // ─── DQ Domain Endpoints ──────────────────────────────────────────

    @GetMapping("/{domainId}/dq-score")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<java.util.Map<String, Object>> getDomainDqScore(@PathVariable UUID domainId) {
        return ResponseEntity.ok(dqRuleEngine.getDomainDqScore(domainId));
    }

    @PostMapping("/{domainId}/dq-scan")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<java.util.Map<String, Object>> runDomainDqScan(@PathVariable UUID domainId) {
        java.util.Map<String, Object> scoreData = dqRuleEngine.runDomainDqScan(domainId);
        dqScoreSnapshotService.recordSnapshot(domainId, scoreData, "MANUAL");
        return ResponseEntity.ok(scoreData);
    }

    @GetMapping("/{domainId}/dq-rules-count")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<java.util.Map<String, Long>> getDomainDqRulesCount(@PathVariable UUID domainId) {
        long count = dqRuleRepository.countByDomainId(domainId);
        return ResponseEntity.ok(java.util.Map.of("count", count));
    }

    @GetMapping("/{domainId}/dq-violations")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<PageResponse<com.classification.domain_system.dto.DqViolationResponse>> getDomainDqViolations(
            @PathVariable UUID domainId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String fieldKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dqRuleEngine.getDomainDqViolations(domainId, severity, fieldKey, PageRequest.of(page, size)));
    }

    @GetMapping("/{domainId}/dq-score/trend")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<java.util.List<com.classification.domain_system.entity.DqScoreSnapshot>> getDqScoreTrend(
            @PathVariable UUID domainId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to) {
        return ResponseEntity.ok(dqScoreSnapshotService.getTrend(domainId, from, to));
    }

    @GetMapping("/{domainId}/dq-score/recent")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<java.util.List<com.classification.domain_system.entity.DqScoreSnapshot>> getDqScoreRecent(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(dqScoreSnapshotService.getRecentSnapshots(domainId));
    }

    @GetMapping("/{domainId}/package/export")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<com.classification.domain_system.dto.DomainPackageDto> exportDomainPackage(
            @PathVariable UUID domainId) {
        String currentUserId = "system";
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            currentUserId = auth.getName();
        }
        com.classification.domain_system.dto.DomainPackageDto pkg = domainPackageService.exportDomainPackage(domainId, currentUserId);
        return ResponseEntity.ok(pkg);
    }

    @PostMapping("/package/import")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'admin:write')")
    public ResponseEntity<com.classification.domain_system.dto.DomainPackageImportResult> importDomainPackage(
            @RequestBody com.classification.domain_system.dto.DomainPackageDto pkg,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        String currentUserId = "system";
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            currentUserId = auth.getName();
        }
        com.classification.domain_system.dto.DomainPackageImportResult result = domainPackageService.importDomainPackage(pkg, currentUserId, overwrite);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{domainId}/layout")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<java.util.Map<String, Object>> getDomainLayout(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(domainService.getDomainLayout(domainId));
    }

    @PutMapping("/{domainId}/layout")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<java.util.Map<String, Object>> saveDomainLayout(
            @PathVariable UUID domainId,
            @RequestBody com.classification.domain_system.dto.RecordLayoutDto request) {
        return ResponseEntity.ok(domainService.saveDomainLayout(domainId, request));
    }

    @GetMapping("/specialized-templates")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<com.classification.domain_system.dto.SpecializedDomainTemplateDto>> getSpecializedTemplates() {
        return ResponseEntity.ok(specializedDomainTemplateService.getTemplates());
    }

    @PostMapping("/specialized-provision")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.DomainResponse> provisionSpecializedDomain(
            @RequestBody com.classification.domain_system.dto.SpecializedDomainProvisionRequest request) {
        return ResponseEntity.ok(specializedDomainTemplateService.provisionDomain(request));
    }

    @PostMapping("/specialized-stock/seed-real-data")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<com.classification.domain_system.dto.StockSeedResponse> seedRealStockData(
            @RequestBody(required = false) com.classification.domain_system.dto.StockSeedRequest request,
            java.security.Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(stockDataIngestionService.seedRealStockData(request, username));
    }
}

