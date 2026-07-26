package com.classification.domain_system.service;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DomainPermission;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.DomainPermissionRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.repository.DomainAccessRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainPermissionServiceTest {

    @Mock
    private DomainPermissionRepository permissionRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainAccessRequestRepository requestRepository;

    @InjectMocks
    private DomainPermissionService domainPermissionService;

    @Test
    @DisplayName("성공 - 사용자에게 도메인 권한을 부여한다")
    void grantPermission_success() {
        // given
        String userId = "user1";
        UUID domainId = UUID.randomUUID();
        
        given(permissionRepository.findByUserIdAndDomainId(userId, domainId)).willReturn(Optional.empty());
        
        User user = new User();
        user.setId(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        
        Domain domain = new Domain();
        domain.setId(domainId);
        given(domainRepository.findById(domainId)).willReturn(Optional.of(domain));

        // when
        domainPermissionService.grantPermission(userId, domainId);

        // then
        verify(permissionRepository).save(any(DomainPermission.class));
    }

    @Test
    @DisplayName("성공 - 신청 중인 권한 요청을 취소(개별 삭제)한다")
    void cancelRequest_success() {
        // given
        UUID requestId = UUID.randomUUID();
        String userId = "user1";
        com.classification.domain_system.entity.DomainAccessRequest request = new com.classification.domain_system.entity.DomainAccessRequest();
        request.setId(requestId);
        User user = new User();
        user.setId(userId);
        user.setUsername(userId);
        request.setUser(user);
        request.setStatus("PENDING");

        given(requestRepository.findById(requestId)).willReturn(Optional.of(request));

        // when
        domainPermissionService.cancelRequest(requestId, userId);

        // then
        verify(requestRepository).delete(request);
    }
}
