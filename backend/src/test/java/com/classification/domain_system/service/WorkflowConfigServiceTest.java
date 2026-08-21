package com.classification.domain_system.service;

import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConfigServiceTest {

    @Mock
    private WorkflowConfigRepository repository;

    @InjectMocks
    private WorkflowConfigService workflowConfigService;

    @Test
    @DisplayName("saveForDomain - 유효하지 않은 stepOrder(1부터 시작하지 않음)시 BusinessException 발생")
    void saveForDomain_InvalidStepOrder_ThrowsException() {
        UUID domainId = UUID.randomUUID();
        WorkflowConfig config = new WorkflowConfig();
        config.setStepsConfig("{\"steps\":[{\"stepOrder\":2}]}");

        assertThrows(BusinessException.class, () -> 
            workflowConfigService.saveForDomain(domainId, List.of(config))
        );
    }

    @Test
    @DisplayName("saveForDomain - 유효한 설정 저장 성공")
    void saveForDomain_Success() {
        UUID domainId = UUID.randomUUID();
        WorkflowConfig config = new WorkflowConfig();
        config.setActionType("CREATE");
        config.setStepsConfig("{\"steps\":[{\"stepOrder\":1},{\"stepOrder\":2}]}");

        when(repository.findByDomainId(domainId)).thenReturn(List.of());
        when(repository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<WorkflowConfig> result = workflowConfigService.saveForDomain(domainId, List.of(config));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(domainId, result.get(0).getDomainId());
        assertNull(result.get(0).getNodeId());
        verify(repository).saveAll(any());
    }

    @Test
    @DisplayName("saveForNode - 노드 워크플로우 설정 저장 성공")
    void saveForNode_Success() {
        UUID nodeId = UUID.randomUUID();
        WorkflowConfig config = new WorkflowConfig();
        config.setActionType("CREATE");
        config.setStepsConfig("{\"steps\":[{\"stepOrder\":1}]}");

        when(repository.findByNodeId(nodeId)).thenReturn(List.of());
        when(repository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<WorkflowConfig> result = workflowConfigService.saveForNode(nodeId, List.of(config));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(nodeId, result.get(0).getNodeId());
        verify(repository).saveAll(any());
    }

    @Test
    @DisplayName("getByDomain - 도메인 설정 조회 (nodeId가 null인 것만 반환)")
    void getByDomain_Success() {
        UUID domainId = UUID.randomUUID();
        WorkflowConfig c1 = new WorkflowConfig();
        c1.setDomainId(domainId);
        c1.setNodeId(null);

        WorkflowConfig c2 = new WorkflowConfig();
        c2.setDomainId(domainId);
        c2.setNodeId(UUID.randomUUID());

        when(repository.findByDomainId(domainId)).thenReturn(List.of(c1, c2));

        List<WorkflowConfig> result = workflowConfigService.getByDomain(domainId);

        assertEquals(1, result.size());
        assertNull(result.get(0).getNodeId());
    }

    @Test
    @DisplayName("deleteWorkflowConfig - 워크플로우 설정 삭제")
    void deleteWorkflowConfig_Success() {
        UUID id = UUID.randomUUID();

        workflowConfigService.deleteWorkflowConfig(id);

        verify(repository).deleteById(id);
    }
}
