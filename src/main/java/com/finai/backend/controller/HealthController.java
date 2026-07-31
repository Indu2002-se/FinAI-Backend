package com.finai.backend.controller;

import com.finai.backend.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides endpoints to verify application status
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the application is running")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("profile", activeProfile);
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("Application is healthy", health));
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Simple ping endpoint")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}
