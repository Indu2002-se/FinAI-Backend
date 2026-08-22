package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetStatusResponse {
    private String month;
    private BigDecimal totalAllocated;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private Double overallUsagePercentage;
    private List<BudgetResponse> categoryBudgets;
}
