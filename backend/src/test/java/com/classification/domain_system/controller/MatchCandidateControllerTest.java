package com.classification.domain_system.controller;

import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.MatchCandidateService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchCandidateController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchCandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchCandidateService matchCandidateService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("P0-1: POST /api/match-candidates/{id}/ignore 엔드포인트가 200 OK 및 candidateService.ignoreCandidate 호출 결과를 반환한다")
    void ignoreCandidate_Success() throws Exception {
        UUID candidateId = UUID.randomUUID();
        MatchCandidate candidate = new MatchCandidate();
        candidate.setId(candidateId);
        candidate.setStatus("IGNORED");

        when(matchCandidateService.ignoreCandidate(eq(candidateId), any())).thenReturn(candidate);

        mockMvc.perform(post("/api/match-candidates/" + candidateId + "/ignore"))
                .andExpect(status().isOk());
    }
}
