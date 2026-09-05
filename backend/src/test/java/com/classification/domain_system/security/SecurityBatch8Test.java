package com.classification.domain_system.security;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.IntegrationChannel;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.integration.DataMappingTransformer;
import com.classification.domain_system.integration.InboundIntegrationService;
import com.classification.domain_system.repository.IntegrationChannelRepository;
import com.classification.domain_system.service.DataMaskingService;
import com.classification.domain_system.service.FieldEncryptionService;
import com.classification.domain_system.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityBatch8Test {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // For InboundIntegrationService test
    @Mock private IntegrationChannelRepository channelRepository;
    @Mock private FieldEncryptionService encryptionService;
    @Mock private com.classification.domain_system.integration.IntegrationLogService logService;
    @InjectMocks private InboundIntegrationService inboundIntegrationService;

    // For DataMaskingService test
    @Mock private com.classification.domain_system.repository.RecordRepository recordRepository;
    @Mock private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    @InjectMocks private DataMaskingService dataMaskingService;

    // For JwtFilter test
    @Mock private JwtUtil jwtUtil;
    @Mock private PermissionService permissionService;
    @Mock private AuthContext authContext;
    @Mock private com.classification.domain_system.repository.UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("#188: User 직렬화 시 password, activeSessionId, encryptedTempPassword 등 민감 정보가 제외되어야 한다")
    void testUserJsonSerializationIgnoresSensitiveFields() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("$2a$10$e8wF9aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890");
        user.setEncryptedTempPassword("encrypted_temp_12345");
        user.setActiveSessionId("session-token-9999");
        user.setFailedLoginCount(5);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).contains("\"username\":\"testuser\"");
        assertThat(json).contains("\"email\":\"test@example.com\"");
        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("encryptedTempPassword");
        assertThat(json).doesNotContain("activeSessionId");
        assertThat(json).doesNotContain("failedLoginCount");
        assertThat(json).doesNotContain("lockedUntil");
    }

    @Test
    @DisplayName("#188: JwtFilter는 REFRESH 토큰으로 API 접근 시 인증을 거부해야 한다")
    void testJwtFilterRejectsRefreshTokenForApiAccess() throws ServletException, IOException {
        JwtFilter jwtFilter = JwtFilter.createForTest(jwtUtil, permissionService, authContext, userRepository);

        when(request.getServletPath()).thenReturn("/api/records");
        when(request.getHeader("Authorization")).thenReturn("Bearer mock.refresh.token");
        when(jwtUtil.extractUsername("mock.refresh.token")).thenReturn("testuser");
        when(jwtUtil.isTokenValid("mock.refresh.token")).thenReturn(true);

        io.jsonwebtoken.Claims mockClaims = mock(io.jsonwebtoken.Claims.class);
        when(mockClaims.get("tokenType", String.class)).thenReturn("REFRESH");
        when(jwtUtil.extractAllClaims("mock.refresh.token")).thenReturn(mockClaims);

        jwtFilter.doFilter(request, response, filterChain);

        // SecurityContext에 인증 정보가 설정되지 않아야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(permissionService, never()).getAuthoritiesForUser(anyString(), anyString());
    }

    @Test
    @DisplayName("#171: DataMaskingService는 API 응답 JSON에서 _idx_* 블라인드 인덱스/암호문 키를 완전히 제거해야 한다")
    void testDataMaskingServiceRemovesBlindIndexKeys() throws Exception {
        FieldDefinition fEmail = new FieldDefinition();
        fEmail.setKey("email");
        fEmail.setIsEncrypted(true);
        fEmail.setMaskingPattern("EMAIL");

        String rawJson = "{"
                + "\"name\":\"홍길동\","
                + "\"email\":\"vault:v1:someCiphertext\","
                + "\"_mask_email\":\"h***@company.com\","
                + "\"_idx_email\":\"vault:v1:blindIndexHash123\","
                + "\"_idx_ssn\":\"vault:v1:blindIndexHash456\""
                + "}";

        String resultJson = dataMaskingService.maskJsonData(rawJson, List.of(fEmail), false);
        Map<?, ?> resultMap = objectMapper.readValue(resultJson, Map.class);

        assertThat(resultMap.get("name")).isEqualTo("홍길동");
        assertThat(resultMap.get("email")).isEqualTo("h***@company.com");
        assertThat(resultMap.containsKey("_idx_email")).isFalse();
        assertThat(resultMap.containsKey("_idx_ssn")).isFalse();
        assertThat(resultJson).doesNotContain("_idx_email");
        assertThat(resultJson).doesNotContain("_idx_ssn");
    }

    @Test
    @DisplayName("#182: Inbound 웹훅 인증은 잘못된 토큰 시 fail-closed(SecurityException)되어야 한다")
    void testInboundAuthenticationFailsClosedOnInvalidAuth() {
        UUID channelId = UUID.randomUUID();
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setDirection("INBOUND");
        channel.setActive(true);
        channel.setConfigJson("{\"authType\":\"BEARER_TOKEN\",\"secretToken\":\"encryptedToken\"}");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));
        when(encryptionService.isEncrypted("encryptedToken")).thenReturn(true);
        when(encryptionService.decrypt("encryptedToken")).thenReturn("validToken");

        assertThrows(SecurityException.class, () ->
                inboundIntegrationService.processInboundData(channelId, "{}", "Bearer wrongToken", null, null)
        );
    }

    @Test
    @DisplayName("#182: Inbound 웹훅 인증은 쿼리 파라미터로 apiKey 전달 시 인증을 거부해야 한다")
    void testInboundAuthenticationRejectsQueryParamApiKey() {
        UUID channelId = UUID.randomUUID();
        IntegrationChannel channel = new IntegrationChannel();
        channel.setId(channelId);
        channel.setDirection("INBOUND");
        channel.setActive(true);
        channel.setConfigJson("{\"authType\":\"API_KEY\",\"apiKeyName\":\"X-API-KEY\",\"apiKeyValue\":\"validKey\"}");

        when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));

        // apiKeyParam(쿼리 파라미터)으로 전달된 경우 헤더가 없으므로 SecurityException 발생
        assertThrows(SecurityException.class, () ->
                inboundIntegrationService.processInboundData(channelId, "{}", null, null, "validKey")
        );
    }

    @Test
    @DisplayName("#182: DataMappingTransformer는 SpEL 내 T() 클래스 로딩 및 임의 명령 실행(RCE)을 원천 차단해야 한다")
    void testDataMappingTransformerBlocksSpelRce() {
        DataMappingTransformer transformer = new DataMappingTransformer();

        String payloadJson = "{\"cmd\":\"id\"}";
        String maliciousConfigStr = "{"
                + "\"mappings\": ["
                + "  {\"targetField\": \"result\", \"sourceExpression\": \"T(java.lang.Runtime).getRuntime().exec('calc')\"}"
                + "]"
                + "}";

        String result = transformer.transformPayload(payloadJson, maliciousConfigStr);
        assertThat(result).contains("\"result\":null");
        assertThat(result).doesNotContain("Process");
    }

    @Test
    @DisplayName("#182: DataMappingTransformer는 #this.getClass() 등 리플렉션 탐색을 차단해야 한다")
    void testDataMappingTransformerBlocksReflectionAccess() {
        DataMappingTransformer transformer = new DataMappingTransformer();

        String payloadJson = "{\"data\": [{\"name\":\"Alice\"}]}";
        String maliciousConfigStr = "{"
                + "\"rootPath\": \"payload['data']\","
                + "\"mappings\": ["
                + "  {\"targetField\": \"cls\", \"sourceExpression\": \"#this.getClass().getName()\"}"
                + "]"
                + "}";

        String result = transformer.transformPayload(payloadJson, maliciousConfigStr);
        assertThat(result).contains("\"cls\":null");
        assertThat(result).doesNotContain("java.util");
    }
}
