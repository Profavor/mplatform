package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class VolumeAnomalyDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VolumeDataPoint {
        private String timeBucket;
        private int createCount;
        private int updateCount;
        private int deleteCount;
        private int apiCallCount;
        private double zScore;
        private boolean isSpike;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VolumeRadarResponse {
        private String status; // NORMAL, SPIKE_DETECTED
        private int currentThroughput;
        private int baselineThroughput;
        private List<VolumeDataPoint> history;
        private String recommendation;
    }
}
