package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordSearchDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RecordSearchService {

    private final RecordRepository recordRepository;

    public RecordSearchService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public RecordSearchDto.SearchResponse searchRecords(RecordSearchDto.SearchRequest request) {
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
