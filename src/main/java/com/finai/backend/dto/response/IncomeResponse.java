package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.IncomeCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeResponse {
    private Long id;
    private String source;
    private IncomeCategory category;
    private BigDecimal amount;
    private LocalDate incomeDate;
    private String description;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
