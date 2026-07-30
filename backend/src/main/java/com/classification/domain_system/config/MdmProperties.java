package com.classification.domain_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mdm")
@Getter
@Setter
public class MdmProperties {

    private Matching matching = new Matching();
    private Dq dq = new Dq();
    private Merge merge = new Merge();

    @Getter @Setter
    public static class Matching {
        private int fuzzyMaxCandidates = 500;
        private int fuzzyMaxPages = 10;
        private double feedbackPrecisionThreshold = 0.95;
        private double feedbackScoreIncrement = 0.02;
    }

    @Getter @Setter
    public static class Dq {
        private String systemUserId = "SYSTEM";
        private int trendWindowDays = 30;
    }

    @Getter @Setter
    public static class Merge {
        private int defaultSourcePriority = 999;
    }
}
