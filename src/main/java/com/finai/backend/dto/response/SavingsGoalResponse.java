package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.GoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalResponse {
    private Long id;
    private Long childProfileId;
    private String childName;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Double progressPercentage;
    private LocalDate deadline;
    private GoalStatus status;
    private String category;
    private String icon;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
