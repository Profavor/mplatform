package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.RecordMergeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecordMergeController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class RecordMergeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordMergeService recordMergeService;

    @MockitoBean
    private com.classification.domain_system.service.ApprovalService approvalService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    private UUID recordId;
    private UUID mergedRecordId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        mergedRecordId = UUID.randomUUID();
    }

    @Test
    @DisplayName("수동 병합 POST /api/records/merge 성공 시 ApprovalRequest(RECORD_MERGE) 반환")
    void mergeRecords_Success() throws Exception {
        com.classification.domain_system.entity.ApprovalRequest approval = new com.classification.domain_system.entity.ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setTargetType("RECORD_MERGE");
        approval.setTargetId(recordId);

        when(approvalService.requestRecordMerge(any(), any())).thenReturn(approval);

        String jsonBody = "{\"survivorRecordId\":\"" + recordId + "\", \"mergedRecordIds\":[\"" + mergedRecordId + "\"]}";

        mockMvc.perform(post("/api/records/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetType").value("RECORD_MERGE"))
                .andExpect(jsonPath("$.targetId").value(recordId.toString()));
    }

    @Test
    @DisplayName("자동 병합 POST /api/records/merge/auto 성공 시 ApprovalRequest(RECORD_MERGE) 반환")
    void autoMergeRecords_Success() throws Exception {
        com.classification.domain_system.entity.ApprovalRequest approval = new com.classification.domain_system.entity.ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setTargetType("RECORD_MERGE");
        approval.setTargetId(recordId);

        RecordMergeService.MergeRequest mergeReq = new RecordMergeService.MergeRequest();
        mergeReq.survivorRecordId = recordId;
        mergeReq.mergedRecordIds = List.of(mergedRecordId);

        when(recordMergeService.buildSurvivorshipMergeRequest(eq(recordId), eq(List.of(mergedRecordId)))).thenReturn(mergeReq);
        when(approvalService.requestRecordMerge(any(), any())).thenReturn(approval);

        String jsonBody = "{\"survivorRecordId\":\"" + recordId + "\", \"mergedRecordIds\":[\"" + mergedRecordId + "\"]}";

        mockMvc.perform(post("/api/records/merge/auto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetType").value("RECORD_MERGE"))
                .andExpect(jsonPath("$.targetId").value(recordId.toString()));
    }

    @Test
    @DisplayName("MERGED 레코드 Un-merge 요청 성공 시 ACTIVE 레코드 반환")
    void unmergeRecord_Success() throws Exception {
        Record activeRecord = new Record();
        activeRecord.setId(recordId);
        activeRecord.setStatus("ACTIVE");
        activeRecord.setMergedIntoRecordId(null);

        when(recordMergeService.unmergeRecord(eq(recordId), any()))
                .thenReturn(activeRecord);

        mockMvc.perform(post("/api/records/{id}/unmerge", recordId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recordId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("존재하지 않는 레코드 Un-merge 요청 시 404 반환")
    void unmergeRecord_NotFound() throws Exception {
        when(recordMergeService.unmergeRecord(eq(recordId), any()))
                .thenThrow(new ResourceNotFoundException("Record not found for unmerge: " + recordId));

        mockMvc.perform(post("/api/records/{id}/unmerge", recordId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("MERGED 상태가 아닌 레코드 Un-merge 요청 시 400 반환")
    void unmergeRecord_NotMergedStatus() throws Exception {
        when(recordMergeService.unmergeRecord(eq(recordId), any()))
                .thenThrow(new BusinessException(ErrorCode.INVALID_INPUT, "Record is not in MERGED status."));

        mockMvc.perform(post("/api/records/{id}/unmerge", recordId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
