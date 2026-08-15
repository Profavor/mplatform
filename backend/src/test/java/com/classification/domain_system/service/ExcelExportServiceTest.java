package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExcelExportServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private ExcelExportService excelExportService;

    private UUID domainId;
    private FieldDefinition field;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        field = new FieldDefinition();
        field.setKey("userName");
        field.setName(Map.of("ko", "사용자명", "en", "User Name"));
        field.setType("STRING");
        field.setRequired(true);
    }

    @Test
    @DisplayName("generateTemplate: UTF-8 BOM 및 3행 템플릿 정상 생성")
    void testGenerateTemplate() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(field));

        byte[] templateBytes = excelExportService.generateTemplate(domainId, null, "ko");
        String content = new String(templateBytes, StandardCharsets.UTF_8);

        assertThat(content).startsWith("\uFEFF");
        assertThat(content).contains("사용자명*");
        assertThat(content).contains("userName");
        assertThat(content).contains("예시 텍스트");
    }

    @Test
    @DisplayName("exportRecordsToCsv: 레코드 CSV 데이터 내보내기 정상 동작")
    void testExportRecordsToCsv() {
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(field));

        Record record = new Record();
        record.setData("{\"userName\": \"홍길동\"}");
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(record));

        byte[] csvBytes = excelExportService.exportRecordsToCsv(domainId, null, "ko");
        String content = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(content).startsWith("\uFEFF");
        assertThat(content).contains("사용자명");
        assertThat(content).contains("홍길동");
    }
}
