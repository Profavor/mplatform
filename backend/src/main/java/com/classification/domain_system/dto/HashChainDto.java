package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class HashChainDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LedgerBlockItem {
        private long blockIndex;
        private UUID recordId;
        private String recordCode;
        private String actionType;
        private String actor;
        private String prevHash;
        private String blockHash;
        private LocalDateTime timestamp;
        private boolean valid;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LedgerVerificationResponse {
        private int totalBlocks;
        private int validBlocks;
        private int corruptedBlocks;
        private boolean isChainIntact;
        private List<LedgerBlockItem> blocks;
        private String summary;
    }
}
