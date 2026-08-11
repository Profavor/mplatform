package com.classification.domain_system.controller;

import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecordHistoryController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class RecordHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordHistoryRepository recordHistoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    @MockitoBean
    private com.classification.domain_system.repository.UserOrgHistoryRepository userOrgHistoryRepository;

    @MockitoBean
    private com.classification.domain_system.repository.DepartmentRepository departmentRepository;

    @MockitoBean
    private com.classification.domain_system.repository.TeamRepository teamRepository;

    @MockitoBean
    private com.classification.domain_system.repository.OrganizationRepository organizationRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.service.RecordService recordService;

    @Test
    @DisplayName("getRecordHistory - 히스토리 조회 시 changedBy ID가 사용자 이름(changedByName)으로 매핑된다")
    void getRecordHistory_ResolvesChangedByName() throws Exception {
        UUID recordId = UUID.randomUUID();
        String userId = "user-12345";
        
        RecordHistory history = new RecordHistory();
        history.setId(UUID.randomUUID());
        history.setRecordId(recordId);
        history.setChangeType("UPDATE");
        history.setChangedBy(userId);
        history.setPreviousData("{\"hire_date\":\"2022-05-06\"}");
        history.setNewData("{\"hire_date\":\"2022-05-07\"}");
        history.setChangedAt(LocalDateTime.now());

        User user = new User();
        user.setId(userId);
        user.setUsername("홍길동");
        user.setRole("ADMIN");

        when(recordHistoryRepository.findByRecordIdOrderByChangedAtDesc(recordId)).thenReturn(List.of(history));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/records/" + recordId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].changedBy").value(userId))
                .andExpect(jsonPath("$[0].changedByName").value("홍길동"))
                .andExpect(jsonPath("$[0].changedUserProfile.username").value("홍길동"))
                .andExpect(jsonPath("$[0].changedUserProfile.role").value("ADMIN"));
    }
}
