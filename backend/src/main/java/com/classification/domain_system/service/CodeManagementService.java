package com.classification.domain_system.service;

import com.classification.domain_system.dto.CodeDetailRequest;
import com.classification.domain_system.dto.CodeGroupRequest;
import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.repository.CodeDetailRepository;
import com.classification.domain_system.repository.CodeGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.classification.domain_system.dto.CodeExportDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeManagementService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeDetailRepository codeDetailRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void dumpCodeStateToSeedFiles() {
        try {
            String userDir = System.getProperty("user.dir");
            java.io.File resourcesDir = java.nio.file.Paths.get(userDir, "src", "main", "resources").toFile();
            if (!resourcesDir.exists()) {
                org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).warn("Cannot dump seed files: src/main/resources not found.");
                return;
            }
            List<CodeExportDto> codes = exportCodes();
            java.io.File codesFile = new java.io.File(resourcesDir, "default_codes.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(codesFile, codes);
            org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).info("Dumped codes to {}", codesFile.getAbsolutePath());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).error("Failed to dump code seed files", e);
            throw new RuntimeException("Failed to dump code seed files", e);
        }
    }

    @Transactional
    public void syncCodes() {
        try {
            org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).info("Syncing system codes from default_codes.json...");
            
            java.io.InputStream is = null;
            String userDir = System.getProperty("user.dir");
            java.io.File localFile = java.nio.file.Paths.get(userDir, "src", "main", "resources", "default_codes.json").toFile();
            
            if (localFile.exists()) {
                is = new java.io.FileInputStream(localFile);
                org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).info("Using local filesystem seed: {}", localFile.getAbsolutePath());
            } else {
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("default_codes.json");
                if (resource.exists()) {
                    is = resource.getInputStream();
                    org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).info("Using classpath seed");
                }
            }
            
            if (is == null) {
                org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).warn("default_codes.json not found in resources!");
                return;
            }
            
            List<CodeExportDto> defaultCodes;
            try (java.io.InputStream finalIs = is) {
                defaultCodes = objectMapper.readValue(finalIs, new com.fasterxml.jackson.core.type.TypeReference<List<CodeExportDto>>() {});
            }
            if (defaultCodes == null || defaultCodes.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).warn("No default codes found in default_codes.json!");
                return;
            }
            importCodes(defaultCodes);
            org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).info("System codes sync completed successfully.");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CodeManagementService.class).error("Failed to sync code data", e);
            throw new RuntimeException("Code sync failed", e);
        }
    }

    @Transactional
    public CodeGroup createGroup(CodeGroupRequest request) {
        CodeGroup group = new CodeGroup();
        group.setGroupCode(request.getGroupCode());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return codeGroupRepository.save(group);
    }

    @Transactional
    public CodeGroup updateGroup(UUID id, CodeGroupRequest request) {
        CodeGroup group = codeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CodeGroup not found"));
        if (request.getGroupCode() != null) group.setGroupCode(request.getGroupCode());
        if (request.getName() != null) group.setName(request.getName());
        if (request.getDescription() != null) group.setDescription(request.getDescription());
        if (request.getIsActive() != null) group.setIsActive(request.getIsActive());
        return codeGroupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(UUID id) {
        CodeGroup group = codeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CodeGroup not found"));
        // Explicit delete of associated details
        List<CodeDetail> details = codeDetailRepository.findByCodeGroupIdOrderBySortOrderAsc(id);
        codeDetailRepository.deleteAll(details);
        codeGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public List<CodeGroup> getGroups() {
        return codeGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CodeGroup> getGroupsPaged(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return codeGroupRepository.searchByKeyword(keyword, pageable);
        }
        return codeGroupRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public CodeGroup getGroupByCode(String groupCode) {
        return codeGroupRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new RuntimeException("CodeGroup not found"));
    }

    @Transactional
    public CodeDetail createDetail(UUID groupId, CodeDetailRequest request) {
        CodeGroup group = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("CodeGroup not found"));
        CodeDetail detail = new CodeDetail();
        detail.setCodeGroup(group);
        detail.setDetailCode(request.getDetailCode());
        detail.setName(request.getName());
        detail.setSortOrder(request.getSortOrder());
        detail.setValidFrom(request.getValidFrom());
        detail.setValidTo(request.getValidTo());
        detail.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return codeDetailRepository.save(detail);
    }

    @Transactional
    public CodeDetail updateDetail(UUID detailId, CodeDetailRequest request) {
        CodeDetail detail = codeDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("CodeDetail not found"));
        if (request.getDetailCode() != null) detail.setDetailCode(request.getDetailCode());
        if (request.getName() != null) detail.setName(request.getName());
        if (request.getSortOrder() != null) detail.setSortOrder(request.getSortOrder());
        detail.setValidFrom(request.getValidFrom());
        detail.setValidTo(request.getValidTo());
        if (request.getIsActive() != null) detail.setIsActive(request.getIsActive());
        return codeDetailRepository.save(detail);
    }

    @Transactional
    public void deleteDetail(UUID detailId) {
        CodeDetail detail = codeDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("CodeDetail not found"));
        codeDetailRepository.delete(detail);
    }

    @Transactional(readOnly = true)
    public List<CodeDetail> getDetailsByGroup(UUID groupId) {
        return codeDetailRepository.findByCodeGroupIdOrderBySortOrderAsc(groupId);
    }

    @Transactional(readOnly = true)
    public List<CodeDetail> getActiveDetailsByGroupCode(String groupCode) {
        List<CodeDetail> details = codeDetailRepository.findByCodeGroupGroupCode(groupCode);
        LocalDate now = LocalDate.now();
        return details.stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .filter(d -> d.getValidFrom() == null || !now.isBefore(d.getValidFrom()))
                .filter(d -> d.getValidTo() == null || !now.isAfter(d.getValidTo()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CodeExportDto> exportCodes() {
        List<CodeGroup> groups = codeGroupRepository.findAll();
        List<CodeExportDto> exportList = new ArrayList<>();
        
        for (CodeGroup group : groups) {
            CodeExportDto dto = new CodeExportDto();
            
            dto.setGroupCode(group.getGroupCode());
            dto.setName(group.getName());
            dto.setDescription(group.getDescription());
            dto.setIsActive(group.getIsActive());
            
            List<CodeDetail> details = codeDetailRepository.findByCodeGroupIdOrderBySortOrderAsc(group.getId());
            List<CodeDetailRequest> detailReqs = details.stream().map(d -> {
                CodeDetailRequest req = new CodeDetailRequest();
                req.setDetailCode(d.getDetailCode());
                req.setName(d.getName());
                req.setSortOrder(d.getSortOrder());
                req.setValidFrom(d.getValidFrom());
                req.setValidTo(d.getValidTo());
                req.setIsActive(d.getIsActive());
                return req;
            }).collect(Collectors.toList());
            
            dto.setDetails(detailReqs);
            exportList.add(dto);
        }
        return exportList;
    }

    @Transactional
    public void importCodes(List<CodeExportDto> importData) {
        for (CodeExportDto dto : importData) {
            CodeExportDto groupReq = dto;
            
            Optional<CodeGroup> existingGroupOpt = codeGroupRepository.findByGroupCode(groupReq.getGroupCode());
            CodeGroup group;
            if (existingGroupOpt.isPresent()) {
                group = existingGroupOpt.get();
                group.setName(groupReq.getName());
                group.setDescription(groupReq.getDescription());
                group.setIsActive(groupReq.getIsActive() != null ? groupReq.getIsActive() : true);
            } else {
                group = new CodeGroup();
                group.setGroupCode(groupReq.getGroupCode());
                group.setName(groupReq.getName());
                group.setDescription(groupReq.getDescription());
                group.setIsActive(groupReq.getIsActive() != null ? groupReq.getIsActive() : true);
            }
            group = codeGroupRepository.save(group);
            
            if (dto.getDetails() != null) {
                List<CodeDetail> existingDetails = codeDetailRepository.findByCodeGroupIdOrderBySortOrderAsc(group.getId());
                java.util.Map<String, CodeDetail> existingDetailMap = existingDetails.stream()
                        .collect(java.util.stream.Collectors.toMap(CodeDetail::getDetailCode, d -> d));
                
                for (CodeDetailRequest detailReq : dto.getDetails()) {
                    CodeDetail detail = existingDetailMap.remove(detailReq.getDetailCode());
                    if (detail == null) {
                        detail = new CodeDetail();
                        detail.setCodeGroup(group);
                        detail.setDetailCode(detailReq.getDetailCode());
                    }
                    detail.setName(detailReq.getName());
                    detail.setSortOrder(detailReq.getSortOrder());
                    detail.setValidFrom(detailReq.getValidFrom());
                    detail.setValidTo(detailReq.getValidTo());
                    detail.setIsActive(detailReq.getIsActive() != null ? detailReq.getIsActive() : true);
                    codeDetailRepository.save(detail);
                }
                
                // Delete explicitly any remaining details that were not in the import data
                for (CodeDetail remainingDetail : existingDetailMap.values()) {
                    codeDetailRepository.delete(remainingDetail);
                }
            }
        }
    }
}
