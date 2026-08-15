package com.classification.domain_system.service;

import com.classification.domain_system.dto.SemanticOntologyDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.MasterRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SemanticOntologyServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private MasterRelationRepository masterRelationRepository;

    @InjectMocks
    private SemanticOntologyService ontologyService;

    private UUID domainId1;
    private UUID domainId2;

    @BeforeEach
    void setUp() {
        domainId1 = UUID.randomUUID();
        domainId2 = UUID.randomUUID();

        Domain d1 = new Domain();
        d1.setId(domainId1);
        Map<String, String> name1 = new HashMap<>();
        name1.put("ko", "고객 마스터");
        d1.setName(name1);

        Domain d2 = new Domain();
        d2.setId(domainId2);
        Map<String, String> name2 = new HashMap<>();
        name2.put("ko", "주문/계약");
        d2.setName(name2);

        MasterRelation rel = new MasterRelation();
        rel.setId(UUID.randomUUID());
        rel.setSourceDomainId(domainId2);
        rel.setTargetDomainId(domainId1);
        rel.setRelationType("PURCHASED_BY");

        given(domainRepository.findAll()).willReturn(List.of(d1, d2));
        given(masterRelationRepository.findAll()).willReturn(List.of(rel));
    }

    @Test
    @DisplayName("getOntologyGraph: DB 실제 도메인 및 관계 엔티티 기반 동적 지식 그래프 조회")
    void testGetOntologyGraph() {
        SemanticOntologyDto.OntologyGraphResponse res = ontologyService.getOntologyGraph();

        assertThat(res).isNotNull();
        assertThat(res.getNodes()).hasSize(2);
        assertThat(res.getNodes().get(0).getLabel()).isEqualTo("고객 마스터");
        assertThat(res.getEdges()).hasSize(1);
        assertThat(res.getEdges().get(0).getRelationType()).isEqualTo("PURCHASED_BY");
    }

    @Test
    @DisplayName("searchOntology: 특정 도메인 키워드 기반 온톨로지 동적 서브그래프 검색")
    void testSearchOntology() {
        SemanticOntologyDto.OntologyGraphResponse res = ontologyService.searchOntology("고객");

        assertThat(res).isNotNull();
        assertThat(res.getNodes()).hasSize(1);
        assertThat(res.getNodes().get(0).getLabel()).isEqualTo("고객 마스터");
        assertThat(res.getEdges()).isNotEmpty();
    }
}
