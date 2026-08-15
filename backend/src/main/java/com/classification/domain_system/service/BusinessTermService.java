package com.classification.domain_system.service;

import com.classification.domain_system.dto.BusinessTermDto;
import com.classification.domain_system.entity.BusinessTerm;
import com.classification.domain_system.repository.BusinessTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessTermService {

    private final BusinessTermRepository businessTermRepository;

    @Transactional(readOnly = true)
    public List<BusinessTermDto.BusinessTermResponse> getAllTerms(UUID domainId) {
        List<BusinessTerm> list = (domainId != null)
                ? businessTermRepository.findByDomainId(domainId)
                : businessTermRepository.findAll();

        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BusinessTermDto.BusinessTermResponse createTerm(BusinessTermDto.BusinessTermCreateRequest request) {
        BusinessTerm term = BusinessTerm.builder()
                .termName(request.getTermName() != null ? request.getTermName() : new HashMap<>())
                .termCode(request.getTermCode())
                .domainId(request.getDomainId())
                .abbreviation(request.getAbbreviation())
                .synonyms(request.getSynonyms())
                .dataType(request.getDataType() != null ? request.getDataType() : "STRING")
                .sensitivityLevel(request.getSensitivityLevel() != null ? request.getSensitivityLevel() : "GENERAL")
                .description(request.getDescription())
                .build();

        BusinessTerm saved = businessTermRepository.save(term);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BusinessTermDto.TermRecommendation> recommendTerms(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String cleanKey = keyword.trim().toLowerCase();
        List<BusinessTerm> allTerms = businessTermRepository.findAll();
        List<BusinessTermDto.TermRecommendation> recs = new ArrayList<>();

        for (BusinessTerm term : allTerms) {
            double maxScore = 0.0;
            String reason = "";

            String koName = term.getTermName().getOrDefault("ko", "").toLowerCase();
            String enName = term.getTermName().getOrDefault("en", "").toLowerCase();
            String code = term.getTermCode() != null ? term.getTermCode().toLowerCase() : "";
            String abbr = term.getAbbreviation() != null ? term.getAbbreviation().toLowerCase() : "";
            String syns = term.getSynonyms() != null ? term.getSynonyms().toLowerCase() : "";

            if (koName.equals(cleanKey) || enName.equals(cleanKey) || code.equals(cleanKey)) {
                maxScore = 1.0;
                reason = "완전 일치 (Exact Match)";
            } else if (koName.contains(cleanKey) || enName.contains(cleanKey)) {
                maxScore = 0.85;
                reason = "용어명 부분 일치";
            } else if (abbr.equals(cleanKey) || code.contains(cleanKey)) {
                maxScore = 0.75;
                reason = "약어 또는 코드 매칭";
            } else if (syns.contains(cleanKey)) {
                maxScore = 0.65;
                reason = "동의어 매칭";
            }

            if (maxScore > 0.3) {
                recs.add(BusinessTermDto.TermRecommendation.builder()
                        .term(toResponse(term))
                        .similarityScore(maxScore)
                        .matchReason(reason)
                        .build());
            }
        }

        recs.sort(Comparator.comparingDouble(BusinessTermDto.TermRecommendation::getSimilarityScore).reversed());
        return recs;
    }

    private BusinessTermDto.BusinessTermResponse toResponse(BusinessTerm t) {
        return BusinessTermDto.BusinessTermResponse.builder()
                .id(t.getId())
                .termName(t.getTermName())
                .termCode(t.getTermCode())
                .domainId(t.getDomainId())
                .abbreviation(t.getAbbreviation())
                .synonyms(t.getSynonyms())
                .dataType(t.getDataType())
                .sensitivityLevel(t.getSensitivityLevel())
                .description(t.getDescription())
                .build();
    }
}
