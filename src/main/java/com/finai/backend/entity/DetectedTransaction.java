package com.finai.backend.entity;

import com.finai.backend.entity.enums.DetectedSourceType;
import com.finai.backend.entity.enums.DetectedTransactionStatus;
import com.finai.backend.entity.enums.DetectedTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detected_transactions", indexes = {
        @Index(name = "idx_dt_user_status", columnList = "user_id, status"),
        @Index(name = "idx_dt_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_dt_user_hash", columnList = "user_id, raw_text_hash")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectedTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private DetectedSourceType sourceType;

    @Column(name = "source_app", length = 150)
    private String sourceApp;

    @Column(name = "source_sender", length = 100)
    private String sourceSender;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private DetectedTransactionType transactionType;

    @Column(name = "merchant", length = 255)
    private String merchant;

    @Column(name = "account_reference", length = 50)
    private String accountReference;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "reference", length = 150)
    private String reference;

    @Column(name = "raw_text_hash", length = 128)
    private String rawTextHash;

    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DetectedTransactionStatus status;

    @Column(name = "suggested_category", length = 50)
    private String suggestedCategory;

    @Column(name = "confirmed_income_id")
    private Long confirmedIncomeId;

    @Column(name = "confirmed_expense_id")
    private Long confirmedExpenseId;
}
