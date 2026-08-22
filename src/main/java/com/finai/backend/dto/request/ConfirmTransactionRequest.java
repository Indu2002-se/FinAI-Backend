package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.ExpenseCategory;
import com.finai.backend.entity.enums.IncomeCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmTransactionRequest {
    private BigDecimal amount;
    private ExpenseCategory expenseCategory;
    private IncomeCategory incomeCategory;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethodOrSource;
}
