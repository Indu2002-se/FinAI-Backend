package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private ExpenseCategory category;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
