package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wizard response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WizardResponse {

    private Long id;
    private Long userId;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal savingsGoal;
    private FinancialKnowledgeLevel financialKnowledgeLevel;
    private EmploymentStatus employmentStatus;
    private String preferredCurrency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
