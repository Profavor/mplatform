package com.classification.domain_system.service;

import com.classification.domain_system.dto.HashChainDto;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashChainAuditServiceTest {

    @Mock private RecordHistoryRepository recordHistoryRepository;

    @InjectMocks
    private HashChainAuditService hashChainAuditService;

    private UUID recordId;
    private RecordHistory history1;
    private RecordHistory history2;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();

        history1 = new RecordHistory();
        history1.setId(UUID.randomUUID());
        history1.setRecordId(recordId);
        history1.setChangeType("CREATE");
        history1.setChangedBy("ADMIN");
        history1.setVersion(1);
        history1.setChangedAt(LocalDateTime.now().minusHours(2));

        history2 = new RecordHistory();
        history2.setId(UUID.randomUUID());
        history2.setRecordId(recordId);
        history2.setChangeType("UPDATE");
        history2.setChangedBy("USER1");
        history2.setVersion(2);
        history2.setChangedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("verifyRecordLedger: 변경 이력 해시체인 블록 연속성 및 무결성 검증")
    void testVerifyRecordLedger() {
        when(recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId)).thenReturn(List.of(history1, history2));

        HashChainDto.LedgerVerificationResponse res = hashChainAuditService.verifyRecordLedger(recordId);

        assertThat(res).isNotNull();
        assertThat(res.getTotalBlocks()).isEqualTo(2);
        assertThat(res.getValidBlocks()).isEqualTo(2);
        assertThat(res.isChainIntact()).isTrue();

        List<HashChainDto.LedgerBlockItem> blocks = res.getBlocks();
        assertThat(blocks.get(0).getBlockHash()).isNotEmpty();
        assertThat(blocks.get(1).getPrevHash()).isEqualTo(blocks.get(0).getBlockHash());
    }
}
