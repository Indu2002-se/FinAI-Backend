package com.finai.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String type;
    private UserResponse user;

    public static AuthenticationResponse of(String token, UserResponse user) {
        return AuthenticationResponse.builder()
                .token(token)
                .type("Bearer")
                .user(user)
                .build();
    }
}
