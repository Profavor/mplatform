package com.classification.domain_system.controller;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionMasterService;
import com.classification.domain_system.service.PermissionService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PermissionMasterController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class PermissionMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionMasterService permissionMasterService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("getAllGroups - 전체 권한 그룹 조회 성공")
    void getAllGroups_Success() throws Exception {
        PermissionGroup group = new PermissionGroup();
        group.setId("domain");
        group.setCode("domain");
        group.setTitleKo("도메인 권한");

        when(permissionMasterService.getAllPermissionGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/permissions/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("domain"))
                .andExpect(jsonPath("$[0].titleKo").value("도메인 권한"));
    }

    @Test
    @DisplayName("createGroup - 권한 그룹 생성 성공")
    void createGroup_Success() throws Exception {
        PermissionGroup group = new PermissionGroup();
        group.setCode("custom");
        group.setTitleKo("커스텀 권한");

        when(permissionMasterService.createGroup(any())).thenReturn(group);

        mockMvc.perform(post("/api/permissions/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"custom\",\"titleKo\":\"커스텀 권한\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleKo").value("커스텀 권한"));
    }

    @Test
    @DisplayName("addItemToGroup - 그룹 내 항목 추가 성공")
    void addItemToGroup_Success() throws Exception {
        PermissionItem item = new PermissionItem();
        item.setLabelKo("조회 (read)");
        item.setPermValue("custom:read");

        when(permissionMasterService.addItemToGroup(eq("custom"), any())).thenReturn(item);

        mockMvc.perform(post("/api/permissions/groups/custom/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"labelKo\":\"조회 (read)\",\"permValue\":\"custom:read\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permValue").value("custom:read"));
    }

    @Test
    @DisplayName("deleteItem - 항목 삭제 성공")
    void deleteItem_Success() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(delete("/api/permissions/groups/items/" + itemId))
                .andExpect(status().isOk());

        verify(permissionMasterService).deleteItem(itemId);
    }
}
