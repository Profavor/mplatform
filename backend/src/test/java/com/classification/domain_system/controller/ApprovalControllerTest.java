package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApprovalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprovalService approvalService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.repository.UserRepository userRepository;

    @MockitoBean
    private com.classification.domain_system.service.FieldDefinitionService fieldDefinitionService;

    private UUID nodeId;
    private UUID stepId;
    private String approverId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        stepId = UUID.randomUUID();
        approverId = UUID.randomUUID().toString();
        when(authContext.getUserId()).thenReturn(approverId);
    }

    // ─────────────────────────────────────────────────────────────────
    // hasEffectiveWorkflow 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("hasEffectiveWorkflow")
    class HasEffectiveWorkflow {

        @Test
        @DisplayName("config가 null이면 false 반환")
        void configNull_ReturnsFalse() throws Exception {
            when(approvalService.resolveWorkflow(eq(nodeId), eq("CREATE"))).thenReturn(null);

            mockMvc.perform(get("/api/approval-requests/effective-workflow/{nodeId}", nodeId)
                    .param("actionType", "CREATE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("stepsConfig가 빈 JSON이면 false 반환")
        void emptyStepsJson_ReturnsFalse() throws Exception {
            WorkflowConfig config = new WorkflowConfig();
            config.setStepsConfig("{\"steps\":[],\"observerIds\":[]}");
            when(approvalService.resolveWorkflow(eq(nodeId), eq("CREATE"))).thenReturn(config);

            mockMvc.perform(get("/api/approval-requests/effective-workflow/{nodeId}", nodeId)
                    .param("actionType", "CREATE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("stepsConfig가 실제 단계를 포함하면 true 반환")
        void validStepsConfig_ReturnsTrue() throws Exception {
            WorkflowConfig config = new WorkflowConfig();
            config.setStepsConfig("{\"steps\":[{\"approverIds\":[]}],\"observerIds\":[]}");
            when(approvalService.resolveWorkflow(eq(nodeId), eq("CREATE"))).thenReturn(config);

            mockMvc.perform(get("/api/approval-requests/effective-workflow/{nodeId}", nodeId)
                    .param("actionType", "CREATE"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // approveStep 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("approveStep")
    class ApproveStep {

        @Test
        @DisplayName("payload에 comment 포함 시 AuthContext의 userId로 승인 성공")
        void withComment_Success() throws Exception {
            ApprovalRequest approvalRequest = new ApprovalRequest();
            approvalRequest.setId(UUID.randomUUID());
            approvalRequest.setStatus("APPROVED");

            when(approvalService.approveStep(eq(stepId), eq(approverId), eq("승인합니다")))
                    .thenReturn(approvalRequest);

            mockMvc.perform(post("/api/approval-requests/steps/{stepId}/approve", stepId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"comment\":\"승인합니다\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @DisplayName("spoofed approverId 파라미터가 전달되어도 무시되고 AuthContext의 userId가 사용됨")
        void withSpoofedParam_UsesAuthContext() throws Exception {
            ApprovalRequest approvalRequest = new ApprovalRequest();
            approvalRequest.setId(UUID.randomUUID());
            approvalRequest.setStatus("APPROVED");

            String spoofedId = UUID.randomUUID().toString();
            when(approvalService.approveStep(eq(stepId), eq(approverId), isNull()))
                    .thenReturn(approvalRequest);

            mockMvc.perform(post("/api/approval-requests/steps/{stepId}/approve", stepId)
                    .param("approverId", spoofedId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));

            verify(approvalService).approveStep(eq(stepId), eq(approverId), isNull());
        }

        @Test
        @DisplayName("AuthContext에 userId가 없으면 거부됨")
        void withoutAuthContextUserId_Fails() throws Exception {
            when(authContext.getUserId()).thenReturn(null);

            mockMvc.perform(post("/api/approval-requests/steps/{stepId}/approve", stepId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("rejectStep")
    class RejectStep {

        @Test
        @DisplayName("reject 시 comment 전달되어 거절 성공")
        void withComment_Success() throws Exception {
            ApprovalRequest approvalRequest = new ApprovalRequest();
            approvalRequest.setId(UUID.randomUUID());
            approvalRequest.setStatus("REJECTED");

            when(approvalService.rejectStep(eq(stepId), eq(approverId), eq("반려 사유")))
                    .thenReturn(approvalRequest);

            mockMvc.perform(post("/api/approval-requests/steps/{stepId}/reject", stepId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"comment\":\"반려 사유\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 페이지 조회 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("페이지 목록 조회")
    class PagedList {

        @Test
        @DisplayName("getPendingRequests - page/size 파라미터가 올바르게 PageRequest에 바인딩")
        void getPendingRequests_PageBindingCorrect() throws Exception {
            ApprovalRequest req = new ApprovalRequest();
            req.setId(UUID.randomUUID());

            when(approvalService.getPendingRequests(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(req), PageRequest.of(0, 5), 1));

            mockMvc.perform(get("/api/approval-requests")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));

            // 실제로 page=0, size=5 로 PageRequest가 생성되어 전달됐는지 검증
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(approvalService).getPendingRequests(captor.capture());
            assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
            assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("getMyTodos - assigneeId 파라미터가 서비스로 전달")
        void getMyTodos_AssigneeIdPassed() throws Exception {
            String assigneeId = UUID.randomUUID().toString();
            ApprovalStep step = new ApprovalStep();
            step.setId(UUID.randomUUID());

            when(approvalService.getMyTodos(eq(assigneeId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(step), PageRequest.of(0, 100), 1));

            mockMvc.perform(get("/api/approval-requests/todos")
                    .param("assigneeId", assigneeId.toString())
                    .param("page", "0")
                    .param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("getAllRequests - search 및 status 파라미터가 서비스로 전달")
        void getAllRequests_ParamsPassed() throws Exception {
            ApprovalRequest req = new ApprovalRequest();
            req.setId(UUID.randomUUID());

            when(approvalService.getAllRequests(eq("검색어"), eq("APPROVED,PENDING"), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(req), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/api/approval-requests/all")
                    .param("search", "검색어")
                    .param("status", "APPROVED,PENDING")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("getAllRequests - filterModel 파라미터가 서비스로 전달")
        void getAllRequests_FilterModelPassed() throws Exception {
            ApprovalRequest req = new ApprovalRequest();
            req.setId(UUID.randomUUID());

            String fmJson = "{\"status\":{\"filterType\":\"set\",\"values\":[\"APPROVED\"]}}";
            when(approvalService.getAllRequests(eq("검색어"), eq("APPROVED"), eq(fmJson), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(req), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/api/approval-requests/all")
                    .param("search", "검색어")
                    .param("status", "APPROVED")
                    .param("filterModel", fmJson)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }
}
