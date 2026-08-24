package com.finai.backend.service.interfaces;

import com.finai.backend.dto.response.AiAnalysisResponse;
import com.finai.backend.dto.response.AiRecommendationResponse;
import com.finai.backend.dto.response.ExpenseForecastResponse;
import com.finai.backend.dto.response.FinancialRiskResponse;
import com.finai.backend.dto.response.SavingsPlanResponse;
import com.finai.backend.entity.User;

public interface AiService {
    AiAnalysisResponse runFullAnalysis(User user);
    FinancialRiskResponse getLatestRiskPrediction(User user);
    ExpenseForecastResponse getLatestForecast(User user);
    AiRecommendationResponse getLatestRecommendation(User user);
    SavingsPlanResponse generateSavingsPlan(Long goalId, User user);
}
