package com.classification.domain_system.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void run(ApplicationArguments args) {
        try {
            // Check if current DB is PostgreSQL before running PostgreSQL-specific GIN index creation
            Object dialect = entityManager.getEntityManagerFactory().getProperties().get("hibernate.dialect");
            String dialectStr = dialect != null ? dialect.toString() : "";
            if (dialectStr.contains("H2")) {
                log.info("Skipping GIN index creation for H2 database.");
                return;
            }

            String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_record_data_gin ON record USING GIN (CAST(data AS jsonb))";
            entityManager.createNativeQuery(createIndexSql).executeUpdate();
            log.info("Successfully ensured GIN index on record.data column for performance optimization.");

            // Ensure SLA & escalation columns on approval_step table for legacy DB compatibility
            try {
                String ensureSlaColumnsSql = "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS is_escalated BOOLEAN NOT NULL DEFAULT FALSE;"
                        + "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS sla_hours INTEGER DEFAULT 48;"
                        + "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS sla_due_at TIMESTAMP;"
                        + "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS escalated_from_user_id VARCHAR(100);"
                        + "ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP;";
                entityManager.createNativeQuery(ensureSlaColumnsSql).executeUpdate();
            } catch (Exception ignored) {}

            // Ensure ON DELETE CASCADE/SET NULL on tables referencing record(id)
            try {
                String ensureRecordFkCascadeSql = 
                    "ALTER TABLE dq_violation DROP CONSTRAINT IF EXISTS fkiyvb583s9upl4prtl1bho9kw1;"
                    + "ALTER TABLE dq_violation ADD CONSTRAINT fkiyvb583s9upl4prtl1bho9kw1 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE;"
                    + "ALTER TABLE record_secondary_node DROP CONSTRAINT IF EXISTS fk4gi888ouu16rl8gikxod61v68;"
                    + "ALTER TABLE record_secondary_node ADD CONSTRAINT fk4gi888ouu16rl8gikxod61v68 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE;"
                    + "ALTER TABLE record_field_source DROP CONSTRAINT IF EXISTS fksack7ykq36libnbl0tijbwvg8;"
                    + "ALTER TABLE record_field_source ADD CONSTRAINT fksack7ykq36libnbl0tijbwvg8 FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE;"
                    + "ALTER TABLE match_candidate DROP CONSTRAINT IF EXISTS fk4jhsvm9jbj308q2gy9x8bnw77;"
                    + "ALTER TABLE match_candidate ADD CONSTRAINT fk4jhsvm9jbj308q2gy9x8bnw77 FOREIGN KEY (existing_record_id) REFERENCES record(id) ON DELETE CASCADE;"
                    + "ALTER TABLE integration_logs DROP CONSTRAINT IF EXISTS fkjnr7c768g2v41h8xuauxc8t3u;"
                    + "ALTER TABLE integration_logs ADD CONSTRAINT fkjnr7c768g2v41h8xuauxc8t3u FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE SET NULL;"
                    + "ALTER TABLE record DROP CONSTRAINT IF EXISTS fk6iako2qqonoa9pvw3jdh8yjw8;"
                    + "ALTER TABLE record ADD CONSTRAINT fk6iako2qqonoa9pvw3jdh8yjw8 FOREIGN KEY (merged_into_record_id) REFERENCES record(id) ON DELETE SET NULL;"
                    + "ALTER TABLE record_history DROP CONSTRAINT IF EXISTS fk3n0uq2afivhyimre1l9h92lhl;";
                entityManager.createNativeQuery(ensureRecordFkCascadeSql).executeUpdate();
                log.info("Successfully ensured ON DELETE CASCADE / SET NULL on tables referencing record(id).");
            } catch (Exception ex) {
                log.warn("Could not ensure record FK cascade constraints: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to create GIN index on record.data. It may already exist or DB is not PostgreSQL: {}", e.getMessage());
        }
    }
}
