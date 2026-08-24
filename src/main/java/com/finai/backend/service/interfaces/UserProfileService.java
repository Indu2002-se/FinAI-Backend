package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.UserProfileRequest;
import com.finai.backend.dto.response.UserProfileResponse;
import com.finai.backend.entity.User;

public interface UserProfileService {
    UserProfileResponse getProfile(User user);
    UserProfileResponse updateProfile(UserProfileRequest request, User user);
    UserProfileResponse completeOnboarding(UserProfileRequest request, User user);
}
