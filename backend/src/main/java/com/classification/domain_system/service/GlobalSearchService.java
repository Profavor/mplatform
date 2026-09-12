package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.classification.domain_system.repository.DomainPermissionRepository domainPermissionRepository;

    @Transactional(readOnly = true)
    public Page<Record> searchGlobal(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> allowedDomainIds = resolveAllowedDomainIds();
        if (allowedDomainIds != null && allowedDomainIds.isEmpty()) {
            return Page.empty(pageable);
        }

        try {
            Criteria criteria = new Criteria("searchableData").contains(keyword)
                                .or(new Criteria("id").is(keyword));

            if (allowedDomainIds != null) {
                criteria = criteria.and(new Criteria("domainId").in(allowedDomainIds));
            }
                                
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setPageable(pageable);

            SearchHits<RecordDocument> searchHits = elasticsearchOperations.search(query, RecordDocument.class);
            
            if (searchHits.getTotalHits() > 0) {
                List<Record> results = searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
                        
                return new PageImpl<>(results, pageable, searchHits.getTotalHits());
            } else {
                return searchWithJpaFallback(keyword, pageable, allowedDomainIds);
            }

        } catch (Throwable e) {
            log.warn("OpenSearch search failed. Falling back to JPA full-text search. Error: {}", e.getMessage());
            return searchWithJpaFallback(keyword, pageable, allowedDomainIds);
        }
    }

    private Page<Record> searchWithJpaFallback(String keyword, Pageable pageable, List<String> allowedDomainIds) {
        Page<Record> page = recordRepository.searchGlobalRecords(keyword.trim(), pageable);
        // Force initialize LAZY node and domain for Jackson serialization
        for (Record r : page.getContent()) {
            if (r.getNode() != null) {
                r.getNode().getName();
                if (r.getNode().getDomain() != null) {
                    r.getNode().getDomain().getName();
                }
            }
        }
        if (allowedDomainIds != null) {
            Set<UUID> allowedUuids = allowedDomainIds.stream().map(UUID::fromString).collect(Collectors.toSet());
            List<Record> filtered = page.getContent().stream()
                    .filter(r -> r.getNode() != null && r.getNode().getDomain() != null && allowedUuids.contains(r.getNode().getDomain().getId()))
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }
        return page;
    }

    private List<String> resolveAllowedDomainIds() {
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isAdmin) {
            return null;
        }
        if (domainPermissionRepository != null) {
            List<com.classification.domain_system.entity.DomainPermission> perms = 
                    domainPermissionRepository.findByUserId(auth.getName());
            if (perms != null && !perms.isEmpty()) {
                return perms.stream()
                        .map(p -> p.getDomain().getId().toString())
                        .collect(Collectors.toList());
            }
        }
        return null;
    }
    
    private Record mapToResponse(RecordDocument doc) {
        Record resp = new Record();
        if (doc.getId() != null) {
            resp.setId(UUID.fromString(doc.getId()));
        }
        resp.setStatus(doc.getStatus() != null ? doc.getStatus() : "ACTIVE");
        resp.setCreatedAt(doc.getCreatedAt());
        resp.setUpdatedAt(doc.getUpdatedAt());
        
        if (doc.getNodeId() != null) {
            try {
                UUID nodeId = UUID.fromString(doc.getNodeId());
                nodeRepository.findById(nodeId).ifPresent(resp::setNode);
            } catch (Exception ignored) {}
        }
        
        try {
            if (doc.getData() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                resp.setData(mapper.writeValueAsString(doc.getData()));
            }
        } catch (Exception e) {
            log.warn("Failed to parse data for doc {}", doc.getId(), e);
        }
        return resp;
    }
}
