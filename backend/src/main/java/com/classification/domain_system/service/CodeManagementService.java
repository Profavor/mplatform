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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeManagementService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeDetailRepository codeDetailRepository;

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
}
