package com.finai.backend.service.impl;

import com.finai.backend.dto.request.UserProfileRequest;
import com.finai.backend.dto.response.UserProfileResponse;
import com.finai.backend.entity.User;
import com.finai.backend.entity.UserProfile;
import com.finai.backend.repository.UserProfileRepository;
import com.finai.backend.repository.UserRepository;
import com.finai.backend.service.interfaces.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User user) {
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));
        return mapToResponse(profile, user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UserProfileRequest request, User user) {
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        updateProfileFields(profile, request, user);
        UserProfile saved = userProfileRepository.save(profile);
        return mapToResponse(saved, user);
    }

    @Override
    @Transactional
    public UserProfileResponse completeOnboarding(UserProfileRequest request, User user) {
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> createDefaultProfile(user));

        updateProfileFields(profile, request, user);
        UserProfile saved = userProfileRepository.save(profile);

        user.setProfileComplete(true);
        userRepository.save(user);

        return mapToResponse(saved, user);
    }

    private void updateProfileFields(UserProfile profile, UserProfileRequest request, User user) {
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getAge() != null) profile.setAge(request.getAge());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getEducation() != null) profile.setEducation(request.getEducation());
        if (request.getMaritalStatus() != null) profile.setMaritalStatus(request.getMaritalStatus());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getEmploymentStatus() != null) profile.setEmploymentStatus(request.getEmploymentStatus());
        if (request.getHouseholdSize() != null) profile.setHouseholdSize(request.getHouseholdSize());
        if (request.getDependentsCount() != null) profile.setDependentsCount(request.getDependentsCount());
        if (request.getMonthlyIncome() != null) profile.setMonthlyIncome(request.getMonthlyIncome());
        if (request.getMonthlyExpense() != null) profile.setMonthlyExpense(request.getMonthlyExpense());
        if (request.getSavingsGoal() != null) profile.setSavingsGoal(request.getSavingsGoal());
        if (request.getTotalDebt() != null) profile.setTotalDebt(request.getTotalDebt());
        if (request.getCreditScore() != null) profile.setCreditScore(request.getCreditScore());
        if (request.getPreferredCurrency() != null) profile.setPreferredCurrency(request.getPreferredCurrency());
        if (request.getFinancialKnowledgeLevel() != null) profile.setFinancialKnowledgeLevel(request.getFinancialKnowledgeLevel());
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile p = UserProfile.builder()
                .user(user)
                .householdSize(1)
                .dependentsCount(0)
                .creditScore(700)
                .preferredCurrency("LKR")
                .build();
        return userProfileRepository.save(p);
    }

    private UserProfileResponse mapToResponse(UserProfile p, User u) {
        return UserProfileResponse.builder()
                .id(p.getId())
                .userId(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .age(p.getAge())
                .gender(p.getGender())
                .education(p.getEducation())
                .maritalStatus(p.getMaritalStatus())
                .occupation(p.getOccupation())
                .employmentStatus(p.getEmploymentStatus())
                .householdSize(p.getHouseholdSize())
                .dependentsCount(p.getDependentsCount())
                .monthlyIncome(p.getMonthlyIncome())
                .monthlyExpense(p.getMonthlyExpense())
                .savingsGoal(p.getSavingsGoal())
                .totalDebt(p.getTotalDebt())
                .creditScore(p.getCreditScore())
                .preferredCurrency(p.getPreferredCurrency())
                .financialKnowledgeLevel(p.getFinancialKnowledgeLevel())
                .profileComplete(u.getProfileComplete())
                .build();
    }
}
