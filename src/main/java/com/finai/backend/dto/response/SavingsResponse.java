package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsResponse {
    private Long id;
    private String accountName;
    private String bankName;
    private BigDecimal currentBalance;
    private BigDecimal targetMonthlyDeposit;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
