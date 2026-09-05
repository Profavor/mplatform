package com.classification.domain_system.service;

import com.classification.domain_system.config.MdmProperties;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.CustomAccessDeniedException;
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
    private final MdmProperties mdmProperties;
    private final com.classification.domain_system.security.SecurityUtils securityUtils;
    private final InboxService inboxService;
    private final com.classification.domain_system.repository.UserRepository userRepository;

    private String resolveUsername(String username) {
        if (username != null && !username.isBlank()) {
            return username;
        }
        String authUser = securityUtils.getCurrentUserId();
        if (authUser != null) {
            return authUser;
        }
        throw new CustomAccessDeniedException("Unauthenticated user context");
    }

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
        candidate.setReviewedBy(resolveUsername(username));
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
        candidate.setReviewedBy(resolveUsername(username));
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
        candidate.setReviewedBy(resolveUsername(username));
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

    @Transactional
    public MatchCandidate createAndNotifyCandidate(
            UUID nodeId,
            UUID existingRecordId,
            String incomingDataJson,
            Double score,
            String matchedFieldDetails,
            UUID matchedRuleId,
            String source
    ) {
        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        UUID domainId = (node != null && node.getDomain() != null) ? node.getDomain().getId() : null;

        MatchCandidate candidate = new MatchCandidate();
        candidate.setNodeId(nodeId);
        candidate.setDomainId(domainId);
        candidate.setExistingRecordId(existingRecordId);
        candidate.setIncomingDataJson(incomingDataJson);
        candidate.setScore(score != null ? score : 1.0);
        candidate.setMatchedFieldDetails(matchedFieldDetails);
        candidate.setMatchedRuleId(matchedRuleId);
        candidate.setSource(source != null ? source : "MANUAL");
        candidate.setStatus("PENDING_REVIEW");

        MatchCandidate saved = candidateRepository.save(candidate);

        // Notify stewards and admins via Inbox (#148)
        try {
            if (inboxService != null) {
                String domainName = "기본 도메인";
                if (node != null && node.getDomain() != null && node.getDomain().getName() != null) {
                    domainName = node.getDomain().getName().getOrDefault("ko",
                            node.getDomain().getName().values().stream().findFirst().orElse("도메인"));
                }
                String candCode = "CAND-" + saved.getId().toString().substring(0, 8);
                String recCode = "REC-" + existingRecordId.toString().substring(0, 8);

                List<String> recipients = new java.util.ArrayList<>();
                if (userRepository != null) {
                    userRepository.findAll().stream()
                            .filter(u -> "admin".equalsIgnoreCase(u.getUsername()) || "ROLE_ADMIN".equalsIgnoreCase(u.getRole()))
                            .map(com.classification.domain_system.entity.User::getUsername)
                            .filter(java.util.Objects::nonNull)
                            .forEach(recipients::add);
                }
                if (recipients.isEmpty()) {
                    recipients.add("admin");
                }

                String subject = String.format("[중복 검토 요청] 도메인 '%s'에 중복 의심 레코드 후보가 등록되었습니다.", domainName);
                String body = String.format(
                        "<div style='font-family: sans-serif; line-height: 1.6;'>" +
                        "<h3>중복 의심 레코드 검토 알림</h3>" +
                        "<p>도메인 <strong>%s</strong>에서 신규 레코드 입력 중 기존 데이터와의 유사도가 높은 중복 후보가 감지되었습니다.</p>" +
                        "<ul>" +
                        "<li><strong>후보 식별 코드:</strong> %s</li>" +
                        "<li><strong>기존 레코드:</strong> %s</li>" +
                        "<li><strong>유사도 점수:</strong> %.1f%%</li>" +
                        "<li><strong>매칭 사유:</strong> %s</li>" +
                        "</ul>" +
                        "<p><a href='/admin/match-review?candidateId=%s' style='display:inline-block; padding: 8px 16px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 4px;'>중복 검토 화면으로 이동</a></p>" +
                        "</div>",
                        domainName, candCode, recCode, (saved.getScore() * 100),
                        matchedFieldDetails != null ? matchedFieldDetails : "유사도 규칙 매칭",
                        saved.getId()
                );

                com.classification.domain_system.dto.InboxMessageRequest msgReq = com.classification.domain_system.dto.InboxMessageRequest.builder()
                        .subject(subject)
                        .body(body)
                        .importance("HIGH")
                        .messageType("MATCH_REVIEW")
                        .toRecipients(recipients)
                        .isDraft(false)
                        .build();

                inboxService.sendMessage(msgReq, "system");
                log.info("[MatchCandidate] Sent inbox notification for candidate {} to {}", candCode, recipients);
            }
        } catch (Exception e) {
            log.warn("[MatchCandidate] Failed to send inbox notification for candidate {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }
}

