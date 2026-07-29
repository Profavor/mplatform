package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordSecondaryNodeResponse;
import com.classification.domain_system.entity.ClassificationAxis;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordSecondaryNode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.RecordSecondaryNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiAxisRecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private RecordSecondaryNodeRepository secondaryNodeRepository;

    @InjectMocks
    private MultiAxisRecordService multiAxisRecordService;

    private UUID recordId;
    private UUID nodeId;
    private UUID axisId;
    private Record record;
    private ClassificationNode node;
    private ClassificationAxis axis;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        nodeId = UUID.randomUUID();
        axisId = UUID.randomUUID();

        record = new Record();
        record.setId(recordId);

        axis = new ClassificationAxis();
        axis.setId(axisId);
        axis.setAxisCode("EMPLOYMENT");
        axis.setName(Map.of("ko", "고용형태 축"));

        node = new ClassificationNode();
        node.setId(nodeId);
        node.setName(Map.of("ko", "정규직"));
        node.setPath("ROOT > 정규직");
        node.setAxis(axis);
    }

    @Test
    @DisplayName("레코드의 서브 노드 매핑을 설정한다")
    void setSecondaryNodes_Success() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(nodeRepository.findAllById(List.of(nodeId))).thenReturn(List.of(node));
        when(secondaryNodeRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<RecordSecondaryNodeResponse> responses = multiAxisRecordService.setSecondaryNodes(recordId, List.of(nodeId));

        verify(secondaryNodeRepository).deleteByRecordId(recordId);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNodeId()).isEqualTo(nodeId);
        assertThat(responses.get(0).getAxisCode()).isEqualTo("EMPLOYMENT");
    }

    @Test
    @DisplayName("존재하지 않는 레코드 ID 설정 시 ResourceNotFoundException 예외를 발생시킨다")
    void setSecondaryNodes_RecordNotFound_ThrowsException() {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> multiAxisRecordService.setSecondaryNodes(recordId, List.of(nodeId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("레코드의 모든 서브 노드 매핑 목록을 조회한다")
    void getSecondaryNodes_ReturnsList() {
        RecordSecondaryNode mapping = new RecordSecondaryNode();
        mapping.setId(UUID.randomUUID());
        mapping.setRecordId(recordId);
        mapping.setNode(node);
        mapping.setAxisId(axisId);

        when(recordRepository.existsById(recordId)).thenReturn(true);
        when(secondaryNodeRepository.findByRecordId(recordId)).thenReturn(List.of(mapping));

        List<RecordSecondaryNodeResponse> list = multiAxisRecordService.getSecondaryNodes(recordId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getRecordId()).isEqualTo(recordId);
        assertThat(list.get(0).getNodeName()).containsEntry("ko", "정규직");
    }
}
