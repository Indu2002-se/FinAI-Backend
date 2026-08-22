package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildDashboardResponse {
    private Long childProfileId;
    private String childName;
    private Integer age;
    private String avatar;
    private BigDecimal currentSavings;
    private Integer totalPoints;
    private List<SavingsGoalResponse> savingsGoals;
    private List<QuizResponse> recommendedQuizzes;
    private List<RewardResponse> recentRewards;
}
