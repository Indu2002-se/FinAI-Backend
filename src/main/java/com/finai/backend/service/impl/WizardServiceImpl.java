package com.finai.backend.service.impl;

import com.finai.backend.dto.request.WizardRequest;
import com.finai.backend.dto.response.WizardResponse;
import com.finai.backend.entity.User;
import com.finai.backend.entity.WizardProfile;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.UserRepository;
import com.finai.backend.repository.WizardProfileRepository;
import com.finai.backend.service.interfaces.WizardService;
import com.finai.backend.util.WizardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wizard service implementation
 */
@Service
@RequiredArgsConstructor
public class WizardServiceImpl implements WizardService {

    private final WizardProfileRepository wizardProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WizardResponse saveWizard(WizardRequest request) {
        // Get current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if wizard profile already exists
        if (wizardProfileRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Wizard profile already exists. Use update endpoint instead.");
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

        // Save wizard profile
        wizardProfile = wizardProfileRepository.save(wizardProfile);

        // Map to response
        return WizardMapper.toWizardResponse(wizardProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public WizardResponse getWizard() {
        // Get current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get wizard profile
        WizardProfile wizardProfile = wizardProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wizard profile not found"));

        // Map to response
        return WizardMapper.toWizardResponse(wizardProfile);
    }

    @Override
    @Transactional
    public WizardResponse updateWizard(WizardRequest request) {
        // Get current authenticated user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get existing wizard profile
        WizardProfile wizardProfile = wizardProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wizard profile not found"));

        // Validate monthly expense is not greater than monthly income
        if (request.getMonthlyExpense().compareTo(request.getMonthlyIncome()) > 0) {
            throw new BadRequestException("Monthly expense cannot be greater than monthly income");
        }

        // Update wizard profile
        wizardProfile.setMonthlyIncome(request.getMonthlyIncome());
        wizardProfile.setMonthlyExpense(request.getMonthlyExpense());
        wizardProfile.setSavingsGoal(request.getSavingsGoal());
        wizardProfile.setFinancialKnowledgeLevel(request.getFinancialKnowledgeLevel());
        wizardProfile.setEmploymentStatus(request.getEmploymentStatus());
        wizardProfile.setPreferredCurrency(request.getPreferredCurrency().toUpperCase());

        // Save wizard profile
        wizardProfile = wizardProfileRepository.save(wizardProfile);

        // Map to response
        return WizardMapper.toWizardResponse(wizardProfile);
    }
}
