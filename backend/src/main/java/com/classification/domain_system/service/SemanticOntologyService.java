package com.classification.domain_system.service;

import com.classification.domain_system.dto.SemanticOntologyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticOntologyService {

    public SemanticOntologyDto.OntologyGraphResponse getOntologyGraph() {
        List<SemanticOntologyDto.OntologyNode> nodes = new ArrayList<>();
        nodes.add(SemanticOntologyDto.OntologyNode.builder().id("NODE-CUST").label("고객 마스터").domainCode("DOM-CUST").type("DOMAIN").build());
        nodes.add(SemanticOntologyDto.OntologyNode.builder().id("NODE-PROD").label("제품/자재 마스터").domainCode("DOM-PROD").type("DOMAIN").build());
        nodes.add(SemanticOntologyDto.OntologyNode.builder().id("NODE-ORD").label("주문/계약").domainCode("DOM-ORD").type("DOMAIN").build());
        nodes.add(SemanticOntologyDto.OntologyNode.builder().id("NODE-ORG").label("조직/담당자").domainCode("DOM-ORG").type("DOMAIN").build());
        nodes.add(SemanticOntologyDto.OntologyNode.builder().id("NODE-VEND").label("협력업체").domainCode("DOM-VEND").type("DOMAIN").build());

        List<SemanticOntologyDto.OntologyEdge> edges = new ArrayList<>();
        edges.add(SemanticOntologyDto.OntologyEdge.builder().sourceId("NODE-ORD").targetId("NODE-CUST").relationType("PURCHASED_BY").weight(1.0).build());
        edges.add(SemanticOntologyDto.OntologyEdge.builder().sourceId("NODE-ORD").targetId("NODE-PROD").relationType("CONTAINS").weight(0.9).build());
        edges.add(SemanticOntologyDto.OntologyEdge.builder().sourceId("NODE-PROD").targetId("NODE-VEND").relationType("SUPPLIED_BY").weight(0.85).build());
        edges.add(SemanticOntologyDto.OntologyEdge.builder().sourceId("NODE-CUST").targetId("NODE-ORG").relationType("MANAGED_BY").weight(0.95).build());

        return SemanticOntologyDto.OntologyGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .summary("전사 5개 핵심 도메인 간의 4대 시맨틱 온톨로지 관계(구매/포함/공급/관리)가 완벽하게 연결되어 있습니다.")
                .build();
    }

    public SemanticOntologyDto.OntologyGraphResponse searchOntology(String keyword) {
        SemanticOntologyDto.OntologyGraphResponse fullGraph = getOntologyGraph();
        if (keyword == null || keyword.isBlank()) {
            return fullGraph;
        }

        List<SemanticOntologyDto.OntologyNode> filteredNodes = fullGraph.getNodes().stream()
                .filter(n -> n.getLabel().contains(keyword) || n.getDomainCode().contains(keyword))
                .toList();

        List<String> nodeIds = filteredNodes.stream().map(SemanticOntologyDto.OntologyNode::getId).toList();
        List<SemanticOntologyDto.OntologyEdge> filteredEdges = fullGraph.getEdges().stream()
                .filter(e -> nodeIds.contains(e.getSourceId()) || nodeIds.contains(e.getTargetId()))
                .toList();

        return SemanticOntologyDto.OntologyGraphResponse.builder()
                .nodes(filteredNodes)
                .edges(filteredEdges)
                .summary(String.format("'%s' 키워드에 연관된 %d개 시맨틱 노드 및 관계가 탐색되었습니다.", keyword, filteredNodes.size()))
                .build();
    }
}
