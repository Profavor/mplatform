package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.SchemaHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaHistoryServiceAsOfTest {

    @Mock
    private SchemaHistoryRepository schemaHistoryRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @InjectMocks
    private SchemaHistoryService schemaHistoryService;

    @BeforeEach
    void setUp() {
        // fieldDefinitionService는 @Autowired(required=false) 필드 주입이므로
        // @InjectMocks 생성자 주입으로는 설정되지 않아 수동으로 주입
        ReflectionTestUtils.setField(schemaHistoryService, "fieldDefinitionService", fieldDefinitionService);
    }

    @Test
    @DisplayName("과거 특정 시점(asOf)의 유효 필드 상태 조회가 성공한다")
    void getEffectiveFieldsAsOf_Success() {
        UUID nodeId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(domainId);

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        FieldDefinition currentField = new FieldDefinition();
        currentField.setId(UUID.randomUUID());
        currentField.setKey("field1");
        currentField.setName(Map.of("ko", "Field Current"));

        when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(currentField));

        SchemaHistory history = new SchemaHistory();
        history.setDomainId(domainId);
        history.setTargetType("FIELD");
        history.setTargetId(currentField.getId());
        history.setAction("UPDATE");
        history.setBeforeData("{\"id\":\"" + currentField.getId() + "\",\"key\":\"field1\",\"name\":{\"ko\":\"Field Past\"}}");
        history.setAfterData("{\"id\":\"" + currentField.getId() + "\",\"key\":\"field1\",\"name\":{\"ko\":\"Field Current\"}}");
        history.setChangedAt(LocalDateTime.now().minusDays(1));

        when(schemaHistoryRepository.findByDomainIdAndChangedAtAfterOrderByChangedAtDesc(eq(domainId), any()))
                .thenReturn(List.of(history));

        LocalDateTime targetAsOf = LocalDateTime.now().minusDays(2);
        List<FieldDefinition> result = schemaHistoryService.getEffectiveFieldsAsOf(nodeId, targetAsOf);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isNotNull();
        assertThat(result.get(0).getName().get("ko")).isEqualTo("Field Past");
    }
}
