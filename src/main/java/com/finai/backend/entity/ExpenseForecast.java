package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense_forecasts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseForecast extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "food_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal foodAmount;

    @Column(name = "nonfood_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal nonFoodAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "lower_bound", precision = 15, scale = 2)
    private BigDecimal lowerBound;

    @Column(name = "upper_bound", precision = 15, scale = 2)
    private BigDecimal upperBound;
}
