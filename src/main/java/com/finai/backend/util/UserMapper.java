package com.finai.backend.util;

import com.finai.backend.dto.response.UserResponse;
import com.finai.backend.entity.Role;
import com.finai.backend.entity.User;

import java.util.stream.Collectors;

/**
 * Utility class for mapping User entity to DTOs
 */
public class UserMapper {

    private UserMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Map User entity to UserResponse DTO
     */
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
