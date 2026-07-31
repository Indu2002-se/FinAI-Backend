package com.finai.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * Enables JPA auditing and transaction management
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.finai.backend.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration
}
