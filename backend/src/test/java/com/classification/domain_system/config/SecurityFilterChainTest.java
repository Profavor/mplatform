package com.classification.domain_system.config;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.controller.FileController;
import com.classification.domain_system.controller.GlobalSystemDiagnosticsController;
import com.classification.domain_system.controller.InboxController;
import com.classification.domain_system.controller.NotificationController;
import com.classification.domain_system.controller.SystemInstallController;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.CustomPermissionEvaluator;
import com.classification.domain_system.security.JwtFilter;
import com.classification.domain_system.security.JwtUtil;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.GlobalSystemDiagnosticsService;
import com.classification.domain_system.service.InboxService;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.SseNotificationService;
import com.classification.domain_system.service.SystemInstallService;
import com.classification.domain_system.service.storage.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        SystemInstallController.class,
        GlobalSystemDiagnosticsController.class,
        NotificationController.class,
        InboxController.class,
        FileController.class
})
@Import({SecurityConfig.class, SecurityFilterChainTest.FilterChainTestConfig.class})
@TestPropertySource(properties = {
        "cors.allowed-origins=http://localhost:3000,http://localhost:8080",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/dummy-certs"
})
class SecurityFilterChainTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class FilterChainTestConfig {
        @org.springframework.context.annotation.Bean
        public JwtFilter jwtFilter() {
            return org.mockito.Mockito.mock(JwtFilter.class);
        }

        @org.springframework.context.annotation.Bean
        public CustomPermissionEvaluator customPermissionEvaluator() {
            return org.mockito.Mockito.mock(CustomPermissionEvaluator.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomPermissionEvaluator customPermissionEvaluator;

    @MockitoBean
    private SystemInstallService systemInstallService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthContext authContext;

    @MockitoBean
    private com.classification.domain_system.security.SecurityUtils securityUtils;

    @MockitoBean
    private PermissionService permissionService;

    @MockitoBean
    private GlobalSystemDiagnosticsService globalSystemDiagnosticsService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private SseNotificationService sseNotificationService;

    @MockitoBean
    private InboxService inboxService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("비인증 접근 허용: 시스템 최초 설치 상태 확인 (/api/system/install-status)은 인증 없이 200 OK를 반환해야 한다.")
    void installStatusShouldBePermittedWithoutAuth() throws Exception {
        when(systemInstallService.getInstallStatus()).thenReturn(new com.classification.domain_system.dto.SystemInstallStatusResponse(true, true));

        mockMvc.perform(get("/api/system/install-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비인증 접근 허용: SSE 알림 구독 (/api/notifications/subscribe)은 인증 없이 접근 가능해야 한다.")
    void notificationsSubscribeShouldBePermittedWithoutAuth() throws Exception {
        when(sseNotificationService.subscribe(any())).thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/api/notifications/subscribe")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비인증 접근 허용: 이메일 오픈 트래킹 (/api/inbox/track/open/{recipientId})은 인증 없이 200 OK 및 투명 GIF를 반환해야 한다.")
    void inboxTrackOpenShouldBePermittedWithoutAuth() throws Exception {
        UUID recipientId = UUID.randomUUID();

        mockMvc.perform(get("/api/inbox/track/open/" + recipientId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비인증 접근 차단: 시스템 진단 API (/api/system/diagnostics)는 비인증 시 401 Unauthorized를 반환해야 한다.")
    void systemDiagnosticsShouldBeForbiddenWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/system/diagnostics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("비인증 접근 차단: 파일 다운로드 API (/api/files/download/test.png)는 비인증 시 401 Unauthorized를 반환해야 한다.")
    void fileDownloadShouldBeUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/files/download/test.png"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("비인증 접근 차단: 파일 메타정보 API (/api/files/info/test.png)는 비인증 시 401 Unauthorized를 반환해야 한다.")
    void fileInfoShouldBeUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/files/info/test.png"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증 접근 성공: 인증된 사용자는 파일 다운로드 API (/api/files/download/test.txt)에 접근할 수 있어야 한다.")
    void fileDownloadShouldSucceedWithAuth() throws Exception {
        byte[] content = "hello test".getBytes();
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };
        when(fileStorageService.loadFileAsResource("test.txt")).thenReturn(resource);

        mockMvc.perform(get("/api/files/download/test.txt")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }
}
