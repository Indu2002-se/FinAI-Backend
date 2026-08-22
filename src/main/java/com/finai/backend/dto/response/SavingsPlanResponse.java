package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsPlanResponse {
    private String goalTitle;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Integer targetMonths;
    private BigDecimal monthlyRequiredSavings;
    private BigDecimal monthlySurplus;
    private Double feasibilityScore;
    private String feasibilityStatus;
    private String difficultyLevel;
    private List<CategoryReductionItem> categoryReductions;
    private List<MilestoneItem> milestones;
    private String aiStrategyReport;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryReductionItem {
        private String category;
        private BigDecimal suggestedCut;
        private String action;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MilestoneItem {
        private Integer month;
        private BigDecimal targetAccumulated;
        private Double completionPercentage;
    }
}
