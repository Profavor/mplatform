package com.favorsoft.mplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories(basePackages = {"com.favorsoft.mplatform.cdn.repository.jdbc"})
public class JdbcConfig {

}
