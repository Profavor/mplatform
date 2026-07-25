package com.classification.domain_system.service;

import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.SchemaHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaHistoryServiceTest {

    @Mock
    private SchemaHistoryRepository schemaHistoryRepository;

    @InjectMocks
    private SchemaHistoryService schemaHistoryService;

    private UUID domainId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        targetId = UUID.randomUUID();
    }

    @Test
    @DisplayName("recordChange - 스키마 변경 사항을 Before/After JSON으로 정상 직렬화 및 저장")
    void recordChange_Success() {
        when(schemaHistoryRepository.save(any(SchemaHistory.class))).thenAnswer(i -> i.getArgument(0));

        SchemaHistory result = schemaHistoryService.recordChange(
                domainId, "FIELD_DEFINITION", targetId, "CREATE", null, "{\"name\":\"전화번호\"}", "admin"
        );

        assertThat(result.getDomainId()).isEqualTo(domainId);
        assertThat(result.getBeforeData()).isNull();
        assertThat(result.getAfterData()).contains("전화번호");
        assertThat(result.getChangedBy()).isEqualTo("admin");
        verify(schemaHistoryRepository, times(1)).save(any(SchemaHistory.class));
    }

    @Test
    @DisplayName("getDomainHistory - 특정 도메인의 스키마 변경 이력 목록 조회")
    void getDomainHistory_Success() {
        SchemaHistory history = new SchemaHistory();
        history.setDomainId(domainId);
        history.setAction("UPDATE");

        when(schemaHistoryRepository.findByDomainIdOrderByChangedAtDesc(domainId)).thenReturn(List.of(history));

        List<SchemaHistory> list = schemaHistoryService.getDomainHistory(domainId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getAction()).isEqualTo("UPDATE");
    }

}
