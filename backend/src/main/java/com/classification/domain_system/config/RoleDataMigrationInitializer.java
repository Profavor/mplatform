package com.classification.domain_system.config;

import com.classification.domain_system.service.RoleInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleDataMigrationInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final RoleInitializer roleInitializer;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting safety DB data migration: Updating legacy 'ADMIN' roles to 'ROLE_ADMIN'...");

            // 1. users 테이블의 role 'ADMIN' -> 'ROLE_ADMIN'
            int updatedUsers = jdbcTemplate.update(
                    "UPDATE users SET role = 'ROLE_ADMIN' WHERE role = 'ADMIN'"
            );
            if (updatedUsers > 0) {
                log.info("Migrated {} user(s) role from 'ADMIN' to 'ROLE_ADMIN'.", updatedUsers);
            }

            // 2. users 테이블의 role 'USER' -> 'ROLE_USER'
            int updatedUsersRoleUser = jdbcTemplate.update(
                    "UPDATE users SET role = 'ROLE_USER' WHERE role = 'USER'"
            );
            if (updatedUsersRoleUser > 0) {
                log.info("Migrated {} user(s) role from 'USER' to 'ROLE_USER'.", updatedUsersRoleUser);
            }

            // 3. role 테이블의 name 'ADMIN' -> 'ROLE_ADMIN' (중복 시 자식 role_permissions 삭제 후 role 삭제 및 업데이트)
            jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM role WHERE name = 'ADMIN' AND organization_id IN (SELECT organization_id FROM role WHERE name = 'ROLE_ADMIN'))");
            jdbcTemplate.update("DELETE FROM role WHERE name = 'ADMIN' AND organization_id IN (SELECT organization_id FROM role WHERE name = 'ROLE_ADMIN')");
            int updatedRoles = jdbcTemplate.update(
                    "UPDATE role SET name = 'ROLE_ADMIN' WHERE name = 'ADMIN'"
            );
            if (updatedRoles > 0) {
                log.info("Migrated {} role(s) name from 'ADMIN' to 'ROLE_ADMIN'.", updatedRoles);
            }


            // 4-1. INTGRATION / INTGRATION_MANAGER -> INTEGRATION 오타 DB 통합 마이그레이션
            jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM role WHERE name IN ('INTGRATION', 'INTEGRATION_MANAGER') AND organization_id IN (SELECT organization_id FROM role WHERE name = 'INTEGRATION'))");
            jdbcTemplate.update("DELETE FROM role WHERE name IN ('INTGRATION', 'INTEGRATION_MANAGER') AND organization_id IN (SELECT organization_id FROM role WHERE name = 'INTEGRATION')");
            jdbcTemplate.update("UPDATE role SET name = 'INTEGRATION' WHERE name IN ('INTGRATION', 'INTEGRATION_MANAGER')");
            jdbcTemplate.update("UPDATE users SET role = 'INTEGRATION' WHERE role IN ('INTGRATION', 'INTEGRATION_MANAGER')");
            jdbcTemplate.update("UPDATE department_roles SET role_name = 'INTEGRATION' WHERE role_name IN ('INTGRATION', 'INTEGRATION_MANAGER')");
            jdbcTemplate.update("UPDATE menu_roles SET role_name = 'INTEGRATION' WHERE role_name IN ('INTGRATION', 'INTEGRATION_MANAGER')");

            // 5. 기본 시스템 역할들의 is_system_role 및 display_name 보정
            jdbcTemplate.update("UPDATE role SET is_system_role = true WHERE name IN ('ADMIN', 'ROLE_ADMIN', 'ORG_ADMIN', 'DATA_STEWARD', 'DOMAIN_EDITOR', 'DQ_MANAGER', 'INTEGRATION', 'INTEGRATION_MANAGER', 'WORKFLOW', 'VIEWER', 'USER', 'ROLE_USER')");
            jdbcTemplate.update("UPDATE role SET display_name = '{\"ko\":\"시스템 관리자\",\"en\":\"System Admin\"}' WHERE name IN ('ADMIN', 'ROLE_ADMIN') AND (display_name IS NULL OR display_name = '')");
            jdbcTemplate.update("UPDATE role SET display_name = '{\"ko\":\"조직 관리자\",\"en\":\"Organization Admin\"}' WHERE name = 'ORG_ADMIN' AND (display_name IS NULL OR display_name = '')");
            jdbcTemplate.update("UPDATE role SET display_name = '{\"ko\":\"연계 관리자\",\"en\":\"Integration Manager\"}' WHERE name IN ('INTEGRATION', 'INTEGRATION_MANAGER') AND (display_name IS NULL OR display_name = '')");

            // 5-1. INTEGRATION 역할 퍼미션 (org:read, field:read, user:read, integration:*) 보정
            jdbcTemplate.update("INSERT INTO role_permissions (role_id, permission) SELECT id, 'org:read' FROM role WHERE name = 'INTEGRATION' AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = role.id AND rp.permission = 'org:read')");
            jdbcTemplate.update("INSERT INTO role_permissions (role_id, permission) SELECT id, 'field:read' FROM role WHERE name = 'INTEGRATION' AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = role.id AND rp.permission = 'field:read')");
            jdbcTemplate.update("INSERT INTO role_permissions (role_id, permission) SELECT id, 'user:read' FROM role WHERE name = 'INTEGRATION' AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = role.id AND rp.permission = 'user:read')");

            // 6. department_roles 1NF 테이블: 'ADMIN' -> 'ROLE_ADMIN'
            int updatedDeptRoles = jdbcTemplate.update(
                    "UPDATE department_roles SET role_name = 'ROLE_ADMIN' WHERE role_name = 'ADMIN'"
            );
            if (updatedDeptRoles > 0) {
                log.info("Migrated {} department_roles row(s) from 'ADMIN' to 'ROLE_ADMIN'.", updatedDeptRoles);
            }


            // 8. menu_roles 1NF 테이블: 'ADMIN' -> 'ROLE_ADMIN'
            int updatedMenuRoles = jdbcTemplate.update(
                    "UPDATE menu_roles SET role_name = 'ROLE_ADMIN' WHERE role_name = 'ADMIN'"
            );
            if (updatedMenuRoles > 0) {
                log.info("Migrated {} menu_roles row(s) from 'ADMIN' to 'ROLE_ADMIN'.", updatedMenuRoles);
            }

            // 9. ROLE_USER, USER, VIEWER 역할에 record:read 권한 보정
            int addedRecordReadPerms = jdbcTemplate.update(
                    "INSERT INTO role_permissions (role_id, permission) " +
                    "SELECT r.id, 'record:read' FROM role r " +
                    "WHERE r.name IN ('ROLE_USER', 'USER', 'VIEWER') " +
                    "AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission = 'record:read')"
            );
            if (addedRecordReadPerms > 0) {
                log.info("Migrated {} 'record:read' permission(s) to USER and VIEWER roles.", addedRecordReadPerms);
            }

            // 10. 모든 조직 대상 8대 기본 역할 및 퍼미션 동기화
            if (roleInitializer != null) {
                roleInitializer.syncDefaultRolesForAllOrganizations();
            }

            log.info("Completed safety DB data migration for ROLE_ADMIN and ORG_ADMIN.");
        } catch (Exception e) {
            log.error("Error occurred during DB role data migration", e);
        }
    }
}

