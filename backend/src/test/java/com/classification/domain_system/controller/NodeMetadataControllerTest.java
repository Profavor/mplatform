package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.ClassificationNodeService;
import com.classification.domain_system.service.PermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NodeMetadataController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class NodeMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClassificationNodeService nodeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    @Test
    @DisplayName("이슈 #222: 존재하는 nodeId로 GET /api/nodes/{nodeId} 호출 시 노드 메타정보 200 반환")
    void getNode_Success() throws Exception {
        UUID nodeId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "국회온 의원 마스터", "en", "Gukhoeon Member Master"));

        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);
        node.setName(Map.of("ko", "제22대 국회의원", "en", "22nd Assembly Members"));
        node.setPath("/국회온 의원 마스터/제22대 국회의원");
        node.setDepth(1);

        when(nodeService.getNode(nodeId)).thenReturn(node);

        mockMvc.perform(get("/api/nodes/{nodeId}", nodeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(nodeId.toString()))
                .andExpect(jsonPath("$.name.ko").value("제22대 국회의원"))
                .andExpect(jsonPath("$.path").value("/국회온 의원 마스터/제22대 국회의원"))
                .andExpect(jsonPath("$.depth").value(1))
                .andExpect(jsonPath("$.domainId").value(domainId.toString()))
                .andExpect(jsonPath("$.domainName.ko").value("국회온 의원 마스터"));
    }

    @Test
    @DisplayName("이슈 #222: 존재하지 않는 nodeId로 GET /api/nodes/{nodeId} 호출 시 404 RESOURCE_NOT_FOUND 반환")
    void getNode_NotFound() throws Exception {
        UUID nonExistentNodeId = UUID.randomUUID();
        when(nodeService.getNode(nonExistentNodeId))
                .thenThrow(new ResourceNotFoundException("Node not found with id: " + nonExistentNodeId));

        mockMvc.perform(get("/api/nodes/{nodeId}", nonExistentNodeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
