package com.finai.backend.dto.request;

import com.finai.backend.entity.enums.DetectedSourceType;
import com.finai.backend.entity.enums.DetectedTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectedTransactionRequest {

    @NotNull(message = "Source type is required")
    private DetectedSourceType sourceType;

    private String sourceApp;
    private String sourceSender;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private DetectedTransactionType transactionType;

    private String merchant;
    private String accountReference;
    private LocalDateTime transactionDate;
    private String reference;
    private String rawTextHash;
    private BigDecimal confidence;
    private String suggestedCategory;
}
