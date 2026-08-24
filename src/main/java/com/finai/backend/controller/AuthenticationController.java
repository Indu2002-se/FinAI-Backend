package com.finai.backend.controller;

import com.finai.backend.dto.request.LoginRequest;
import com.finai.backend.dto.request.RegisterRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.AuthenticationResponse;
import com.finai.backend.dto.response.UserResponse;
import com.finai.backend.service.interfaces.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 * Handles user registration, login, and profile endpoints
 */
@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user", 
            description = "Get currently authenticated user profile",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout", 
            description = "Logout current user (JWT is stateless, client should discard token)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Since JWT is stateless, logout is handled on client side by discarding the token
        // This endpoint is provided for consistency with mobile app expectations
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
