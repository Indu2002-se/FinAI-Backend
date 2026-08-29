package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.LoginRequest;
import com.finai.backend.dto.request.RegisterRequest;
import com.finai.backend.dto.response.AuthenticationResponse;
import com.finai.backend.dto.response.UserResponse;

/**
 * Authentication service interface
 * Defines authentication operations
 */
public interface AuthenticationService {

    /**
     * Register a new user
     * @param request registration request
     * @return authentication response with token
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticate user login
     * @param request login request
     * @return authentication response with token
     */
    AuthenticationResponse login(LoginRequest request);

    /**
     * Verify a Firebase ID token and issue the application's JWT response.
     */
    AuthenticationResponse loginWithFirebaseIdToken(String idToken);

    /**
     * Get current authenticated user
     * @return user response
     */
    UserResponse getCurrentUser();
}
