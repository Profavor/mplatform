package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalDelegationDto;
import com.classification.domain_system.entity.ApprovalDelegation;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.ApprovalDelegationRepository;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApprovalDelegationServiceTest {

    @Mock
    private ApprovalDelegationRepository delegationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApprovalDelegationService delegationService;

    private User delegator;
    private User delegatee;

    @BeforeEach
    void setUp() {
        delegator = new User();
        delegator.setId("user-delegator");
        delegator.setUsername("김위임");

        delegatee = new User();
        delegatee.setId("user-delegatee");
        delegatee.setUsername("이대결");
    }

    @Test
    @DisplayName("createDelegation: 정상적으로 결재 위임 등록 성공")
    void testCreateDelegationSuccess() {
        when(userRepository.findById("user-delegatee")).thenReturn(Optional.of(delegatee));
        when(userRepository.findById("user-delegator")).thenReturn(Optional.of(delegator));

        ApprovalDelegation saved = new ApprovalDelegation();
        saved.setId(UUID.randomUUID());
        saved.setDelegatorUserId("user-delegator");
        saved.setDelegateeUserId("user-delegatee");
        saved.setStartDate(LocalDateTime.now().minusDays(1));
        saved.setEndDate(LocalDateTime.now().plusDays(5));
        saved.setReason("휴가로 인한 대결 지정");
        saved.setIsActive(true);

        when(delegationRepository.save(any(ApprovalDelegation.class))).thenReturn(saved);

        ApprovalDelegationDto dto = ApprovalDelegationDto.builder()
                .delegatorUserId("user-delegator")
                .delegateeUserId("user-delegatee")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .reason("휴가로 인한 대결 지정")
                .build();

        ApprovalDelegationDto result = delegationService.createDelegation(dto, "user-delegator");

        assertThat(result).isNotNull();
        assertThat(result.getDelegatorUserId()).isEqualTo("user-delegator");
        assertThat(result.getDelegateeUserId()).isEqualTo("user-delegatee");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("createDelegation: 본인에게 위임 시도 시 예외 발생")
    void testCreateDelegationSelfError() {
        ApprovalDelegationDto dto = ApprovalDelegationDto.builder()
                .delegatorUserId("user-delegator")
                .delegateeUserId("user-delegator")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> delegationService.createDelegation(dto, "user-delegator"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delegate approvals to yourself");
    }

    @Test
    @DisplayName("isDelegatedApprover: 유효 기간 내 대결자 조회 시 true 반환")
    void testIsDelegatedApprover() {
        ApprovalDelegation activePair = new ApprovalDelegation();
        activePair.setDelegatorUserId("user-delegator");
        activePair.setDelegateeUserId("user-delegatee");
        activePair.setIsActive(true);

        when(delegationRepository.findActiveDelegationPair(eq("user-delegator"), eq("user-delegatee"), any(LocalDateTime.class)))
                .thenReturn(List.of(activePair));

        boolean isDelegated = delegationService.isDelegatedApprover("user-delegatee", "user-delegator");
        assertThat(isDelegated).isTrue();

        boolean notDelegated = delegationService.isDelegatedApprover("other-user", "user-delegator");
        assertThat(notDelegated).isFalse();
    }

    @Test
    @DisplayName("revokeDelegation: 위임 해제 시 isActive=false 처리")
    void testRevokeDelegation() {
        UUID id = UUID.randomUUID();
        ApprovalDelegation d = new ApprovalDelegation();
        d.setId(id);
        d.setDelegatorUserId("user-delegator");
        d.setIsActive(true);

        when(delegationRepository.findById(id)).thenReturn(Optional.of(d));

        delegationService.revokeDelegation(id, "user-delegator");

        assertThat(d.getIsActive()).isFalse();
        verify(delegationRepository).save(d);
    }
}
