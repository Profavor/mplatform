package com.classification.domain_system.config;

import com.classification.domain_system.service.RoleInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RoleDataMigrationInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RoleInitializer roleInitializer;

    @InjectMocks
    private RoleDataMigrationInitializer migrationInitializer;

    @Test
    @DisplayName("RoleDataMigrationInitializer 실행 시 존재하지 않는 menu.required_role 및 department.role 쿼리를 수행하지 않는다")
    void testMigrationDoesNotExecuteInvalidColumnQueries() throws Exception {
        // execute
        migrationInitializer.run();

        // verify valid updates are executed
        verify(jdbcTemplate, atLeastOnce()).update(anyString());

        // verify invalid column queries are NOT executed
        verify(jdbcTemplate, never()).update("UPDATE menu SET required_role = 'ROLE_ADMIN' WHERE required_role = 'ADMIN'");
        verify(jdbcTemplate, never()).update("UPDATE department SET role = 'ROLE_ADMIN' WHERE role = 'ADMIN'");

        // verify default role sync is called
        verify(roleInitializer).syncDefaultRolesForAllOrganizations();
    }
}
