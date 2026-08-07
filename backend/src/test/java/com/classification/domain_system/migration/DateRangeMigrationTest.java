package com.classification.domain_system.migration;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=none"})
public class DateRangeMigrationTest {

    static {
        // IntelliJ 등에서 실행할 때 환경 변수(.env)를 자동으로 불러와 시스템 프로퍼티로 주입합니다.
        try {
            List<String> lines = Files.readAllLines(Paths.get("../.env"));
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        System.setProperty(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load ../.env file: " + e.getMessage());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DateRangeMigrationTest.class);

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private FieldDefinitionRepository fieldDefinitionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.classification.domain_system.service.RecordService recordService;

    /**
     * One-off migration script to update DATE_RANGE delimiters from ',' to '~'.
     * Remove @Disabled to run this test and perform the migration, then put it back.
     */
    @Test
    @Disabled("One-off migration test. Remove @Disabled to execute.")
    @Transactional
    @Rollback(false)
    public void migrateDateRangeDelimiter() {
        log.info("Starting DATE_RANGE data migration...");

        // 1. Find all DATE_RANGE fields
        List<FieldDefinition> dateRangeFields = fieldDefinitionRepository.findAll().stream()
                .filter(f -> "DATE_RANGE".equals(f.getType()))
                .collect(Collectors.toList());

        if (dateRangeFields.isEmpty()) {
            log.info("No DATE_RANGE fields found. Skipping migration.");
            return;
        }

        List<String> dateRangeKeys = dateRangeFields.stream()
                .map(FieldDefinition::getKey)
                .collect(Collectors.toList());
        
        log.info("Found DATE_RANGE fields with keys: {}", dateRangeKeys);

        // 2. Iterate and update records
        List<Record> records = recordRepository.findAll();
        int updatedCount = 0;

        for (Record record : records) {
            if (record.getData() == null || record.getData().trim().isEmpty()) continue;

            boolean changed = false;
            try {
                Map<String, Object> dataMap = objectMapper.readValue(record.getData(), new TypeReference<Map<String, Object>>() {});
                
                for (String key : dateRangeKeys) {
                    if (dataMap.containsKey(key) && dataMap.get(key) != null) {
                        String val = dataMap.get(key).toString();
                        if (val.contains(",")) {
                            // Migrate from ',' to '~'
                            dataMap.put(key, val.replace(",", "~"));
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    String newData = objectMapper.writeValueAsString(dataMap);
                    
                    // Regenerate searchable_data based on the updated data
                    String newSearchable = record.getNode() != null ? 
                            recordService.generateSearchableData(record.getNode().getId(), newData) : null;

                    if (newSearchable != null) {
                        jdbcTemplate.update("UPDATE record SET data = cast(? as jsonb), searchable_data = cast(? as jsonb) WHERE id = ?", newData, newSearchable, record.getId());
                    } else {
                        jdbcTemplate.update("UPDATE record SET data = cast(? as jsonb) WHERE id = ?", newData, record.getId());
                    }
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to parse or update data for record " + record.getId(), e);
            }
        }

        log.info("DATE_RANGE data migration completed. Updated {} records.", updatedCount);
    }
}
