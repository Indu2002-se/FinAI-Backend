package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.DebtStatus;
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
public class DebtRequest {

    @NotBlank(message = "Debt name is required")
    private String debtName;

    @NotBlank(message = "Source is required")
    private String source;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    @NotNull(message = "Remaining amount is required")
    @DecimalMin(value = "0.00", message = "Remaining amount must be non-negative")
    private BigDecimal remainingAmount;

    private BigDecimal monthlyPayment;
    private BigDecimal interestRate;
    private LocalDate dueDate;
    private DebtStatus status;
    private Boolean isCreditCard;
}
