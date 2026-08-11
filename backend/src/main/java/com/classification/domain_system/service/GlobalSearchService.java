package com.classification.domain_system.service;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final RecordRepository recordRepository;

    public Page<Record> searchGlobal(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        try {
            Criteria criteria = new Criteria("searchableData").contains(keyword)
                                .or(new Criteria("id").is(keyword));
                                
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setPageable(pageable);

            SearchHits<RecordDocument> searchHits = elasticsearchOperations.search(query, RecordDocument.class);
            
            List<Record> results = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
                    
            return new PageImpl<>(results, pageable, searchHits.getTotalHits());

        } catch (Exception e) {
            log.warn("OpenSearch search failed. Falling back to JPA full-text search. Error: {}", e.getMessage());
            return searchWithJpaFallback(keyword, pageable);
        }
    }

    private Page<Record> searchWithJpaFallback(String keyword, Pageable pageable) {
        return recordRepository.findBySearchableDataContainingIgnoreCase(keyword, pageable);
    }
    
    private Record mapToResponse(RecordDocument doc) {
        Record resp = new Record();
        resp.setId(java.util.UUID.fromString(doc.getId()));
        
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
