package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataLineageServiceTest {

    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private IntegrationLogRepository integrationLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private RecordService recordService;
    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private IntegrationChannelRepository channelRepository;

    @InjectMocks
    private RecordLineageService lineageService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "인사 도메인"));
        domain.setCreatedAt(LocalDateTime.now().minusMonths(1));
    }

    @Test
    @DisplayName("getDomainLineage: 도메인-노드-연계채널 간 노드 및 엣지 계보 그래프 정상 생성")
    void testGetDomainLineage() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setName(Map.of("ko", "정규직"));
        node.setCreatedAt(LocalDateTime.now().minusWeeks(2));
        when(nodeRepository.findByDomain_Id(domainId)).thenReturn(List.of(node));

        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(UUID.randomUUID());
        channel.setName("ERP 인사연계");
        channel.setCreatedAt(LocalDateTime.now().minusWeeks(3));
        when(channelRepository.findAll()).thenReturn(List.of(channel));

        RecordLineageDto.DomainLineageResponse response = lineageService.getDomainLineage(domainId);

        assertThat(response).isNotNull();
        assertThat(response.getDomainName()).isEqualTo("인사 도메인");
        // 1 Domain node + 1 Classification node + 1 Channel node = 3 nodes
        assertThat(response.getNodes()).hasSize(3);
        // 1 CONTAINS edge + 1 SYNC_PIPELINE edge = 2 edges
        assertThat(response.getEdges()).hasSize(2);

        assertThat(response.getEdges().get(0).getRelationship()).isEqualTo("CONTAINS");
        assertThat(response.getEdges().get(1).getRelationship()).isEqualTo("SYNC_PIPELINE");
    }

    @Test
    @DisplayName("getDomainLineage: 도메인 미존재 시 ResourceNotFoundException 발생")
    void testDomainNotFound() {
        when(domainRepository.findById(domainId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lineageService.getDomainLineage(domainId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Domain not found");
    }
}
