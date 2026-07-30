package com.classification.domain_system.service;

import com.classification.domain_system.dto.AdminUserUpdateDto;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.UserOrgHistory;
import com.classification.domain_system.repository.UserOrgHistoryRepository;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceOrgHistoryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOrgHistoryRepository userOrgHistoryRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("updateAdminUserInfo - 조직/부서/팀 변경 시 UserOrgHistory가 자동 저장된다")
    void updateAdminUserInfo_LogsUserOrgHistory_WhenOrgChanges() {
        String userId = "user-123";
        UUID oldOrgId = UUID.randomUUID();
        UUID oldDeptId = UUID.randomUUID();
        UUID newOrgId = UUID.randomUUID();
        UUID newDeptId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setOrganizationId(oldOrgId);
        existingUser.setDepartmentId(oldDeptId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserUpdateDto dto = new AdminUserUpdateDto();
        dto.setOrganizationId(newOrgId);
        dto.setDepartmentId(newDeptId);

        userService.updateAdminUserInfo(userId, dto);

        ArgumentCaptor<UserOrgHistory> captor = ArgumentCaptor.forClass(UserOrgHistory.class);
        verify(userOrgHistoryRepository).save(captor.capture());

        UserOrgHistory savedHistory = captor.getValue();
        assertThat(savedHistory.getUserId()).isEqualTo(userId);
        assertThat(savedHistory.getPrevOrganizationId()).isEqualTo(oldOrgId);
        assertThat(savedHistory.getPrevDepartmentId()).isEqualTo(oldDeptId);
        assertThat(savedHistory.getNewOrganizationId()).isEqualTo(newOrgId);
        assertThat(savedHistory.getNewDepartmentId()).isEqualTo(newDeptId);
    }
}
