package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportResponse {
    private String month; // YYYY-MM
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private BigDecimal savingsRate; // percentage
    private BigDecimal expenseToIncomeRatio; // percentage
    
    // Category Breakdown
    private Map<ExpenseCategory, BigDecimal> categoryExpenses;
    
    // Budget Performance
    private BigDecimal budgetAllocated;
    private BigDecimal budgetSpent;
    private BigDecimal budgetVariance;

    // AI Analysis in this report
    private BigDecimal financialHealthScore;
    private String riskLevel;
    private String topRiskDriver;
    private String aiRecommendation;
}
