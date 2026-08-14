package com.classification.domain_system.integration;

import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IntegrationLogServiceTest {

    @Mock
    private IntegrationLogRepository logRepository;

    @Mock
    private IntegrationChannelRepository channelRepository;

    @Mock
    private IntegrationRetryService retryService;

    @InjectMocks
    private IntegrationLogService integrationLogService;

    private UUID channelId;
    private UUID recordId;
    private IntegrationLog logEntity;

    @BeforeEach
    void setUp() {
        channelId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        logEntity = new IntegrationLog();
        logEntity.setId(UUID.randomUUID());
        logEntity.setChannelId(channelId);
        logEntity.setRecordId(recordId);
        logEntity.setStatus("SUCCESS");
    }

    @Test
    @DisplayName("채널 ID 조건이 있을 때 로그 페이징 조회")
    void getLogs_withChannelId() {
        PageRequest pr = PageRequest.of(0, 10);
        given(logRepository.findByChannelId(channelId, pr)).willReturn(new PageImpl<>(List.of(logEntity)));

        Page<IntegrationLog> result = integrationLogService.getLogs(channelId, pr);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("SUCCESS");
        verify(logRepository).findByChannelId(channelId, pr);
    }

    @Test
    @DisplayName("채널 ID 조건이 없을 때 전체 로그 페이징 조회")
    void getLogs_withoutChannelId() {
        PageRequest pr = PageRequest.of(0, 10);
        given(logRepository.findAll(pr)).willReturn(new PageImpl<>(List.of(logEntity)));

        Page<IntegrationLog> result = integrationLogService.getLogs(null, pr);

        assertThat(result.getContent()).hasSize(1);
        verify(logRepository).findAll(pr);
    }

    @Test
    @DisplayName("레코드 ID로 로그 목록 조회")
    void getLogsByRecordId() {
        given(logRepository.findByRecordIdOrderByCreatedAtDesc(recordId)).willReturn(List.of(logEntity));

        List<IntegrationLog> result = integrationLogService.getLogsByRecordId(recordId);

        assertThat(result).hasSize(1);
        verify(logRepository).findByRecordIdOrderByCreatedAtDesc(recordId);
    }
}
