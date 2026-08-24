package com.finai.backend.entity;

import com.finai.backend.entity.enums.EmploymentStatus;
import com.finai.backend.entity.enums.FinancialKnowledgeLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "education", length = 50)
    private String education;

    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 30)
    private EmploymentStatus employmentStatus;

    @Column(name = "household_size")
    @Builder.Default
    private Integer householdSize = 1;

    @Column(name = "dependents_count")
    @Builder.Default
    private Integer dependentsCount = 0;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "monthly_expense", precision = 15, scale = 2)
    private BigDecimal monthlyExpense;

    @Column(name = "savings_goal", precision = 15, scale = 2)
    private BigDecimal savingsGoal;

    @Column(name = "total_debt", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDebt = BigDecimal.ZERO;

    @Column(name = "credit_score")
    @Builder.Default
    private Integer creditScore = 700;

    @Column(name = "preferred_currency", length = 10)
    @Builder.Default
    private String preferredCurrency = "LKR";

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_knowledge_level", length = 30)
    private FinancialKnowledgeLevel financialKnowledgeLevel;
}
