package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.DetectedSourceType;
import com.finai.backend.entity.enums.DetectedTransactionStatus;
import com.finai.backend.entity.enums.DetectedTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectedTransactionResponse {
    private Long id;
    private DetectedSourceType sourceType;
    private String sourceApp;
    private String sourceSender;
    private BigDecimal amount;
    private DetectedTransactionType transactionType;
    private String merchant;
    private String accountReference;
    private LocalDateTime transactionDate;
    private String reference;
    private String rawTextHash;
    private BigDecimal confidence;
    private DetectedTransactionStatus status;
    private String suggestedCategory;
    private Long confirmedIncomeId;
    private Long confirmedExpenseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
