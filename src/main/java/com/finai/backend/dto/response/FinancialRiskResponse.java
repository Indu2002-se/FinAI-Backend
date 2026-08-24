package com.finai.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRiskResponse {
    private BigDecimal financialHealthScore;
    private String riskLevel;
    private BigDecimal riskProbability;
    private String topDriver;
    private String topDriverReadable;
    private List<DriverDetail> drivers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriverDetail {
        private String feature;
        private Double impact;
        private String direction;
        private String readableName;
        private String description;
    }
}
