package com.classification.domain_system.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordSearchDto {

    public static class SearchRequest {
        private UUID domainId;
        private String keyword;
        private boolean fuzzy = false;
        private Map<String, Object> fieldFilters = new HashMap<>();
        private int page = 0;
        private int size = 20;

        public UUID getDomainId() { return domainId; }
        public void setDomainId(UUID domainId) { this.domainId = domainId; }

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }

        public boolean isFuzzy() { return fuzzy; }
        public void setFuzzy(boolean fuzzy) { this.fuzzy = fuzzy; }

        public Map<String, Object> getFieldFilters() { return fieldFilters; }
        public void setFieldFilters(Map<String, Object> fieldFilters) { this.fieldFilters = fieldFilters; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    public static class SearchItem {
        private UUID recordId;
        private String recordCode;
        private String rawData;
        private Map<String, List<String>> highlights = new HashMap<>();

        public UUID getRecordId() { return recordId; }
        public void setRecordId(UUID recordId) { this.recordId = recordId; }

        public String getRecordCode() { return recordCode; }
        public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

        public String getRawData() { return rawData; }
        public void setRawData(String rawData) { this.rawData = rawData; }

        public Map<String, List<String>> getHighlights() { return highlights; }
        public void setHighlights(Map<String, List<String>> highlights) { this.highlights = highlights; }
    }

    public static class SearchResponse {
        private List<SearchItem> items = new ArrayList<>();
        private long totalElements;
        private int totalPages;
        private int currentPage;

        public SearchResponse() {}

        public SearchResponse(List<SearchItem> items, long totalElements, int currentPage, int size) {
            this.items = items;
            this.totalElements = totalElements;
            this.currentPage = currentPage;
            this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        }

        public List<SearchItem> getItems() { return items; }
        public void setItems(List<SearchItem> items) { this.items = items; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    }
}
