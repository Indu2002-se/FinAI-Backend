package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private String userName;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private BigDecimal totalSavingsBalance;
    private BigDecimal totalDebt;
    
    // Budget
    private BigDecimal monthlyBudgetAllocated;
    private BigDecimal monthlyBudgetSpent;
    private Double budgetUsagePercentage;

    // AI summary
    private BigDecimal financialHealthScore;
    private String riskLevel;
    private BigDecimal riskProbability;
    private String topRiskDriver;
    private String forecastSummary;
    private String latestRecommendation;

    // Recent items
    private List<ExpenseResponse> recentExpenses;
    private List<IncomeResponse> recentIncomes;
    private List<NotificationAlertResponse> alerts;
}
