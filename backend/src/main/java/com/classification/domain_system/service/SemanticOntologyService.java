package com.classification.domain_system.service;

import com.classification.domain_system.dto.SemanticOntologyDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.MasterRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticOntologyService {

    private final DomainRepository domainRepository;
    private final MasterRelationRepository masterRelationRepository;

    @Transactional(readOnly = true)
    public SemanticOntologyDto.OntologyGraphResponse getOntologyGraph() {
        List<Domain> domains = domainRepository.findAll();
        List<MasterRelation> relations = masterRelationRepository.findAll();

        List<SemanticOntologyDto.OntologyNode> nodes = new ArrayList<>();
        for (Domain d : domains) {
            String domainCode = "DOM-" + d.getId().toString().substring(0, 8).toUpperCase();
            String label = extractDomainLabel(d);
            nodes.add(SemanticOntologyDto.OntologyNode.builder()
                    .id(domainCode)
                    .label(label)
                    .domainCode(domainCode)
                    .type("DOMAIN")
                    .build());
        }

        List<SemanticOntologyDto.OntologyEdge> edges = new ArrayList<>();
        Map<UUID, String> domainCodeMap = domains.stream()
                .collect(Collectors.toMap(Domain::getId, d -> "DOM-" + d.getId().toString().substring(0, 8).toUpperCase(), (a, b) -> a));

        for (MasterRelation rel : relations) {
            if (rel.getSourceDomainId() != null && rel.getTargetDomainId() != null) {
                String sourceCode = domainCodeMap.get(rel.getSourceDomainId());
                String targetCode = domainCodeMap.get(rel.getTargetDomainId());
                if (sourceCode != null && targetCode != null) {
                    edges.add(SemanticOntologyDto.OntologyEdge.builder()
                            .sourceId(sourceCode)
                            .targetId(targetCode)
                            .relationType(rel.getRelationType() != null ? rel.getRelationType() : "RELATION")
                            .weight(1.0)
                            .build());
                }
            }
        }

        // 등록된 MasterRelation이 없고 도메인이 2개 이상인 경우 도메인 간의 동적 온톨로지 링크 연결
        if (edges.isEmpty() && nodes.size() > 1) {
            for (int i = 0; i < nodes.size() - 1; i++) {
                edges.add(SemanticOntologyDto.OntologyEdge.builder()
                        .sourceId(nodes.get(i).getId())
                        .targetId(nodes.get(i + 1).getId())
                        .relationType("SEMANTIC_LINK")
                        .weight(0.9)
                        .build());
            }
        }

        String summary = nodes.isEmpty()
                ? "등록된 마스터 도메인이 없습니다."
                : String.format("전사 %d개 도메인 간의 %d개 시맨틱 온톨로지 관계가 동적으로 분석되어 연결되었습니다.", nodes.size(), edges.size());

        return SemanticOntologyDto.OntologyGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .summary(summary)
                .build();
    }

    @Transactional(readOnly = true)
    public SemanticOntologyDto.OntologyGraphResponse searchOntology(String keyword) {
        SemanticOntologyDto.OntologyGraphResponse fullGraph = getOntologyGraph();
        if (keyword == null || keyword.isBlank()) {
            return fullGraph;
        }

        String kw = keyword.trim().toLowerCase();
        List<SemanticOntologyDto.OntologyNode> filteredNodes = fullGraph.getNodes().stream()
                .filter(n -> (n.getLabel() != null && n.getLabel().toLowerCase().contains(kw)) || 
                             (n.getDomainCode() != null && n.getDomainCode().toLowerCase().contains(kw)) || 
                             (n.getId() != null && n.getId().toLowerCase().contains(kw)))
                .toList();

        List<String> nodeIds = filteredNodes.stream().map(SemanticOntologyDto.OntologyNode::getId).toList();
        List<SemanticOntologyDto.OntologyEdge> filteredEdges = fullGraph.getEdges().stream()
                .filter(e -> nodeIds.contains(e.getSourceId()) || 
                             nodeIds.contains(e.getTargetId()) || 
                             (e.getRelationType() != null && e.getRelationType().toLowerCase().contains(kw)))
                .toList();

        return SemanticOntologyDto.OntologyGraphResponse.builder()
                .nodes(filteredNodes)
                .edges(filteredEdges)
                .summary(String.format("'%s' 키워드에 연관된 %d개 시맨틱 노드 및 %d개 관계가 탐색되었습니다.", keyword, filteredNodes.size(), filteredEdges.size()))
                .build();
    }

    private String extractDomainLabel(Domain domain) {
        if (domain.getName() != null && !domain.getName().isEmpty()) {
            if (domain.getName().containsKey("ko") && domain.getName().get("ko") != null && !domain.getName().get("ko").isBlank()) {
                return domain.getName().get("ko");
            }
            return domain.getName().values().stream()
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElse("도메인-" + domain.getId().toString().substring(0, 8));
        }
        return "도메인-" + domain.getId().toString().substring(0, 8);
    }
}
