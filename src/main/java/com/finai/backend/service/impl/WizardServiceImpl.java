package com.finai.backend.service.impl;

import com.finai.backend.dto.request.WizardRequest;
import com.finai.backend.dto.response.WizardResponse;
import com.finai.backend.entity.User;
import com.finai.backend.entity.UserProfile;
import com.finai.backend.entity.WizardProfile;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.UserProfileRepository;
import com.finai.backend.repository.UserRepository;
import com.finai.backend.repository.WizardProfileRepository;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.service.interfaces.WizardService;
import com.finai.backend.util.WizardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wizard service implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WizardServiceImpl implements WizardService {

    private final WizardProfileRepository wizardProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AiService aiService;

    @Override
    @Transactional
    public WizardResponse saveWizard(WizardRequest request) {
        // Get current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if wizard profile already exists → if so, delegate to update
        if (wizardProfileRepository.existsByUserId(user.getId())) {
            return updateWizard(request);
        }

        // Validate monthly expense is not greater than monthly income
        if (request.getMonthlyExpense().compareTo(request.getMonthlyIncome()) > 0) {
            throw new BadRequestException("Monthly expense cannot be greater than monthly income");
        }

        // Create wizard profile
        WizardProfile wizardProfile = WizardProfile.builder()
                .user(user)
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyExpense(request.getMonthlyExpense())
                .savingsGoal(request.getSavingsGoal())
                .financialKnowledgeLevel(request.getFinancialKnowledgeLevel())
                .employmentStatus(request.getEmploymentStatus())
                .preferredCurrency(request.getPreferredCurrency().toUpperCase())
                .build();

        wizardProfile = wizardProfileRepository.save(wizardProfile);

        // Sync to UserProfile so AI features can use the baseline data
        syncUserProfile(user, request);

        // Mark user profile as complete
        user.setProfileComplete(true);
        userRepository.save(user);

        // Trigger AI analysis asynchronously (fire-and-forget, does not block response)
        triggerAiAnalysis(user);

        return WizardMapper.toWizardResponse(wizardProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public WizardResponse getWizard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WizardProfile wizardProfile = wizardProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wizard profile not found"));

        return WizardMapper.toWizardResponse(wizardProfile);
    }

    @Override
    @Transactional
    public WizardResponse updateWizard(WizardRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WizardProfile wizardProfile = wizardProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wizard profile not found"));

        // Validate monthly expense is not greater than monthly income
        if (request.getMonthlyExpense().compareTo(request.getMonthlyIncome()) > 0) {
            throw new BadRequestException("Monthly expense cannot be greater than monthly income");
        }

        wizardProfile.setMonthlyIncome(request.getMonthlyIncome());
        wizardProfile.setMonthlyExpense(request.getMonthlyExpense());
        wizardProfile.setSavingsGoal(request.getSavingsGoal());
        wizardProfile.setFinancialKnowledgeLevel(request.getFinancialKnowledgeLevel());
        wizardProfile.setEmploymentStatus(request.getEmploymentStatus());
        wizardProfile.setPreferredCurrency(request.getPreferredCurrency().toUpperCase());
        wizardProfile = wizardProfileRepository.save(wizardProfile);

        // Re-sync UserProfile
        syncUserProfile(user, request);

        // Ensure profile is marked complete
        if (!Boolean.TRUE.equals(user.getProfileComplete())) {
            user.setProfileComplete(true);
            userRepository.save(user);
        }

        // Re-trigger AI analysis
        triggerAiAnalysis(user);

        return WizardMapper.toWizardResponse(wizardProfile);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void syncUserProfile(User user, WizardRequest request) {
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .monthlyIncome(request.getMonthlyIncome())
                    .monthlyExpense(request.getMonthlyExpense())
                    .savingsGoal(request.getSavingsGoal())
                    .employmentStatus(request.getEmploymentStatus())
                    .financialKnowledgeLevel(request.getFinancialKnowledgeLevel())
                    .preferredCurrency(request.getPreferredCurrency().toUpperCase())
                    .build();
        } else {
            profile.setMonthlyIncome(request.getMonthlyIncome());
            profile.setMonthlyExpense(request.getMonthlyExpense());
            profile.setSavingsGoal(request.getSavingsGoal());
            profile.setEmploymentStatus(request.getEmploymentStatus());
            profile.setFinancialKnowledgeLevel(request.getFinancialKnowledgeLevel());
            profile.setPreferredCurrency(request.getPreferredCurrency().toUpperCase());
        }
        userProfileRepository.save(profile);
        log.info("UserProfile synced for user id: {}", user.getId());
    }

    private void triggerAiAnalysis(User user) {
        try {
            log.info("Triggering AI analysis for user id: {}", user.getId());
            aiService.runFullAnalysis(user);
        } catch (Exception e) {
            // Fire-and-forget: log and swallow so wizard response is never blocked
            log.warn("AI analysis trigger failed for user {}: {}", user.getId(), e.getMessage());
        }
    }
}
