package com.finai.backend.entity;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Wizard Profile entity
 * Stores user onboarding wizard data
 */
@Entity
@Table(name = "wizard_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WizardProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "monthly_expense", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyExpense;

    @Column(name = "savings_goal", nullable = false, precision = 15, scale = 2)
    private BigDecimal savingsGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_knowledge_level", nullable = false, length = 20)
    private FinancialKnowledgeLevel financialKnowledgeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus;

    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency;
}
