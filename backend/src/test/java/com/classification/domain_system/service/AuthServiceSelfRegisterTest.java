package com.classification.domain_system.service;

import com.classification.domain_system.dto.DomainResponse;
import com.classification.domain_system.dto.SelfRegisterRequest;
import com.classification.domain_system.dto.SpecializedDomainProvisionRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DomainPermission;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.DomainPermissionRepository;
import com.classification.domain_system.repository.LoginLogRepository;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceSelfRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DomainPermissionRepository domainPermissionRepository;

    @Mock
    private SpecializedDomainTemplateService specializedDomainTemplateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginLogRepository loginLogRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("성공: 신규 조직, 사용자, 체험 도메인이 정상 생성되고 자동 로그인 토큰이 반환된다")
    void selfRegister_success() {
        // given
        SelfRegisterRequest request = SelfRegisterRequest.builder()
                .username("corp_admin")
                .email("admin@corp.com")
                .password("Password123!")
                .companyName("(주)알파데이터")
                .termsAgreed(true)
                .timezone("Asia/Seoul")
                .build();

        given(userRepository.findByUsername("corp_admin")).willReturn(Optional.empty());
        given(passwordEncoder.encode("Password123!")).willReturn("encodedPassword");

        UUID orgId = UUID.randomUUID();
        Organization savedOrg = new Organization();
        savedOrg.setId(orgId);
        savedOrg.setName("(주)알파데이터");
        given(organizationRepository.save(any(Organization.class))).willReturn(savedOrg);

        String userId = UUID.randomUUID().toString();
        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setUsername("corp_admin");
        savedUser.setEmail("admin@corp.com");
        savedUser.setOrganizationId(orgId);
        savedUser.setRole("ROLE_USER,ORG_ADMIN");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        UUID domainId = UUID.randomUUID();
        DomainResponse mockDomainResponse = DomainResponse.builder()
                .id(domainId)
                .name(Map.of("ko", "(주)알파데이터 고객 마스터"))
                .build();
        given(specializedDomainTemplateService.provisionDomainForOrganization(any(SpecializedDomainProvisionRequest.class), eq(orgId)))
                .willReturn(mockDomainResponse);

        given(jwtUtil.generateToken(eq("corp_admin"), eq("ROLE_USER,ORG_ADMIN"), eq(userId), any()))
                .willReturn("mock-access-token");
        given(jwtUtil.generateRefreshToken(eq("corp_admin"), eq("ROLE_USER,ORG_ADMIN"), eq(userId), any()))
                .willReturn("mock-refresh-token");

        // when
        Map<String, String> tokens = authService.selfRegister(request, "127.0.0.1", "TestAgent");

        // then
        assertThat(tokens).isNotNull();
        assertThat(tokens.get("token")).isEqualTo("mock-access-token");
        assertThat(tokens.get("refreshToken")).isEqualTo("mock-refresh-token");

        verify(organizationRepository).save(any(Organization.class));
        verify(userRepository).save(any(User.class));
        verify(specializedDomainTemplateService).provisionDomainForOrganization(any(SpecializedDomainProvisionRequest.class), eq(orgId));
        verify(domainPermissionRepository).save(any(DomainPermission.class));
    }

    @Test
    @DisplayName("실패: 이미 존재하는 아이디인 경우 USERNAME_ALREADY_EXISTS 예외가 발생한다")
    void selfRegister_fail_duplicateUsername() {
        // given
        SelfRegisterRequest request = SelfRegisterRequest.builder()
                .username("existing_user")
                .email("new@corp.com")
                .password("Password123!")
                .companyName("(주)알파데이터")
                .termsAgreed(true)
                .build();

        given(userRepository.findByUsername("existing_user")).willReturn(Optional.of(new User()));

        // when & then
        assertThatThrownBy(() -> authService.selfRegister(request, "127.0.0.1", "TestAgent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("실패: 이용약관 미동의 시 예외가 발생한다")
    void selfRegister_fail_termsNotAgreed() {
        // given
        SelfRegisterRequest request = SelfRegisterRequest.builder()
                .username("corp_admin")
                .email("admin@corp.com")
                .password("Password123!")
                .companyName("(주)알파데이터")
                .termsAgreed(false)
                .build();

        // when & then
        assertThatThrownBy(() -> authService.selfRegister(request, "127.0.0.1", "TestAgent"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("실패: 필수 정보(회사명, 아이디, 비밀번호) 누락 시 예외가 발생한다")
    void selfRegister_fail_blankFields() {
        // given
        SelfRegisterRequest request = SelfRegisterRequest.builder()
                .username("")
                .email("admin@corp.com")
                .password("Password123!")
                .companyName("")
                .termsAgreed(true)
                .build();

        // when & then
        assertThatThrownBy(() -> authService.selfRegister(request, "127.0.0.1", "TestAgent"))
                .isInstanceOf(BusinessException.class);
    }
}
