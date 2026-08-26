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
    private String refreshToken;
    private String type;
    private UserResponse user;
    private String userType; // 'PARENT' or 'CHILD' - for mobile app routing
    private Long childProfileId; // ID of child profile if user is a child

    public static AuthenticationResponse of(String token, UserResponse user) {
        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(token)
                .type("Bearer")
                .user(user)
                .userType(determineUserType(user))
                .build();
    }

    private static String determineUserType(UserResponse user) {
        if (user.getRoles() != null && user.getRoles().contains("ROLE_CHILD")) {
            return "CHILD";
        }
        return "PARENT";
    }
}
