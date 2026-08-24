package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {
    private Long id;
    private ExpenseCategory category;
    private String budgetMonth;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double usagePercentage;
    private Boolean isExceeded;
    private BigDecimal alertThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
