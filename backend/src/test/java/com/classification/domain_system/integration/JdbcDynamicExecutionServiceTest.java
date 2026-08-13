package com.classification.domain_system.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcDynamicExecutionServiceTest {

    private JdbcDynamicExecutionService service;

    @BeforeEach
    void setUp() {
        // We only need the objectMapper for validation tests, but RequiredArgsConstructor would normally want it.
        // It's instantiated inside the class itself though (private final ObjectMapper objectMapper = new ObjectMapper();)
        // Let's check constructor signature if any.
        // Wait, @RequiredArgsConstructor generates a constructor for `final` uninitialized fields.
        // `objectMapper` is initialized inline. If there are no other uninitialized final fields, the default constructor works.
        service = new JdbcDynamicExecutionService();
    }

    @Test
    void executeUpsert_InvalidTable_ThrowsException() {
        String configJson = "{\"url\":\"jdbc:test\",\"user\":\"test\",\"password\":\"test\",\"table\":\"invalid table name\"}";
        String payloadJson = "{\"col1\":\"value1\"}";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.executeUpsert(configJson, payloadJson);
        });
        assertTrue(ex.getMessage().contains("Invalid identifier"));
    }

    @Test
    void executeUpsert_InvalidColumn_ThrowsException() {
        String configJson = "{\"url\":\"jdbc:test\",\"user\":\"test\",\"password\":\"test\",\"table\":\"valid_table\"}";
        String payloadJson = "{\"col1; DROP TABLE users;\":\"value1\"}";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.executeUpsert(configJson, payloadJson);
        });
        assertTrue(ex.getMessage().contains("Invalid identifier"));
    }

    @Test
    void testBuildMySqlUpsert() throws Exception {
        Method method = JdbcDynamicExecutionService.class.getDeclaredMethod("buildMySqlUpsert", String.class, List.class);
        method.setAccessible(true);
        String sql = (String) method.invoke(service, "my_table", Arrays.asList("id", "name"));
        assertEquals("INSERT INTO `my_table` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `id`=VALUES(`id`), `name`=VALUES(`name`)", sql);
    }

    @Test
    void testBuildPostgresUpsert() throws Exception {
        Method method = JdbcDynamicExecutionService.class.getDeclaredMethod("buildPostgresUpsert", String.class, List.class, List.class);
        method.setAccessible(true);
        String sql = (String) method.invoke(service, "my_table", Arrays.asList("id", "name", "age"), Arrays.asList("id"));
        assertEquals("INSERT INTO \"my_table\" (\"id\", \"name\", \"age\") VALUES (?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET \"name\" = EXCLUDED.\"name\", \"age\" = EXCLUDED.\"age\"", sql);
    }

    @Test
    void testBuildOracleMerge() throws Exception {
        Method method = JdbcDynamicExecutionService.class.getDeclaredMethod("buildOracleMerge", String.class, List.class, List.class);
        method.setAccessible(true);
        String sql = (String) method.invoke(service, "my_table", Arrays.asList("id", "name", "age"), Arrays.asList("id"));
        assertEquals("MERGE INTO \"my_table\" t USING (SELECT ? AS \"id\", ? AS \"name\", ? AS \"age\" FROM DUAL) s ON (t.\"id\" = s.\"id\") WHEN MATCHED THEN UPDATE SET t.\"name\" = s.\"name\", t.\"age\" = s.\"age\" WHEN NOT MATCHED THEN INSERT (\"id\", \"name\", \"age\") VALUES (s.\"id\", s.\"name\", s.\"age\")", sql);
    }
}
