package com.ispilo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class ResiliencyConfig {
    // Enables @Retryable across the application.
    // The system will now autonomously recover from transient DB locks and network drops.
}