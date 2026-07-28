package com.classification.domain_system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
public class H2DatabaseConfigurationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("H2 DataSource 연결 및 드라이버 설정 검증")
    void testH2DataSourceConfiguration() throws SQLException {
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            String driverName = connection.getMetaData().getDriverName();
            String url = connection.getMetaData().getURL();

            assertThat(driverName).containsIgnoringCase("H2");
            assertThat(url).containsIgnoringCase("jdbc:h2");
        }
    }
}
