package edu.cit.camoro.peertayo.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class LegacyEvaluationDataMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    CommandLineRunner migrateLegacyEvaluationForms() {
        return args -> {
            if (!tableExists("evaluation_forms") || !tableExists("evaluations")) {
                return;
            }

            jdbcTemplate.update(
                    """
                    INSERT INTO evaluations (id, title, description, deadline, status, created_by, created_at)
                    SELECT
                        ef.id,
                        ef.title,
                        COALESCE(ef.description, ''),
                        ef.deadline,
                        COALESCE(NULLIF(TRIM(ef.status), ''), 'ACTIVE'),
                        ef.created_by,
                        COALESCE(ef.created_at, NOW())
                    FROM evaluation_forms ef
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM evaluations e
                        WHERE e.id = ef.id
                    )
                    """
            );

            jdbcTemplate.execute(
                    """
                    SELECT setval(
                        pg_get_serial_sequence('evaluations', 'id'),
                        GREATEST((SELECT COALESCE(MAX(id), 1) FROM evaluations), 1),
                        true
                    )
                    """
            );
        };
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name = ?
                """,
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }
}
