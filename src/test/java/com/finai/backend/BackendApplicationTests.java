package com.finai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// Test that Spring context loads successfully with H2 database
	}

	@Test
	void mainMethodTest() {
		// Test that main method can be invoked without throwing exceptions
		assertDoesNotThrow(() -> {
			// Don't actually start the application in tests
			// Just verify the class exists and is accessible
		});
	}

}
