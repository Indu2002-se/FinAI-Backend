package com.finai.backend.controller;

import com.finai.backend.dto.response.*;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.AiService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Insights", description = "Financial risk, forecasting, and AI recommendation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;
    private final SecurityUtils securityUtils;

    @PostMapping("/analyze")
    @Operation(summary = "Trigger full AI financial analysis (Risk, SHAP, Forecast, Recommendations)")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> runFullAnalysis() {
        User user = securityUtils.getCurrentUser();
        AiAnalysisResponse response = aiService.runFullAnalysis(user);
        return ResponseEntity.ok(ApiResponse.success("AI financial analysis completed successfully", response));
    }

    @PostMapping("/risk")
    @Operation(summary = "Request risk and financial health score prediction")
    public ResponseEntity<ApiResponse<FinancialRiskResponse>> predictRisk() {
        User user = securityUtils.getCurrentUser();
        FinancialRiskResponse response = aiService.getLatestRiskPrediction(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/forecast")
    @Operation(summary = "Request 6-month household expense forecast")
    public ResponseEntity<ApiResponse<ExpenseForecastResponse>> forecastExpenses() {
        User user = securityUtils.getCurrentUser();
        ExpenseForecastResponse response = aiService.getLatestForecast(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/recommendation")
    @Operation(summary = "Request personalized financial recommendations")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> getRecommendation() {
        User user = securityUtils.getCurrentUser();
        AiRecommendationResponse response = aiService.getLatestRecommendation(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest persisted AI results")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> getLatestAiResults() {
        User user = securityUtils.getCurrentUser();
        FinancialRiskResponse risk = aiService.getLatestRiskPrediction(user);
        ExpenseForecastResponse forecast = aiService.getLatestForecast(user);
        AiRecommendationResponse recommendation = aiService.getLatestRecommendation(user);

        AiAnalysisResponse response = AiAnalysisResponse.builder()
                .risk(risk)
                .forecast(forecast)
                .recommendation(recommendation)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/savings-plan/{goalId}")
    @Operation(summary = "Generate AI Savings Plan (Gemini) for specific savings goal")
    public ResponseEntity<ApiResponse<SavingsPlanResponse>> generateSavingsPlan(@PathVariable Long goalId) {
        User user = securityUtils.getCurrentUser();
        SavingsPlanResponse response = aiService.generateSavingsPlan(goalId, user);
        return ResponseEntity.ok(ApiResponse.success("AI Savings Plan generated successfully", response));
    }
}
