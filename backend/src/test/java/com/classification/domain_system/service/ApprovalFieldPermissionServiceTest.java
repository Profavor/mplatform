package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalFieldPermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApprovalFieldPermissionService permissionService;

    @Test
    @DisplayName("extractEditableFields - 대상 역할과 일치하는 수정 가능 필드 목록 추출")
    void extractEditableFields_MatchesRole() {
        WorkflowConfig config = new WorkflowConfig();
        config.setStepsConfig("{\"permissions\":[{\"targetType\":\"ROLE\",\"targetRole\":\"REVIEWER\",\"editableFields\":[\"name\",\"desc\"]}]}");

        List<String> fields = permissionService.extractEditableFields(config, "user1", "REVIEWER");

        assertNotNull(fields);
        assertEquals(List.of("name", "desc"), fields);
    }

    @Test
    @DisplayName("validateUserActionPermission - 허용되지 않은 행위 시 BusinessException 던짐")
    void validateUserActionPermission_NotAllowed_ThrowsException() {
        WorkflowConfig config = new WorkflowConfig();
        config.setStepsConfig("{\"permissions\":[{\"targetType\":\"ROLE\",\"targetRole\":\"REVIEWER\",\"allowedActions\":[\"READ\"]}]}");

        assertThrows(BusinessException.class, () -> 
            permissionService.validateUserActionPermission(config, "user1", "REVIEWER", "CREATE")
        );
    }

    @Test
    @DisplayName("extractHiddenFields - 숨김 필드 목록 추출")
    void extractHiddenFields_Success() {
        WorkflowConfig config = new WorkflowConfig();
        config.setStepsConfig("{\"permissions\":[{\"targetType\":\"ALL\",\"hiddenFields\":[\"salary\",\"ssn\"]}]}");

        List<String> fields = permissionService.extractHiddenFields(config, "user1", "USER");

        assertNotNull(fields);
        assertEquals(List.of("salary", "ssn"), fields);
    }
}
