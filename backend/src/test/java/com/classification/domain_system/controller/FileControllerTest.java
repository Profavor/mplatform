package com.classification.domain_system.controller;

import com.classification.domain_system.service.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.context.AuthContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@org.springframework.context.annotation.Import({com.classification.domain_system.config.SecurityConfig.class, com.classification.domain_system.config.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private AuthContext authContext;

    @Test
    void downloadFile_DangerousExtension_ForcedAttachment() throws Exception {
        String fileName = "malicious.html";
        Resource mockResource = new ByteArrayResource("<html><script>alert('xss')</script></html>".getBytes()) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        Mockito.when(fileStorageService.loadFileAsResource(fileName)).thenReturn(mockResource);

        mockMvc.perform(get("/api/files/download/" + fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"));
    }

    @Test
    void downloadFile_SafeExtension_Inline() throws Exception {
        String fileName = "safe.jpg";
        Resource mockResource = new ByteArrayResource("fake-image".getBytes()) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        Mockito.when(fileStorageService.loadFileAsResource(fileName)).thenReturn(mockResource);

        mockMvc.perform(get("/api/files/download/" + fileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("inline")));
    }
}
