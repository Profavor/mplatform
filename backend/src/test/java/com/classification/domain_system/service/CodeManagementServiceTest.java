package com.classification.domain_system.service;

import com.classification.domain_system.dto.CodeDetailRequest;
import com.classification.domain_system.dto.CodeGroupRequest;
import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.repository.CodeDetailRepository;
import com.classification.domain_system.repository.CodeGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeManagementServiceTest {

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CodeDetailRepository codeDetailRepository;

    @InjectMocks
    private CodeManagementService codeManagementService;

    private CodeGroup codeGroup;
    private UUID groupId;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();
        codeGroup = new CodeGroup();
        codeGroup.setId(groupId);
        codeGroup.setGroupCode("TEST_GROUP");
        codeGroup.setIsActive(true);
    }

    @Test
    void testCreateGroup() {
        CodeGroupRequest request = new CodeGroupRequest();
        request.setGroupCode("NEW_GROUP");
        when(codeGroupRepository.save(any(CodeGroup.class))).thenAnswer(i -> i.getArgument(0));

        CodeGroup result = codeManagementService.createGroup(request);
        assertEquals("NEW_GROUP", result.getGroupCode());
        assertTrue(result.getIsActive());
    }

    @Test
    void testGetActiveDetailsByGroupCode_FiltersCorrectly() {
        String groupCode = "TEST_GROUP";
        
        CodeDetail activeValid = new CodeDetail();
        activeValid.setId(UUID.randomUUID());
        activeValid.setIsActive(true);
        activeValid.setValidFrom(LocalDate.now().minusDays(1));
        activeValid.setValidTo(LocalDate.now().plusDays(1));

        CodeDetail inactive = new CodeDetail();
        inactive.setId(UUID.randomUUID());
        inactive.setIsActive(false);

        CodeDetail expired = new CodeDetail();
        expired.setId(UUID.randomUUID());
        expired.setIsActive(true);
        expired.setValidTo(LocalDate.now().minusDays(1));

        when(codeDetailRepository.findByCodeGroupGroupCode(groupCode))
                .thenReturn(Arrays.asList(activeValid, inactive, expired));

        List<CodeDetail> result = codeManagementService.getActiveDetailsByGroupCode(groupCode);

        assertEquals(1, result.size());
        assertEquals(activeValid.getId(), result.get(0).getId());
    }

    @Test
    void testGetGroupsPaged() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        List<CodeGroup> groups = Arrays.asList(codeGroup);
        org.springframework.data.domain.Page<CodeGroup> pagedResponse = new org.springframework.data.domain.PageImpl<>(groups);
        
        when(codeGroupRepository.findAll(pageable)).thenReturn(pagedResponse);

        org.springframework.data.domain.Page<CodeGroup> result = codeManagementService.getGroupsPaged(null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(codeGroup.getId(), result.getContent().get(0).getId());
    }
}
