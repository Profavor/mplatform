package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordBatchUpsertRequest {
    /**
     * Business key used to identify unique records. Defaults to "PRODUCT_ID".
     */
    private String businessKey;

    /**
     * If true, record is saved directly with ACTIVE status. If false, PENDING_APPROVAL.
     * Defaults to true for automated sync.
     */
    @Builder.Default
    private Boolean autoApprove = true;

    /**
     * Name of the calling system (e.g. "cartbom").
     */
    @Builder.Default
    private String sourceSystem = "cartbom";

    /**
     * List of record items to upsert. Can contain wrapped data ("data": {...}) or flat properties.
     */
    private List<Item> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        /**
         * Can be an Object (Map<String, Object>), a JSON String, or null if flat properties are sent.
         */
        private Object data;
        private String requesterId;
        private String comment;
        private UUID workflowConfigId;

        @Builder.Default
        private Map<String, Object> additionalProperties = new HashMap<>();

        @JsonAnySetter
        public void handleUnknownProperty(String key, Object value) {
            if (this.additionalProperties == null) {
                this.additionalProperties = new HashMap<>();
            }
            this.additionalProperties.put(key, value);
        }

        public Object getEffectiveData() {
            if (data != null) {
                return data;
            }
            if (additionalProperties != null && !additionalProperties.isEmpty()) {
                return additionalProperties;
            }
            return null;
        }
    }
}
