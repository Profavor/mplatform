package com.classification.domain_system.service;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchCandidateService {

    private final MatchCandidateRepository candidateRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final RecordMergeService recordMergeService;

    @Transactional(readOnly = true)
    public PageResponse<MatchCandidate> getCandidatesByDomain(UUID domainId, String status, int page, int size) {
        List<ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        if (nodes == null || nodes.isEmpty()) {
            return PageResponse.of(Page.empty());
        }

        List<UUID> nodeIds = nodes.stream().map(ClassificationNode::getId).toList();
        if (nodeIds.isEmpty()) {
            return PageResponse.of(Page.empty());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MatchCandidate> candidatePage;

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            candidatePage = candidateRepository.findByNodeIdInAndStatus(nodeIds, status, pageable);
        } else {
            candidatePage = candidateRepository.findByNodeIdIn(nodeIds, pageable);
        }

        return PageResponse.of(candidatePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<MatchCandidate> getCandidatesByDomain(UUID domainId, int page, int size) {
        return getCandidatesByDomain(domainId, null, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<MatchCandidate> getAllCandidates(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MatchCandidate> candidatePage;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            candidatePage = candidateRepository.findByStatus(status, pageable);
        } else {
            candidatePage = candidateRepository.findAll(pageable);
        }
        return PageResponse.of(candidatePage);
    }

    @Transactional
    public MatchCandidate confirmCandidate(UUID candidateId, RecordMergeService.MergeRequest mergeReq, String username) {
        MatchCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_CANDIDATE_NOT_FOUND, "Match candidate not found"));

        if (!"PENDING_REVIEW".equalsIgnoreCase(candidate.getStatus())) {
            throw new BusinessException(ErrorCode.MATCH_CANDIDATE_ALREADY_RESOLVED, "Candidate already resolved.");
        }

        candidate.setStatus("CONFIRMED_MERGE");
        candidate.setReviewedBy(username != null ? username : "STEWARD");
        candidate.setReviewedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        if (mergeReq != null) {
            recordMergeService.mergeRecords(mergeReq, username);
        }
        return candidate;
    }

    @Transactional
    public Record rejectCandidate(UUID candidateId, String username) {
        MatchCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_CANDIDATE_NOT_FOUND, "Match candidate not found"));

        if (!"PENDING_REVIEW".equalsIgnoreCase(candidate.getStatus())) {
            throw new BusinessException(ErrorCode.MATCH_CANDIDATE_ALREADY_RESOLVED, "Candidate already resolved.");
        }

        candidate.setStatus("REJECTED");
        candidate.setReviewedBy(username != null ? username : "STEWARD");
        candidate.setReviewedAt(LocalDateTime.now());
        candidateRepository.save(candidate);

        // 반려 시 신규 레코드로 그대로 생성
        ClassificationNode node = nodeRepository.findById(candidate.getNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Node not found"));

        Record record = new Record();
        record.setNode(node);
        record.setStatus("ACTIVE");
        record.setSourceSystem(candidate.getSource());
        record.setData(candidate.getIncomingDataJson());
        return recordRepository.save(record);
    }

    @Transactional
    public MatchCandidate ignoreCandidate(UUID candidateId, String username) {
        MatchCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_CANDIDATE_NOT_FOUND, "Match candidate not found"));

        if (!"PENDING_REVIEW".equalsIgnoreCase(candidate.getStatus())) {
            throw new BusinessException(ErrorCode.MATCH_CANDIDATE_ALREADY_RESOLVED, "Candidate already resolved.");
        }

        candidate.setStatus("IGNORED");
        candidate.setReviewedBy(username != null ? username : "STEWARD");
        candidate.setReviewedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    @Transactional
    public void batchConfirmCandidates(List<UUID> ids, UUID domainId, String username) {
        if (ids == null || ids.isEmpty()) return;
        for (UUID id : ids) {
            try {
                confirmCandidate(id, null, username);
            } catch (Exception e) {
                log.warn("Failed to confirm candidate in batch: {}", id, e);
            }
        }
    }

    @Transactional
    public void batchRejectCandidates(List<UUID> ids, String username) {
        if (ids == null || ids.isEmpty()) return;
        for (UUID id : ids) {
            try {
                rejectCandidate(id, username);
            } catch (Exception e) {
                log.warn("Failed to reject candidate in batch: {}", id, e);
            }
        }
    }
}

