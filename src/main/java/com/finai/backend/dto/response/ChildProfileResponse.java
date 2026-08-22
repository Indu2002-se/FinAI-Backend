package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String avatar;
    private BigDecimal currentSavings;
    private Integer totalPoints;
    private Integer activeGoalsCount;
    private Integer completedQuizzesCount;
    private Integer totalRewardsCount;
    private LocalDateTime createdAt;
}
