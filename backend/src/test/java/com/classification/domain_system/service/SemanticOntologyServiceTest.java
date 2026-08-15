package com.classification.domain_system.service;

import com.classification.domain_system.dto.SemanticOntologyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SemanticOntologyServiceTest {

    private SemanticOntologyService ontologyService;

    @BeforeEach
    void setUp() {
        ontologyService = new SemanticOntologyService();
    }

    @Test
    @DisplayName("getOntologyGraph: 전사 시맨틱 온톨로지 지식 그래프 조회")
    void testGetOntologyGraph() {
        SemanticOntologyDto.OntologyGraphResponse res = ontologyService.getOntologyGraph();

        assertThat(res).isNotNull();
        assertThat(res.getNodes()).hasSize(5);
        assertThat(res.getEdges()).hasSize(4);
        assertThat(res.getEdges().get(0).getRelationType()).isEqualTo("PURCHASED_BY");
    }

    @Test
    @DisplayName("searchOntology: 특정 도메인 키워드 기반 온톨로지 서브그래프 검색")
    void testSearchOntology() {
        SemanticOntologyDto.OntologyGraphResponse res = ontologyService.searchOntology("고객");

        assertThat(res).isNotNull();
        assertThat(res.getNodes()).hasSize(1);
        assertThat(res.getNodes().get(0).getDomainCode()).isEqualTo("DOM-CUST");
        assertThat(res.getEdges()).isNotEmpty();
    }
}
