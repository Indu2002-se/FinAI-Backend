package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.InferenceSource;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseForecastResponse {
    private List<ForecastItem> food;
    private List<ForecastItem> nonFood;
    private List<ForecastItem> total;
    private Integer forecastMonths;
    private InferenceSource inferenceSource;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastItem {
        private String date;
        private BigDecimal predictedAmount;
        private BigDecimal lowerBound;
        private BigDecimal upperBound;
    }
}
