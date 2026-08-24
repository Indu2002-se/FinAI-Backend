package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoalRequest {

    private Long childProfileId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;

    @DecimalMin(value = "0.00", message = "Current amount must be non-negative")
    private BigDecimal currentAmount;

    private LocalDate deadline;
    private GoalStatus status;
    private String category;
    private String icon;
    private String notes;
}
