package com.finai.backend.util;

import com.finai.backend.dto.response.WizardResponse;
import com.finai.backend.entity.WizardProfile;

/**
 * Utility class for mapping WizardProfile entity to DTOs
 */
public class WizardMapper {

    private WizardMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Map WizardProfile entity to WizardResponse DTO
     */
    public static WizardResponse toWizardResponse(WizardProfile wizardProfile) {
        return WizardResponse.builder()
                .id(wizardProfile.getId())
                .userId(wizardProfile.getUser().getId())
                .monthlyIncome(wizardProfile.getMonthlyIncome())
                .monthlyExpense(wizardProfile.getMonthlyExpense())
                .savingsGoal(wizardProfile.getSavingsGoal())
                .financialKnowledgeLevel(wizardProfile.getFinancialKnowledgeLevel())
                .employmentStatus(wizardProfile.getEmploymentStatus())
                .preferredCurrency(wizardProfile.getPreferredCurrency())
                .createdAt(wizardProfile.getCreatedAt())
                .updatedAt(wizardProfile.getUpdatedAt())
                .build();
    }
}
