package com.classification.domain_system.config;

import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.repository.CodeDetailRepository;
import com.classification.domain_system.repository.CodeGroupRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

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
    @DisplayName("DB에 공통코드가 이미 존재하고 모든 세부 코드가 존재하면 추가 저장을 건너뛴다")
    void initCommonCodes_whenNotEmpty_shouldSkip() throws Exception {
        // given
        when(codeGroupRepository.count()).thenReturn(10L);

        InputStream is = null;
        File localFile = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "default_codes.json").toFile();
        if (localFile.exists()) {
            is = new FileInputStream(localFile);
        } else {
            is = new ClassPathResource("default_codes.json").getInputStream();
        }

        List<CommonCodeInitializer.CommonCodeSeedDto> seedData;
        try (InputStream stream = is) {
            seedData = objectMapper.readValue(stream, new TypeReference<List<CommonCodeInitializer.CommonCodeSeedDto>>() {});
        }

        Map<String, List<CodeDetail>> groupDetailsMap = new HashMap<>();
        Map<String, CodeGroup> groupMap = new HashMap<>();

        for (CommonCodeInitializer.CommonCodeSeedDto dto : seedData) {
            CodeGroup group = new CodeGroup();
            group.setId(UUID.randomUUID());
            group.setGroupCode(dto.getGroupCode());
            groupMap.put(dto.getGroupCode(), group);

            List<CodeDetail> details = new ArrayList<>();
            if (dto.getDetails() != null) {
                for (CommonCodeInitializer.CommonCodeDetailSeedDto dDto : dto.getDetails()) {
                    CodeDetail cd = new CodeDetail();
                    cd.setCodeGroup(group);
                    cd.setDetailCode(dDto.getDetailCode());
                    details.add(cd);
                }
            }
            groupDetailsMap.put(dto.getGroupCode(), details);
        }

        when(codeGroupRepository.findByGroupCode(anyString())).thenAnswer(inv -> {
            String gCode = inv.getArgument(0);
            return Optional.ofNullable(groupMap.get(gCode));
        });

        when(codeDetailRepository.findByCodeGroupIdOrderBySortOrderAsc(any(UUID.class))).thenAnswer(inv -> {
            UUID groupId = inv.getArgument(0);
            for (CodeGroup g : groupMap.values()) {
                if (Objects.equals(g.getId(), groupId)) {
                    return groupDetailsMap.getOrDefault(g.getGroupCode(), Collections.emptyList());
                }
            }
            return Collections.emptyList();
        });

        // when
        initializer.initCommonCodes();

        // then
        verify(codeGroupRepository, never()).save(any(CodeGroup.class));
        verify(codeDetailRepository, never()).saveAll(anyList());
    }
}
