package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class SemanticOntologyDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OntologyNode {
        private String id;
        private String label;
        private String domainCode;
        private String type; // DOMAIN, ENTITY, FIELD, CONCEPT
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OntologyEdge {
        private String sourceId;
        private String targetId;
        private String relationType; // IS_A, PART_OF, PURCHASED_BY, MANAGED_BY, DEPENDS_ON
        private double weight;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OntologyGraphResponse {
        private List<OntologyNode> nodes;
        private List<OntologyEdge> edges;
        private String summary;
    }
}
