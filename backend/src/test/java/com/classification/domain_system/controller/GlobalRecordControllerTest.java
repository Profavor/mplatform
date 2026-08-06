package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(controllers = GlobalRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(com.classification.domain_system.exception.GlobalExceptionHandler.class)
class GlobalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordRepository recordRepository;

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
    private com.classification.domain_system.service.MultiAxisRecordService multiAxisRecordService;

    private UUID domainId;
    private UUID recordId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        recordId = UUID.randomUUID();
        when(recordService.prepareRecordsForRead(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(recordService.prepareRecordForRead(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("도메인 전체 레코드 페이징 조회 성공")
    void getRecordsByDomain_Success() throws Exception {
        Record record = new Record();
        record.setId(recordId);
        record.setData("{\"ticker\":\"AAPL\"}");
        record.setStatus("APPROVED");

        Page<Record> page = new PageImpl<>(List.of(record), PageRequest.of(0, 100), 1);

        when(recordRepository.findDynamicRecordsByDomain(
                eq(domainId),
                any(Map.class),
                any(PageRequest.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/records/domain/{domainId}", domainId)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("단건 레코드 조회 성공")
    void getRecord_Success() throws Exception {
        Record record = new Record();
        record.setId(recordId);
        record.setData("{\"key\":\"value\"}");
        record.setStatus("APPROVED");

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/records/{id}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("존재하지 않는 레코드 조회 시 404 반환")
    void getRecord_NotFound() throws Exception {
        when(recordRepository.findById(recordId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/records/{id}", recordId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("레코드 수정 요청 중 예외 발생 시 표준 ErrorResponse JSON 반환")
    void updateRecordRequest_Error_ReturnsStandardErrorJson() throws Exception {
        when(approvalService.requestRecordUpdate(eq(recordId), any()))
                .thenThrow(new com.classification.domain_system.exception.BusinessException(
                        com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                        "Invalid data format"
                ));

        mockMvc.perform(post("/api/records/{id}/update-request", recordId)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"update\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("Invalid data format"));
    }

    @Test
    @DisplayName("레코드 삭제 요청 중 예외 발생 시 표준 ErrorResponse JSON 반환")
    void deleteRecordRequest_Error_ReturnsStandardErrorJson() throws Exception {
        when(approvalService.requestRecordDeletion(eq(recordId), any()))
                .thenThrow(new com.classification.domain_system.exception.BusinessException(
                        com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                        "Deletion prohibited"
                ));

        mockMvc.perform(post("/api/records/{id}/delete-request", recordId)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"comment\":\"delete\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("Deletion prohibited"));
    }
}
