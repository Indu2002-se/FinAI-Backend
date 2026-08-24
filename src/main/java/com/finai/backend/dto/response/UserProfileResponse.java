package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
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
    private Boolean profileComplete;
}
