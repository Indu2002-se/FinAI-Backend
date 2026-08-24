package com.finai.backend.entity;

import com.finai.backend.entity.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "category", "budget_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(name = "budget_month", nullable = false, length = 7) // Format: YYYY-MM
    private String budgetMonth;

    @Column(name = "allocated_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "spent_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "alert_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal alertThreshold = new BigDecimal("80.00"); // Alert at 80% usage
}
