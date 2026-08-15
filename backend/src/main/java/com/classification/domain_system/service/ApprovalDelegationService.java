package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalDelegationDto;
import com.classification.domain_system.entity.ApprovalDelegation;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalDelegationRepository;
import com.classification.domain_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalDelegationService {

    private final ApprovalDelegationRepository delegationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApprovalDelegationDto createDelegation(ApprovalDelegationDto dto, String currentUserId) {
        String delegatorId = dto.getDelegatorUserId() != null ? dto.getDelegatorUserId() : currentUserId;
        if (dto.getDelegateeUserId() == null || dto.getDelegateeUserId().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Delegatee user ID is required.");
        }
        if (delegatorId.equals(dto.getDelegateeUserId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot delegate approvals to yourself.");
        }
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Start date and end date are required.");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "End date cannot be before start date.");
        }

        userRepository.findById(dto.getDelegateeUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Delegatee user not found: " + dto.getDelegateeUserId()));

        ApprovalDelegation delegation = new ApprovalDelegation();
        delegation.setDelegatorUserId(delegatorId);
        delegation.setDelegateeUserId(dto.getDelegateeUserId());
        delegation.setStartDate(dto.getStartDate());
        delegation.setEndDate(dto.getEndDate());
        delegation.setReason(dto.getReason());
        delegation.setIsActive(true);

        ApprovalDelegation saved = delegationRepository.save(delegation);
        return toDto(saved);
    }

    @Transactional
    public void revokeDelegation(UUID delegationId, String currentUserId) {
        ApprovalDelegation delegation = delegationRepository.findById(delegationId)
                .orElseThrow(() -> new ResourceNotFoundException("Delegation not found: " + delegationId));

        if (!delegation.getDelegatorUserId().equals(currentUserId) && !"admin".equalsIgnoreCase(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Only the delegator or admin can revoke this delegation.");
        }

        delegation.setIsActive(false);
        delegationRepository.save(delegation);
    }

    @Transactional(readOnly = true)
    public Map<String, List<ApprovalDelegationDto>> getMyDelegations(String userId) {
        List<ApprovalDelegation> byMe = delegationRepository.findByDelegatorUserIdOrderByCreatedAtDesc(userId);
        List<ApprovalDelegation> toMe = delegationRepository.findByDelegateeUserIdOrderByCreatedAtDesc(userId);

        Map<String, List<ApprovalDelegationDto>> result = new HashMap<>();
        result.put("delegatedByMe", byMe.stream().map(this::toDto).collect(Collectors.toList()));
        result.put("delegatedToMe", toMe.stream().map(this::toDto).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<String> getActiveDelegatorIdsForDelegatee(String delegateeUserId) {
        if (delegateeUserId == null) return Collections.emptyList();
        LocalDateTime now = LocalDateTime.now();
        List<ApprovalDelegation> active = delegationRepository.findActiveDelegationsForDelegatee(delegateeUserId, now);
        return active.stream().map(ApprovalDelegation::getDelegatorUserId).distinct().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isDelegatedApprover(String potentialDelegatee, String delegator) {
        if (potentialDelegatee == null || delegator == null) return false;
        LocalDateTime now = LocalDateTime.now();
        List<ApprovalDelegation> pair = delegationRepository.findActiveDelegationPair(delegator, potentialDelegatee, now);
        return !pair.isEmpty();
    }

    private ApprovalDelegationDto toDto(ApprovalDelegation entity) {
        String delegatorName = userRepository.findById(entity.getDelegatorUserId())
                .map(User::getUsername).orElse(entity.getDelegatorUserId());
        String delegateeName = userRepository.findById(entity.getDelegateeUserId())
                .map(User::getUsername).orElse(entity.getDelegateeUserId());

        return ApprovalDelegationDto.builder()
                .id(entity.getId())
                .delegatorUserId(entity.getDelegatorUserId())
                .delegatorUserName(delegatorName)
                .delegateeUserId(entity.getDelegateeUserId())
                .delegateeUserName(delegateeName)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reason(entity.getReason())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
