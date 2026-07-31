package com.finai.backend.controller;

import com.finai.backend.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private HealthController healthController;

    @Test
    void healthCheck_shouldReturnOk() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = healthController.healthCheck();
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("UP", response.getBody().getData().get("status"));
    }

    @Test
    void ping_shouldReturnPong() {
        ResponseEntity<ApiResponse<String>> response = healthController.ping();
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("pong", response.getBody().getData());
    }
}

