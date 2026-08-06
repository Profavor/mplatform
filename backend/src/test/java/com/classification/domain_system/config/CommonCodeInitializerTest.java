package com.classification.domain_system.config;

import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.repository.CodeDetailRepository;
import com.classification.domain_system.repository.CodeGroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonCodeInitializerTest {

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CodeDetailRepository codeDetailRepository;

    private ObjectMapper objectMapper;

    private CommonCodeInitializer initializer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        initializer = new CommonCodeInitializer(codeGroupRepository, codeDetailRepository, objectMapper);
    }

    @Test
    @DisplayName("DB에 공통코드가 비어있으면 초기화를 수행한다")
    void initCommonCodes_whenEmpty_shouldInitialize() {
        // given
        when(codeGroupRepository.count()).thenReturn(0L);
        when(codeGroupRepository.save(any(CodeGroup.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        initializer.initCommonCodes();

        // then
        verify(codeGroupRepository, atLeastOnce()).save(any(CodeGroup.class));
        verify(codeDetailRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    @DisplayName("DB에 공통코드가 이미 존재하면 초기화를 건너뛴다")
    void initCommonCodes_whenNotEmpty_shouldSkip() {
        // given
        when(codeGroupRepository.count()).thenReturn(10L);

        // when
        initializer.initCommonCodes();

        // then
        verify(codeGroupRepository, never()).save(any(CodeGroup.class));
        verify(codeDetailRepository, never()).saveAll(anyList());
    }
}
