package com.ispilo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionChecker.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                logger.info("==================================================================");
                logger.info("✅ SUCCESS: Database is connected successfully!");
                logger.info("✅ Database URL: {}", connection.getMetaData().getURL());
                logger.info("==================================================================");
            } else {
                logger.error("==================================================================");
                logger.error("❌ FAILURE: Database connection is not valid.");
                logger.error("==================================================================");
            }
        } catch (Exception e) {
            logger.error("==================================================================");
            logger.error("❌ ERROR: Failed to connect to the database!");
            logger.error("❌ Exception: {}", e.getMessage());
            logger.error("==================================================================");
        }
    }
}
