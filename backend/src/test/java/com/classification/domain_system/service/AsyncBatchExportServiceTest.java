package com.classification.domain_system.service;

import com.classification.domain_system.dto.AsyncBatchDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncBatchExportServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    private AsyncBatchExportService asyncBatchExportService;

    @BeforeEach
    void setUp() {
        asyncBatchExportService = new AsyncBatchExportService(recordRepository, fieldDefinitionRepository);
    }

    @Test
    @DisplayName("도메인 ID 기반 비동기 엑셀 내보내기 태스크를 시작하고 SXSSF 스트리밍으로 파일을 생성한다")
    void testStartAsyncExportAndGenerateExcel() {
        UUID domainId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인", "en", "Customer"));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setName(Map.of("ko", "VIP 고객"));
        node.setDomain(domain);

        Record r1 = new Record();
        r1.setId(UUID.randomUUID());
        r1.setNode(node);
        r1.setData("{\"CUST_NO\":\"C001\",\"NAME\":\"홍길동\",\"AMOUNT\":150000}");
        r1.setStatus("ACTIVE");
        r1.setUpdatedAt(LocalDateTime.now());

        FieldDefinition fd1 = new FieldDefinition();
        fd1.setId(UUID.randomUUID());
        fd1.setKey("CUST_NO");
        fd1.setName(Map.of("ko", "고객번호"));
        fd1.setType("TEXT");

        FieldDefinition fd2 = new FieldDefinition();
        fd2.setId(UUID.randomUUID());
        fd2.setKey("NAME");
        fd2.setName(Map.of("ko", "고객명"));
        fd2.setType("TEXT");


        when(recordRepository.countByNodeDomainIdAndStatus(any(), any())).thenReturn(1L);
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(r1));
        when(fieldDefinitionRepository.findDomainFieldsWithSort(domainId)).thenReturn(List.of(fd1, fd2));

        AsyncBatchDto.BatchTaskResponse task = asyncBatchExportService.startAsyncExport(domainId, "XLSX");

        assertNotNull(task);
        assertNotNull(task.getTaskId());
        assertTrue(List.of("PROCESSING", "COMPLETED").contains(task.getStatus()));


        // 비동기 처리 실행
        asyncBatchExportService.processExportAsync(task.getTaskId(), 1L);

        AsyncBatchDto.BatchTaskResponse status = asyncBatchExportService.getTaskStatus(task.getTaskId());
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(100, status.getProgressPercent());
        assertNotNull(status.getDownloadUrl());

        byte[] fileBytes = asyncBatchExportService.downloadTaskFile(task.getTaskId());
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 100);
    }

    @Test
    @DisplayName("그리드 선택 데이터 기반 엑셀 내보내기 시 정확한 바이트가 생성된다")
    void testExportWithGridData() {
        AsyncBatchDto.ExportAsyncRequest request = new AsyncBatchDto.ExportAsyncRequest();
        request.setColumns(List.of(
            Map.of("field", "empNo", "headerName", "사번"),
            Map.of("field", "name", "headerName", "이름"),
            Map.of("field", "dept", "headerName", "부서")
        ));

        Map<String, Object> row1 = new HashMap<>();
        row1.put("empNo", "EMP-001");
        row1.put("name", "김철수");
        row1.put("dept", "개발팀");
        request.setRecords(List.of(row1));


        AsyncBatchDto.BatchTaskResponse task = asyncBatchExportService.startAsyncExportWithData(null, "XLSX", request);

        assertNotNull(task);
        asyncBatchExportService.processExportAsync(task.getTaskId(), 1L);

        byte[] fileBytes = asyncBatchExportService.downloadTaskFile(task.getTaskId());
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 0);
    }

    @Test
    @DisplayName("서브테이블 JSON, 파일 URL, 다국어 객체가 포함된 데이터도 정제된 포맷으로 엑셀 파일이 정상 생성된다")
    void testFormattedExcelExport() {
        AsyncBatchDto.ExportAsyncRequest request = new AsyncBatchDto.ExportAsyncRequest();
        request.setColumns(List.of(
            Map.of("field", "empNo", "headerName", "사번"),
            Map.of("field", "name", "headerName", "이름"),
            Map.of("field", "history", "headerName", "학력 이력"),
            Map.of("field", "files", "headerName", "첨부파일"),
            Map.of("field", "ref", "headerName", "참조")
        ));

        Map<String, Object> row1 = new HashMap<>();
        row1.put("empNo", "0000001");
        row1.put("name", Map.of("ko", "인치국", "en", "Lin chigoog"));
        row1.put("history", List.of(
            Map.of("학교명", "안산공업고등학교", "졸업년도", 2005, "학위", "고졸"),
            Map.of("학교명", "세명대학교", "졸업년도", 2012, "학위", "학사")
        ));
        row1.put("files", "[\"/api/files/download/93aeb38a832e67f61bafed2c6fed3e4d486e2bf2c6e0b59efc76a7311cb772b8.xlsx?name=export_master_data_ee69f5fa.xlsx\"]");
        row1.put("ref", "c99b4fda-aeda-4d39-aa62-1a7c682543c8");
        request.setRecords(List.of(row1));

        AsyncBatchDto.BatchTaskResponse task = asyncBatchExportService.startAsyncExportWithData(null, "XLSX", request);
        assertNotNull(task);
        asyncBatchExportService.processExportAsync(task.getTaskId(), 1L);

        byte[] fileBytes = asyncBatchExportService.downloadTaskFile(task.getTaskId());
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 0);
    }

    @Test
    @DisplayName("존재하지 않는 태스크 조회 시 NOT_FOUND 상태를 반환한다")
    void testGetTaskStatusNotFound() {
        AsyncBatchDto.BatchTaskResponse status = asyncBatchExportService.getTaskStatus("non-existent-id");
        assertEquals("NOT_FOUND", status.getStatus());
    }
}

