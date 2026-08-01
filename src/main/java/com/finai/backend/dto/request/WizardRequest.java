package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Wizard request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WizardRequest {

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid monthly income format")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Monthly expense is required")
    @DecimalMin(value = "0.0", message = "Monthly expense must be greater than or equal to 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid monthly expense format")
    private BigDecimal monthlyExpense;

    @NotNull(message = "Savings goal is required")
    @DecimalMin(value = "0.0", message = "Savings goal must be greater than or equal to 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid savings goal format")
    private BigDecimal savingsGoal;

    @NotNull(message = "Financial knowledge level is required")
    private FinancialKnowledgeLevel financialKnowledgeLevel;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;

    @NotBlank(message = "Preferred currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO 4217)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency code format")
    private String preferredCurrency;
}
