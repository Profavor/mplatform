package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecordController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @WebMvcTest 컨텍스트에서는 ObjectMapper가 자동 등록되지 않으므로 직접 초기화
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ApprovalService approvalService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.service.RecordService recordService;

    @MockitoBean
    private com.classification.domain_system.service.BatchValidationService batchValidationService;

    @MockitoBean
    private com.classification.domain_system.service.RecordBatchUpsertService batchUpsertService;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("레코드 생성 요청 시 ApprovalRequest 반환")
    void createRecordRequest_Success() throws Exception {
        RecordRequest request = new RecordRequest();
        request.setData("{\"key\":\"value\"}");

        ApprovalRequest mockApproval = new ApprovalRequest();
        mockApproval.setId(UUID.randomUUID());
        mockApproval.setStatus("PENDING");

        when(approvalService.requestRecordCreation(eq(nodeId), any(RecordRequest.class)))
                .thenReturn(mockApproval);

        mockMvc.perform(post("/api/nodes/{nodeId}/records", nodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("노드 레코드 페이징 조회 성공")
    void getRecords_Success() throws Exception {
        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setData("{\"key\":\"value\"}");
        record.setStatus("APPROVED");

        Page<Record> page = new PageImpl<>(List.of(record), PageRequest.of(0, 100), 1);

        when(recordService.getRecords(eq(nodeId), any(), eq(false), eq(0), eq(100), any(Map.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/nodes/{nodeId}/records", nodeId)
                .param("page", "0")
                .param("size", "100")
                .param("includeChildren", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("존재하지 않는 노드 조회 시 빈 결과 반환")
    void getRecords_NodeNotFound_ReturnsEmpty() throws Exception {
        Page<Record> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);

        when(recordService.getRecords(eq(nodeId), any(), eq(false), eq(0), eq(100), any(Map.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/nodes/{nodeId}/records", nodeId)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("POST /api/nodes/{nodeId}/records/batch-upsert - 대량 레코드 배치 업서트 성공")
    void batchUpsertRecords_Success() throws Exception {
        com.classification.domain_system.dto.RecordBatchUpsertResponse mockResponse =
                com.classification.domain_system.dto.RecordBatchUpsertResponse.builder()
                        .totalCount(2)
                        .createdCount(1)
                        .updatedCount(1)
                        .failedCount(0)
                        .executionTimeMs(15)
                        .items(List.of(
                                com.classification.domain_system.dto.RecordBatchUpsertItemResult.builder()
                                        .index(0)
                                        .businessKey("PRODUCT_ID")
                                        .businessKeyValue("PID-1")
                                        .action("CREATED")
                                        .status("ACTIVE")
                                        .build(),
                                com.classification.domain_system.dto.RecordBatchUpsertItemResult.builder()
                                        .index(1)
                                        .businessKey("PRODUCT_ID")
                                        .businessKeyValue("PID-2")
                                        .action("UPDATED")
                                        .status("ACTIVE")
                                        .build()
                        ))
                        .build();

        when(batchUpsertService.batchUpsertRecords(eq(nodeId), any(), any()))
                .thenReturn(mockResponse);

        com.classification.domain_system.dto.RecordBatchUpsertRequest req =
                com.classification.domain_system.dto.RecordBatchUpsertRequest.builder()
                        .businessKey("PRODUCT_ID")
                        .autoApprove(true)
                        .records(List.of())
                        .build();

        mockMvc.perform(post("/api/nodes/{nodeId}/records/batch-upsert", nodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.createdCount").value(1))
                .andExpect(jsonPath("$.updatedCount").value(1))
                .andExpect(jsonPath("$.items[0].action").value("CREATED"))
                .andExpect(jsonPath("$.items[1].action").value("UPDATED"));
    }

    @Test
    @DisplayName("POST /api/nodes/{nodeId}/records/batch-upsert - JSON Object 형태의 data 페이로드 역직렬화 및 처리 성공")
    void batchUpsertRecords_JsonObjectData_Success() throws Exception {
        com.classification.domain_system.dto.RecordBatchUpsertResponse mockResponse =
                com.classification.domain_system.dto.RecordBatchUpsertResponse.builder()
                        .totalCount(1)
                        .createdCount(1)
                        .updatedCount(0)
                        .failedCount(0)
                        .executionTimeMs(10)
                        .items(List.of(
                                com.classification.domain_system.dto.RecordBatchUpsertItemResult.builder()
                                        .index(0)
                                        .businessKey("PRODUCT_ID")
                                        .businessKeyValue("PID-999")
                                        .action("CREATED")
                                        .status("ACTIVE")
                                        .build()
                        ))
                        .build();

        when(batchUpsertService.batchUpsertRecords(eq(nodeId), any(), any()))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                    "businessKey": "PRODUCT_ID",
                    "autoApprove": true,
                    "records": [
                        {
                            "data": {
                                "PRODUCT_ID": "PID-999",
                                "PRODUCT_NAME": "곤약밥",
                                "PRODUCT_PRICE": 15000
                            }
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/nodes/{nodeId}/records/batch-upsert", nodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.createdCount").value(1));
    }
}
