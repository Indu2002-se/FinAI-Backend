package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.IncomeCategory;
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
public class IncomeRequest {

    @NotBlank(message = "Source is required")
    private String source;

    @NotNull(message = "Category is required")
    private IncomeCategory category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Income date is required")
    private LocalDate incomeDate;

    private String description;
    private Boolean isRecurring;
}
