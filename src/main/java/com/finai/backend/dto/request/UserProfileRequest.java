package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Integer age;
    private String gender;
    private String education;
    private String maritalStatus;
    private String occupation;
    private EmploymentStatus employmentStatus;
    private Integer householdSize;
    private Integer dependentsCount;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal savingsGoal;
    private BigDecimal totalDebt;
    private Integer creditScore;
    private String preferredCurrency;
    private FinancialKnowledgeLevel financialKnowledgeLevel;
}
