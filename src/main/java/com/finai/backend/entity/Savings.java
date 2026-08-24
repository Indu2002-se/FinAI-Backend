package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "savings_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Savings extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "target_monthly_deposit", precision = 15, scale = 2)
    private BigDecimal targetMonthlyDeposit;

    @Column(name = "notes", length = 500)
    private String notes;
}
