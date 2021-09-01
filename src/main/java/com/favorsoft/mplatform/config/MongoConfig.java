package com.favorsoft.mplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.favorsoft.mplatform.cdn.repository.mongo"})
public class MongoConfig {

}
