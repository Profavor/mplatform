package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ClassificationNodeRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.ClassificationNodeService;
import com.classification.domain_system.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClassificationNodeController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class ClassificationNodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @WebMvcTest 컨텍스트에서는 ObjectMapper가 자동 등록되지 않으므로 직접 초기화
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ClassificationNodeService nodeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private com.classification.domain_system.context.AuthContext authContext;

    private UUID domainId;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("루트 노드 생성 성공")
    void createNode_Success() throws Exception {
        ClassificationNodeRequest request = new ClassificationNodeRequest();
        request.setName(Map.of("ko", "Node 1", "en", "Node 1"));

        ClassificationNode mockNode = new ClassificationNode();
        mockNode.setId(UUID.randomUUID());
        mockNode.setName(Map.of("ko", "Node 1", "en", "Node 1"));
        mockNode.setPath("/Test Domain/Node 1");
        mockNode.setDepth(1);

        when(nodeService.createNode(eq(domainId), any(ClassificationNodeRequest.class)))
                .thenReturn(mockNode);

        mockMvc.perform(post("/api/domains/{domainId}/nodes", domainId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/Test Domain/Node 1"))
                .andExpect(jsonPath("$.depth").value(1));
    }

    @Test
    @DisplayName("트리 조회 성공")
    void getTree_Success() throws Exception {
        ClassificationNode node1 = new ClassificationNode();
        node1.setId(UUID.randomUUID());
        node1.setName(Map.of("ko", "루트1"));
        node1.setDepth(1);

        ClassificationNode node2 = new ClassificationNode();
        node2.setId(UUID.randomUUID());
        node2.setName(Map.of("ko", "루트2"));
        node2.setDepth(1);

        when(nodeService.getTree(eq(domainId), any())).thenReturn(List.of(node1, node2));

        mockMvc.perform(get("/api/domains/{domainId}/nodes/tree", domainId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name.ko").value("루트1"))
                .andExpect(jsonPath("$[1].name.ko").value("루트2"));
    }

    @Test
    @DisplayName("노드 수정 성공")
    void updateNode_Success() throws Exception {
        UUID nodeId = UUID.randomUUID();
        ClassificationNodeRequest request = new ClassificationNodeRequest();
        request.setName(Map.of("ko", "수정된 노드"));

        ClassificationNode updatedNode = new ClassificationNode();
        updatedNode.setId(nodeId);
        updatedNode.setName(Map.of("ko", "수정된 노드"));

        when(nodeService.updateNode(eq(domainId), eq(nodeId), any(ClassificationNodeRequest.class)))
                .thenReturn(updatedNode);

        mockMvc.perform(put("/api/domains/{domainId}/nodes/{nodeId}", domainId, nodeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name.ko").value("수정된 노드"));
    }

    @Test
    @DisplayName("노드 삭제 성공 (DELETE API)")
    void deleteNode_Success() throws Exception {
        UUID nodeId = UUID.randomUUID();

        doNothing().when(nodeService).deleteNode(domainId, nodeId);

        mockMvc.perform(delete("/api/domains/{domainId}/nodes/{nodeId}", domainId, nodeId))
                .andExpect(status().isNoContent());
    }
}
