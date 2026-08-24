package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotBlank(message = "Budget month is required (format: YYYY-MM)")
    private String budgetMonth;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(value = "0.01", message = "Allocated amount must be greater than zero")
    private BigDecimal allocatedAmount;

    private BigDecimal alertThreshold;
}
