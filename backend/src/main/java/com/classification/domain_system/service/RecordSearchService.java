package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordSearchDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import com.classification.domain_system.repository.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@Transactional(readOnly = true)
public class RecordSearchService {

    private final RecordRepository recordRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public RecordSearchService(RecordRepository recordRepository, ElasticsearchOperations elasticsearchOperations) {
        this.recordRepository = recordRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public RecordSearchDto.SearchResponse searchRecords(RecordSearchDto.SearchRequest request) {
        try {
            return searchWithOpenSearch(request);
        } catch (Exception e) {
            // OpenSearch 장애 시 JPA로 Fallback (JSONB 검색)
            return searchWithJpaFallback(request);
        }
    }

    private RecordSearchDto.SearchResponse searchWithOpenSearch(RecordSearchDto.SearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        String keyword = request.getKeyword() != null ? request.getKeyword().trim() : "";

        String json;
        if (keyword.isEmpty()) {
            json = String.format("{\"term\": {\"domainId\": \"%s\"}}", request.getDomainId().toString());
        } else {
            json = String.format("{\"bool\": {\"must\": [{\"term\": {\"domainId\": \"%s\"}}, {\"match\": {\"searchableData\": \"%s\"}}]}}", request.getDomainId().toString(), keyword.replace("\"", "\\\""));
        }

        StringQuery stringQuery = new StringQuery(json);
        stringQuery.setPageable(pageRequest);

        if (!keyword.isEmpty()) {
            Highlight highlight = new Highlight(List.of(new HighlightField("searchableData")));
            stringQuery.setHighlightQuery(new HighlightQuery(highlight, null));
        }

        SearchHits<RecordDocument> searchHits = elasticsearchOperations.search(stringQuery, RecordDocument.class);

        List<RecordSearchDto.SearchItem> items = new ArrayList<>();
        for (SearchHit<RecordDocument> hit : searchHits) {
            RecordDocument doc = hit.getContent();
            RecordSearchDto.SearchItem item = new RecordSearchDto.SearchItem();
            item.setRecordId(UUID.fromString(doc.getId()));
            item.setRecordCode("REC-" + doc.getId().substring(0, 8));
            try {
                item.setRawData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(doc.getData()));
            } catch (Exception e) {
                item.setRawData("{}");
            }

            if (!keyword.isEmpty() && hit.getHighlightFields().containsKey("searchableData")) {
                List<String> highlights = hit.getHighlightField("searchableData");
                for (String h : highlights) {
                    item.getHighlights().computeIfAbsent("data", k -> new ArrayList<>()).add(h);
                }
            }
            items.add(item);
        }

        return new RecordSearchDto.SearchResponse(items, searchHits.getTotalHits(), request.getPage(), request.getSize());
    }

    private RecordSearchDto.SearchResponse searchWithJpaFallback(RecordSearchDto.SearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        Page<Record> recordPage = recordRepository.findByDomainId(request.getDomainId(), pageRequest);

        List<RecordSearchDto.SearchItem> items = new ArrayList<>();
        String keyword = request.getKeyword() != null ? request.getKeyword().trim() : "";

        for (Record record : recordPage.getContent()) {
            RecordSearchDto.SearchItem item = new RecordSearchDto.SearchItem();
            item.setRecordId(record.getId());
            item.setRecordCode("REC-" + record.getId().toString().substring(0, 8));
            item.setRawData(record.getData());

            if (!keyword.isEmpty()) {
                String searchableText = record.getSearchableData() != null ? record.getSearchableData() : record.getData();
                if (searchableText != null && searchableText.toLowerCase().contains(keyword.toLowerCase())) {
                    String highlighted = searchableText.replaceAll("(?i)" + java.util.regex.Pattern.quote(keyword), "<mark>$0</mark>");
                    item.getHighlights().computeIfAbsent("data", k -> new ArrayList<>()).add(highlighted);
                }
            }
            items.add(item);
        }

        return new RecordSearchDto.SearchResponse(items, recordPage.getTotalElements(), request.getPage(), request.getSize());
    }
}
