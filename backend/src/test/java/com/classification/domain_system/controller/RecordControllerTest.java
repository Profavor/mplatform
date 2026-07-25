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
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @WebMvcTest 컨텍스트에서는 ObjectMapper가 자동 등록되지 않으므로 직접 초기화
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ApprovalService approvalService;

    @MockitoBean
    private RecordRepository recordRepository;

    @MockitoBean
    private ClassificationNodeRepository classificationNodeRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.service.RecordService recordService;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        when(recordService.prepareRecordsForRead(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(recordService.prepareRecordForRead(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

        when(recordRepository.findDynamicRecords(
                any(List.class),
                any(),
                any(Map.class),
                any(PageRequest.class)
        )).thenReturn(page);

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
        // classificationNodeRepository가 없어도 nodeId 자체를 targetNodeIds에 추가하므로
        // 빈 Page 결과 반환
        Page<Record> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);

        when(recordRepository.findDynamicRecords(
                any(List.class),
                any(),
                any(Map.class),
                any(PageRequest.class)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/nodes/{nodeId}/records", nodeId)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
