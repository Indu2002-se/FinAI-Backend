package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.DebtStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtResponse {
    private Long id;
    private String debtName;
    private String source;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal monthlyPayment;
    private BigDecimal interestRate;
    private LocalDate dueDate;
    private DebtStatus status;
    private Boolean isCreditCard;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
