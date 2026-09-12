package com.classification.domain_system.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Check if current DB is PostgreSQL before running PostgreSQL-specific index creation
            String dbProductName = "";
            try {
                if (jdbcTemplate.getDataSource() != null) {
                    try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
                        dbProductName = conn.getMetaData().getDatabaseProductName();
                    }
                }
            } catch (Exception ignored) {}

            if (dbProductName.toLowerCase().contains("h2")) {
                log.info("Skipping PostgreSQL DDL initialization for H2 database.");
                return;
            }

            // GIN index on record.data column
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_record_data_gin ON record USING GIN (CAST(data AS jsonb))");
                log.info("Successfully ensured GIN index on record.data column for performance optimization.");
            } catch (Exception e) {
                log.warn("Failed to ensure GIN index on record.data: {}", e.getMessage());
            }

            // Ensure critical performance indexes on approval_request, approval_step, and dq_rule
            String[] performanceIndexes = {
                "CREATE INDEX IF NOT EXISTS idx_approval_request_target_status ON approval_request (target_id, status)",
                "CREATE INDEX IF NOT EXISTS idx_approval_request_status ON approval_request (status)",
                "CREATE INDEX IF NOT EXISTS idx_approval_request_requester ON approval_request (requester_id)",
                "CREATE INDEX IF NOT EXISTS idx_approval_request_created_at ON approval_request (created_at DESC)",
                "CREATE INDEX IF NOT EXISTS idx_approval_step_request_order ON approval_step (request_id, step_order ASC)",
                "CREATE INDEX IF NOT EXISTS idx_approval_step_assignee_status ON approval_step (assignee_id, status)",
                "CREATE INDEX IF NOT EXISTS idx_dq_rule_field_active ON dq_rule (field_definition_id, is_active)",
                "CREATE INDEX IF NOT EXISTS idx_dq_rule_domain_active ON dq_rule (domain_id, is_active)"
            };
            for (String indexSql : performanceIndexes) {
                try {
                    jdbcTemplate.execute(indexSql);
                } catch (Exception e) {
                    log.warn("Failed to ensure performance index: {}", e.getMessage());
                }
            }

            // Ensure SLA & escalation columns on approval_step table for legacy DB compatibility
            String[] slaColumns = {
                "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS is_escalated BOOLEAN NOT NULL DEFAULT FALSE",
                "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS sla_hours INTEGER DEFAULT 48",
                "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS sla_due_at TIMESTAMP",
                "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS escalated_from_user_id VARCHAR(100)",
                "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP"
            };
            for (String sql : slaColumns) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception ignored) {}
            }

            // Ensure ON DELETE CASCADE/SET NULL on tables referencing record(id)
            // CRITICAL: Only add constraint if it does NOT already exist! Never DROP & ADD existing constraints on startup to prevent table-level locks and deadlocks.
            String[][] fkDefinitions = {
                {"fkiyvb583s9upl4prtl1bho9kw1", "ALTER TABLE dq_violation ADD CONSTRAINT fkiyvb583s9upl4prtl1bho9kw1 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE"},
                {"fk4gi888ouu16rl8gikxod61v68", "ALTER TABLE record_secondary_node ADD CONSTRAINT fk4gi888ouu16rl8gikxod61v68 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE"},
                {"fksack7ykq36libnbl0tijbwvg8", "ALTER TABLE record_field_source ADD CONSTRAINT fksack7ykq36libnbl0tijbwvg8 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE"},
                {"fk4jhsvm9jbj308q2gy9x8bnw77", "ALTER TABLE match_candidate ADD CONSTRAINT fk4jhsvm9jbj308q2gy9x8bnw77 FOREIGN KEY (existing_record_id) REFERENCES record(id) ON DELETE CASCADE"},
                {"fkjnr7c768g2v41h8xuauxc8t3u", "ALTER TABLE integration_logs ADD CONSTRAINT fkjnr7c768g2v41h8xuauxc8t3u FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE SET NULL"},
                {"fk6iako2qqonoa9pvw3jdh8yjw8", "ALTER TABLE record ADD CONSTRAINT fk6iako2qqonoa9pvw3jdh8yjw8 FOREIGN KEY (merged_into_record_id) REFERENCES record(id) ON DELETE SET NULL"}
            };
            for (String[] def : fkDefinitions) {
                String conName = def[0];
                String addSql = def[1];
                try {
                    Integer count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM pg_constraint WHERE conname = ?", Integer.class, conName);
                    if (count == null || count == 0) {
                        jdbcTemplate.execute(addSql);
                        log.info("Ensured FK constraint: {}", conName);
                    }
                } catch (Exception ex) {
                    log.warn("Could not ensure FK constraint [{}]: {}", conName, ex.getMessage());
                }
            }

            // Ensure indexes on record foreign key columns for ultra-fast deletes and lookups
            String[] indexStatements = {
                "CREATE INDEX IF NOT EXISTS idx_record_history_record_id ON record_history (record_id)",
                "CREATE INDEX IF NOT EXISTS idx_dq_violation_record_id ON dq_violation (record_id)",
                "CREATE INDEX IF NOT EXISTS idx_record_field_source_record_id ON record_field_source (record_id)",
                "CREATE INDEX IF NOT EXISTS idx_match_candidate_record_id ON match_candidate (existing_record_id)",
                "CREATE INDEX IF NOT EXISTS idx_match_candidate_domain_id ON match_candidate (domain_id)",
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_survivorship_domain_field_strategy ON survivorship_rule (domain_id, field_key, strategy)"
            };
            for (String sql : indexStatements) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception ex) {
                    log.warn("Could not ensure index [{}]: {}", sql, ex.getMessage());
                }
            }
            log.info("DatabaseInitializer completed successfully.");
        } catch (Exception e) {
            log.warn("DatabaseInitializer encountered an unexpected error: {}", e.getMessage());
        }
    }
}
