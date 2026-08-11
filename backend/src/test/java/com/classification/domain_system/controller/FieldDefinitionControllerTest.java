package com.classification.domain_system.controller;

import com.classification.domain_system.dto.FieldDefinitionRequest;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FieldDefinitionController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class FieldDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FieldDefinitionService fieldService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("addField - 올바른 파라미터 전달 및 결과 반환")
    void addField_Success() throws Exception {
        UUID nodeId = UUID.randomUUID();
        FieldDefinition def = new FieldDefinition();
        def.setKey("new_field");

        when(fieldService.addField(eq(nodeId), any(FieldDefinitionRequest.class))).thenReturn(def);

        mockMvc.perform(post("/api/nodes/{nodeId}/fields", nodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"new_field\",\"type\":\"TEXT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("new_field"));
    }

    @Test
    @DisplayName("updateField - 올바른 파라미터 전달 및 결과 반환")
    void updateField_Success() throws Exception {
        UUID nodeId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        FieldDefinition def = new FieldDefinition();
        def.setKey("updated_field");

        when(fieldService.updateField(eq(nodeId), eq(fieldId), any(FieldDefinitionRequest.class))).thenReturn(def);

        mockMvc.perform(put("/api/nodes/{nodeId}/fields/{fieldId}", nodeId, fieldId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"updated_field\",\"type\":\"TEXT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("updated_field"));
    }

    @Test
    @DisplayName("getEffectiveFields - 리스트 전체 반환")
    void getEffectiveFields_ReturnsList() throws Exception {
        UUID nodeId = UUID.randomUUID();
        FieldDefinition def1 = new FieldDefinition(); def1.setKey("field1");
        FieldDefinition def2 = new FieldDefinition(); def2.setKey("field2");

        when(fieldService.getEffectiveFields(nodeId)).thenReturn(List.of(def1, def2));

        mockMvc.perform(get("/api/nodes/{nodeId}/fields/effective", nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].key").value("field1"))
                .andExpect(jsonPath("$[1].key").value("field2"));
    }

    @Test
    @DisplayName("getEffectiveFieldsPage - DB 페이징 정상 작동")
    void getEffectiveFieldsPage_DBPagination() throws Exception {
        UUID nodeId = UUID.randomUUID();
        FieldDefinition def1 = new FieldDefinition(); def1.setKey("f1");
        FieldDefinition def2 = new FieldDefinition(); def2.setKey("f2");
        FieldDefinition def3 = new FieldDefinition(); def3.setKey("f3");

        // page 0, size 2 -> f1, f2
        when(fieldService.getEffectiveFieldsPage(eq(nodeId), eq(PageRequest.of(0, 2))))
                .thenReturn(new PageImpl<>(List.of(def1, def2), PageRequest.of(0, 2), 3));

        mockMvc.perform(get("/api/nodes/{nodeId}/fields/effective/page", nodeId)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].key").value("f1"));

        // page 1, size 2 -> f3
        when(fieldService.getEffectiveFieldsPage(eq(nodeId), eq(PageRequest.of(1, 2))))
                .thenReturn(new PageImpl<>(List.of(def3), PageRequest.of(1, 2), 3));

        mockMvc.perform(get("/api/nodes/{nodeId}/fields/effective/page", nodeId)
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].key").value("f3"));
    }
}
