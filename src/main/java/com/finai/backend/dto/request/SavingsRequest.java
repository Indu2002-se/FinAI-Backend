package com.finai.backend.dto.request;

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
public class SavingsRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    private String bankName;

    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.00", message = "Current balance must be non-negative")
    private BigDecimal currentBalance;

    private BigDecimal targetMonthlyDeposit;
    private String notes;
}
