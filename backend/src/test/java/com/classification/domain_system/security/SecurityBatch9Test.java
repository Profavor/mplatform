package com.classification.domain_system.security;

import com.classification.domain_system.config.SecurityConfig;
import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ChatMessageRoom;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import com.classification.domain_system.repository.ChatMessageRoomMemberRepository;
import com.classification.domain_system.repository.ChatMessageRepository;
import com.classification.domain_system.repository.ChatMessageRoomRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.ChatMessageService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.evaluators.UniqueEvaluator;
import com.classification.domain_system.service.storage.FileValidationUtil;
import com.classification.domain_system.service.storage.LocalStorageService;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityBatch9Test {

    @Mock private FileValidationUtil fileValidationUtil;
    @Mock private JwtUtil jwtUtil;
    @Mock private PermissionService permissionService;
    @Mock private AuthContext authContext;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private Environment environment;

    // For ChatMessageService test
    @Mock private ChatMessageRoomRepository roomRepository;
    @Mock private ChatMessageRepository messageRepository;
    @Mock private ChatMessageRoomMemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("#185: LocalStorageService는 절대경로(/etc/passwd) 및 .. 상위 디렉터리 탈출 시도를 완벽히 차단해야 한다")
    void testLocalStorageServicePathTraversalBlocked() throws Exception {
        Path tempDir = Files.createTempDirectory("storage_test");
        LocalStorageService storageService = new LocalStorageService(tempDir.toString(), fileValidationUtil);

        // 1. 절대경로 접근 차단 검증
        assertThrows(BusinessException.class, () -> storageService.loadFileAsResource("/etc/passwd"));
        assertThrows(BusinessException.class, () -> storageService.deleteFile("/etc/passwd"));

        // 2. 상위 경로 탈출 (..) 차단 검증
        assertThrows(BusinessException.class, () -> storageService.loadFileAsResource("../../etc/shadow"));
        assertThrows(BusinessException.class, () -> storageService.deleteFile("../secret.key"));
    }

    @Test
    @DisplayName("#185: JwtFilter는 쿼리스트링 token (?token=...) 으로 전달된 JWT를 인증하지 않아야 한다")
    void testJwtFilterQueryParamTokenIgnored() throws Exception {
        JwtFilter jwtFilter = JwtFilter.createForTest(jwtUtil, permissionService, authContext, userRepository);

        org.springframework.mock.web.MockHttpServletRequest mockReq = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/data");
        mockReq.setParameter("token", "some-malicious-or-leaked-jwt");
        org.springframework.mock.web.MockHttpServletResponse mockResp = new org.springframework.mock.web.MockHttpServletResponse();

        jwtFilter.doFilter(mockReq, mockResp, filterChain);

        // SecurityContext에 인증 정보가 설정되지 않아야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(mockReq, mockResp);
    }

    @Test
    @DisplayName("#190: UniqueEvaluator는 SQL Injection 공격 형태의 필드키(fieldKey)를 거부해야 한다")
    void testUniqueEvaluatorFieldKeySqlInjectionBlocked() {
        UniqueEvaluator evaluator = new UniqueEvaluator(jdbcTemplate, environment);

        FieldDefinition maliciousField = new FieldDefinition();
        maliciousField.setKey("name' OR '1'='1");

        DqRule rule = new DqRule();
        EvaluationContext context = new EvaluationContext(UUID.randomUUID(), null, null);

        assertThrows(IllegalArgumentException.class, () -> {
            evaluator.evaluate(maliciousField, rule, new TextNode("testValue"), context);
        });
    }

    @Test
    @DisplayName("#187: ChatMessageService는 대화방 멤버가 아닌 사용자의 메시지 전송을 차단해야 한다")
    void testChatMessageServiceNonMemberCannotSendMessage() {
        ChatMessageService service = new ChatMessageService(
                roomRepository, memberRepository, messageRepository, userRepository,
                null, null
        );

        UUID roomId = UUID.randomUUID();
        ChatMessageRoom room = new ChatMessageRoom();
        room.setId(roomId);
        lenient().when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // 멤버가 아닌 사용자로 조회될 때
        when(memberRepository.findByRoomIdAndUserId(roomId, "attacker-user")).thenReturn(Optional.empty());

        assertThrows(CustomAccessDeniedException.class, () -> {
            service.sendMessage(roomId, "attacker-user", "TEXT", "Hello", null, null, null);
        });
    }

    @Test
    @DisplayName("#184: SecurityConfig CORS 설정은 credentials 허용 시 와일드카드(*) Origin을 포함하지 않아야 한다")
    void testCorsConfigurationDisallowsWildcardWithCredentials() {
        SecurityConfig config = new SecurityConfig(null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(
                config, "allowedOrigins", "https://mdm.mplat.store,http://localhost:3000"
        );

        CorsConfigurationSource source = config.corsConfigurationSource();
        org.springframework.mock.web.MockHttpServletRequest mockReq = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/records");

        CorsConfiguration corsConfig = source.getCorsConfiguration(mockReq);
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowCredentials()).isTrue();
        assertThat(corsConfig.getAllowedOriginPatterns()).doesNotContain("*");
        assertThat(corsConfig.getAllowedOriginPatterns()).containsExactlyInAnyOrder("https://mdm.mplat.store", "http://localhost:3000");
    }
}
