package com.finai.backend.controller;

import com.finai.backend.dto.request.UserProfileRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.UserProfileResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.UserProfileService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile and onboarding management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserProfileService userProfileService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        User user = securityUtils.getCurrentUser();
        UserProfileResponse response = userProfileService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserProfileRequest request) {
        User user = securityUtils.getCurrentUser();
        UserProfileResponse response = userProfileService.updateProfile(request, user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/onboarding")
    @Operation(summary = "Complete onboarding and mark profile as complete")
    public ResponseEntity<ApiResponse<UserProfileResponse>> completeOnboarding(
            @Valid @RequestBody UserProfileRequest request) {
        User user = securityUtils.getCurrentUser();
        UserProfileResponse response = userProfileService.completeOnboarding(request, user);
        return ResponseEntity.ok(ApiResponse.success("Onboarding completed successfully", response));
    }
}
