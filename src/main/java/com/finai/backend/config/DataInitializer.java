package com.finai.backend.config;

import com.finai.backend.entity.Role;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data initializer
 * Initializes default roles on application startup
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Initialize roles if not exists
            for (RoleType roleType : RoleType.values()) {
                if (roleRepository.findByName(roleType).isEmpty()) {
                    Role role = Role.builder()
                            .name(roleType)
                            .build();
                    roleRepository.save(role);
                    log.info("Created role: {}", roleType.name());
                }
            }
            log.info("Data initialization completed");
        };
    }
}
