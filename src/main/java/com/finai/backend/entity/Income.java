package com.finai.backend.entity;

import com.finai.backend.entity.enums.IncomeCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "incomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private IncomeCategory category;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;
}
