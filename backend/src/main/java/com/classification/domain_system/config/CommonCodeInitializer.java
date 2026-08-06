package com.classification.domain_system.config;

import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.repository.CodeDetailRepository;
import com.classification.domain_system.repository.CodeGroupRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonCodeInitializer {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeDetailRepository codeDetailRepository;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initCommonCodes() {
        try {
            if (codeGroupRepository.count() > 0) {
                log.info("Common code data already exists. Skipping initialization.");
                return;
            }

            log.info("No common code data found. Initializing from default_codes.json...");

            List<CommonCodeSeedDto> seedData = loadDefaultCodes();
            if (seedData == null || seedData.isEmpty()) {
                log.warn("No default codes found in default_codes.json!");
                return;
            }

            int groupCount = 0;
            int detailCount = 0;

            for (CommonCodeSeedDto dto : seedData) {
                CodeGroup group = new CodeGroup();
                group.setGroupCode(dto.getGroupCode());
                group.setName(dto.getName());
                group.setDescription(dto.getDescription());
                group.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
                
                group = codeGroupRepository.save(group);
                groupCount++;

                if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
                    List<CodeDetail> detailEntities = new ArrayList<>();
                    for (CommonCodeDetailSeedDto detailDto : dto.getDetails()) {
                        CodeDetail detail = new CodeDetail();
                        detail.setCodeGroup(group);
                        detail.setDetailCode(detailDto.getDetailCode());
                        detail.setName(detailDto.getName());
                        detail.setSortOrder(detailDto.getSortOrder() != null ? detailDto.getSortOrder() : 0);
                        detail.setIsActive(detailDto.getIsActive() != null ? detailDto.getIsActive() : true);
                        detailEntities.add(detail);
                        detailCount++;
                    }
                    codeDetailRepository.saveAll(detailEntities);
                }
            }

            log.info("Common code initialization completed successfully ({} groups, {} details created).", groupCount, detailCount);
        } catch (Exception e) {
            log.error("Failed to initialize common code data", e);
        }
    }

    private List<CommonCodeSeedDto> loadDefaultCodes() {
        try {
            InputStream is = null;
            String userDir = System.getProperty("user.dir");
            java.io.File localFile = java.nio.file.Paths.get(userDir, "src", "main", "resources", "default_codes.json").toFile();
            
            if (localFile.exists()) {
                is = new java.io.FileInputStream(localFile);
                log.info("Using local filesystem seed: {}", localFile.getAbsolutePath());
            } else {
                ClassPathResource resource = new ClassPathResource("default_codes.json");
                if (resource.exists()) {
                    is = resource.getInputStream();
                    log.info("Using classpath seed");
                }
            }
            
            if (is == null) {
                log.warn("default_codes.json not found in resources!");
                return null;
            }
            
            try (InputStream finalIs = is) {
                return objectMapper.readValue(finalIs, new TypeReference<List<CommonCodeSeedDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to load default_codes.json", e);
            return null;
        }
    }

    @Data
    public static class CommonCodeSeedDto {
        private String groupCode;
        private Map<String, String> name;
        private Map<String, String> description;
        private Boolean isActive;
        private List<CommonCodeDetailSeedDto> details;
    }

    @Data
    public static class CommonCodeDetailSeedDto {
        private String detailCode;
        private Map<String, String> name;
        private Integer sortOrder;
        private Boolean isActive;
    }
}
