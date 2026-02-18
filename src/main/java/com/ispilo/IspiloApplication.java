package com.ispilo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
@EnableCaching
@EnableAsync
// @EnableJpaAuditing is already present in JpaConfig.java
public class IspiloApplication {

    private static final Logger logger = LoggerFactory.getLogger(IspiloApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IspiloApplication.class, args);
    }

    /**
     * Bean to test database connection on application startup
     */
    @Bean
    public CommandLineRunner checkDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                logger.info("✅ Database connection successful!");
                logger.info("✅ Connected to: {}", connection.getMetaData().getURL());
                logger.info("✅ Database: {}", connection.getCatalog());
            } catch (Exception e) {
                logger.error("❌ Database connection failed: {}", e.getMessage());
            }
        };
    }
}
