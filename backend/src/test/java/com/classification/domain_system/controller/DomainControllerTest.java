package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.security.JwtFilter;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.DomainService;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.SectorService;
import com.classification.domain_system.service.FieldGroupService;
import com.classification.domain_system.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(controllers = DomainController.class)
@AutoConfigureMockMvc(addFilters = false)
class DomainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DomainService domainService;

    @MockitoBean
    private FieldDefinitionService fieldDefinitionService;

    @MockitoBean
    private SectorService sectorService;

    @MockitoBean
    private FieldGroupService fieldGroupService;

    @MockitoBean
    private com.classification.domain_system.service.dq.DqRuleEngine dqRuleEngine;

    @MockitoBean
    private com.classification.domain_system.repository.DqRuleRepository dqRuleRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.service.DqScoreSnapshotService dqScoreSnapshotService;

    private Domain createTestDomain(UUID id, String koName, String enName) {
        Domain domain = new Domain();
        domain.setId(id);
        Map<String, String> name = new HashMap<>();
        name.put("ko", koName);
        name.put("en", enName);
        domain.setName(name);
        Map<String, String> desc = new HashMap<>();
        desc.put("ko", koName + " 설명");
        desc.put("en", enName + " description");
        domain.setDescription(desc);
        return domain;
    }

    @Test
    @DisplayName("POST /api/domains - 도메인 생성 성공")
    void createDomain() throws Exception {
        // given
        Domain domain = createTestDomain(UUID.randomUUID(), "인사", "HR");
        given(domainService.createDomain(any())).willReturn(domain);

        Map<String, Object> request = new HashMap<>();
        request.put("name", Map.of("ko", "인사", "en", "HR"));
        request.put("description", Map.of("ko", "인사 설명", "en", "HR description"));

        // when & then
        mockMvc.perform(post("/api/domains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.ko").value("인사"));
    }

    @Test
    @DisplayName("GET /api/domains - 전체 도메인 조회 성공")
    void getAllDomains() throws Exception {
        // given
        List<Domain> domains = List.of(
            createTestDomain(UUID.randomUUID(), "인사", "HR"),
            createTestDomain(UUID.randomUUID(), "재무", "Finance")
        );
        given(domainService.getAllDomains()).willReturn(domains);

        // when & then
        mockMvc.perform(get("/api/domains"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name.ko").value("인사"));
    }

    @Test
    @DisplayName("PUT /api/domains/{id} - 도메인 수정 성공")
    void updateDomain() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        Domain updated = createTestDomain(id, "인사관리", "HR Management");
        given(domainService.updateDomain(any(UUID.class), any())).willReturn(updated);

        Map<String, Object> request = new HashMap<>();
        request.put("name", Map.of("ko", "인사관리", "en", "HR Management"));
        request.put("identifierFieldId", UUID.randomUUID().toString());
        request.put("displayNameFieldId", UUID.randomUUID().toString());

        // when & then
        mockMvc.perform(put("/api/domains/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.ko").value("인사관리"));
    }

    @Test
    @DisplayName("PUT /api/domains/{id} - 식별/표시 필드가 null인 경우에도 도메인 수정 성공")
    void updateDomainWithNullFields() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        Domain updated = createTestDomain(id, "초기도메인", "Initial Domain");
        given(domainService.updateDomain(any(UUID.class), any())).willReturn(updated);

        Map<String, Object> request = new HashMap<>();
        request.put("name", Map.of("ko", "초기도메인", "en", "Initial Domain"));
        request.put("identifierFieldId", null);
        request.put("displayNameFieldId", null);

        // when & then
        mockMvc.perform(put("/api/domains/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name.ko").value("초기도메인"));
    }

    @Test
    @DisplayName("GET /api/domains/{domainId}/fields - 필드 조회 성공")
    void getDomainFields() throws Exception {
        // given
        UUID domainId = UUID.randomUUID();
        given(fieldDefinitionService.getDomainFields(domainId)).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/domains/{domainId}/fields", domainId))
            .andDo(print())
            .andExpect(status().isOk());
    }
}
