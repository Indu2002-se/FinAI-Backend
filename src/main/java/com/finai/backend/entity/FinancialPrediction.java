package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "financial_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialPrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "financial_health_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal financialHealthScore;

    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "risk_probability", nullable = false, precision = 5, scale = 4)
    private BigDecimal riskProbability;

    @Column(name = "top_driver", length = 100)
    private String topDriver;

    @Column(name = "top_driver_readable", length = 150)
    private String topDriverReadable;

    @Column(name = "explanation_json", columnDefinition = "TEXT")
    private String explanationJson;
}
