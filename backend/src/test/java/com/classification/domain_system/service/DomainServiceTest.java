package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.DomainRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class DomainServiceTest extends BaseServiceTest {

    @Mock
    private DomainRepository domainRepository;
    
    @Mock
    private com.classification.domain_system.repository.UserRepository userRepository;

    @InjectMocks
    private DomainService domainService;

    @Nested
    @DisplayName("createDomain")
    class CreateDomain {

        @Test
        @DisplayName("성공 - 도메인을 정상적으로 생성한다")
        void success() {
            // given
            DomainRequest request = createTestDomainRequest("인사", "HR");
            Domain saved = createTestDomain(UUID.randomUUID(), "인사", "HR");
            given(domainRepository.save(any(Domain.class))).willReturn(saved);

            // when
            Domain result = domainService.createDomain(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName().get("ko")).isEqualTo("인사");
            verify(domainRepository).save(any(Domain.class));
        }

        @Test
        @DisplayName("성공 - autoDqScanEnabled 설정이 포함된 도메인을 정상 생성한다")
        void successWithAutoDqScanEnabled() {
            // given
            DomainRequest request = createTestDomainRequest("인사", "HR");
            request.setAutoDqScanEnabled(true);
            Domain saved = createTestDomain(UUID.randomUUID(), "인사", "HR");
            saved.setAutoDqScanEnabled(true);
            given(domainRepository.save(any(Domain.class))).willReturn(saved);

            // when
            Domain result = domainService.createDomain(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isAutoDqScanEnabled()).isTrue();
            verify(domainRepository).save(any(Domain.class));
        }
    }

    @Nested
    @DisplayName("getAllDomains")
    class GetAllDomains {

        @Test
        @DisplayName("성공 - 권한이 있는 경우에만, 또는 ADMIN인 경우 전체를 정렬하여 반환한다")
        void success() {
            // given
            List<Domain> domains = List.of(
                createTestDomain(UUID.randomUUID(), "인사", "HR"),
                createTestDomain(UUID.randomUUID(), "재무", "Finance")
            );
            
            org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
            org.springframework.security.core.context.SecurityContext ctx = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
            given(ctx.getAuthentication()).willReturn(auth);
            org.springframework.security.core.context.SecurityContextHolder.setContext(ctx);
            
            given(auth.isAuthenticated()).willReturn(true);
            given(auth.getName()).willReturn("adminUser");
            given(auth.getAuthorities()).willReturn((Collection) List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
            
            com.classification.domain_system.entity.User admin = new com.classification.domain_system.entity.User();
            admin.setId(UUID.randomUUID().toString());
            admin.setUsername("adminUser");
            admin.setRole("ROLE_ADMIN");
            
            given(userRepository.findByUsername("adminUser")).willReturn(Optional.of(admin));
            given(domainRepository.findAllByOrderBySortOrderAsc()).willReturn(domains);

            // when
            List<Domain> result = domainService.getAllDomains();

            // then
            assertThat(result).hasSize(2);
            verify(domainRepository).findAllByOrderBySortOrderAsc();
            
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("성공 - DATA_STEWARD,ADMIN 복수 권한 유저의 경우 전체 도메인을 반환한다")
        void successMultiRoleUser() {
            // given
            List<Domain> domains = List.of(
                createTestDomain(UUID.randomUUID(), "인사", "HR"),
                createTestDomain(UUID.randomUUID(), "재무", "Finance")
            );
            
            org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
            org.springframework.security.core.context.SecurityContext ctx = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
            given(ctx.getAuthentication()).willReturn(auth);
            org.springframework.security.core.context.SecurityContextHolder.setContext(ctx);
            
            given(auth.isAuthenticated()).willReturn(true);
            given(auth.getName()).willReturn("profavor");
            given(auth.getAuthorities()).willReturn((Collection) List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
            
            com.classification.domain_system.entity.User multiRoleUser = new com.classification.domain_system.entity.User();
            multiRoleUser.setId(UUID.randomUUID().toString());
            multiRoleUser.setUsername("profavor");
            multiRoleUser.setRole("ROLE_DATA_STEWARD,ROLE_ADMIN");
            
            given(userRepository.findByUsername("profavor")).willReturn(Optional.of(multiRoleUser));
            given(domainRepository.findAllByOrderBySortOrderAsc()).willReturn(domains);

            // when
            List<Domain> result = domainService.getAllDomains();

            // then
            assertThat(result).hasSize(2);
            verify(domainRepository).findAllByOrderBySortOrderAsc();
            
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Nested
    @DisplayName("getDomain")
    class GetDomain {

        @Test
        @DisplayName("성공 - ID로 도메인을 조회한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();
            Domain domain = createTestDomain(id, "인사", "HR");
            given(domainRepository.findById(id)).willReturn(Optional.of(domain));

            // when
            Domain result = domainService.getDomain(id);

            // then
            assertThat(result.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID로 조회 시 예외 발생")
        void failNotFound() {
            // given
            UUID id = UUID.randomUUID();
            given(domainRepository.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> domainService.getDomain(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Domain not found");
        }
    }

    @Nested
    @DisplayName("updateDomain")
    class UpdateDomain {

        @Test
        @DisplayName("성공 - 도메인을 정상적으로 수정한다")
        void success() {
            // given
            UUID id = UUID.randomUUID();
            Domain existing = createTestDomain(id, "인사", "HR");
            DomainRequest request = createTestDomainRequest("인사관리", "HR Management");
            given(domainRepository.findById(id)).willReturn(Optional.of(existing));
            given(domainRepository.save(any(Domain.class))).willReturn(existing);

            // when
            Domain result = domainService.updateDomain(id, request);

            // then
            assertThat(result).isNotNull();
            verify(domainRepository).save(any(Domain.class));
        }

        @Test
        @DisplayName("성공 - identifierFieldId 및 displayNameFieldId가 null인 초기 도메인도 정상 수정된다")
        void successWithoutFieldIds() {
            // given
            UUID id = UUID.randomUUID();
            Domain existing = createTestDomain(id, "인사", "HR");
            DomainRequest request = createTestDomainRequest("인사관리", "HR Management");
            request.setIdentifierFieldId(null);
            request.setDisplayNameFieldId(null);

            given(domainRepository.findById(id)).willReturn(Optional.of(existing));
            given(domainRepository.save(any(Domain.class))).willReturn(existing);

            // when
            Domain result = domainService.updateDomain(id, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getIdentifierFieldId()).isNull();
            assertThat(result.getDisplayNameFieldId()).isNull();
            verify(domainRepository).save(any(Domain.class));
        }
    }
}
