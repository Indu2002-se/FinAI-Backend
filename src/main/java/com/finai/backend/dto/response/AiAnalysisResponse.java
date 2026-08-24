package com.finai.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysisResponse {
    private FinancialRiskResponse risk;
    private ExpenseForecastResponse forecast;
    private AiRecommendationResponse recommendation;
}
