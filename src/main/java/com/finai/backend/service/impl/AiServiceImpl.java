package com.finai.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finai.backend.dto.response.*;
import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.ExpenseCategory;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.*;
import com.finai.backend.service.interfaces.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final FinancialPredictionRepository financialPredictionRepository;
    private final ExpenseForecastRepository expenseForecastRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final UserProfileRepository userProfileRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final DebtRepository debtRepository;
    private final SavingsRepository savingsRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Override
    @Transactional
    public AiAnalysisResponse runFullAnalysis(User user) {
        log.info("Executing full AI financial analysis for user id: {}", user.getId());

        // 1. Build Model 1 Feature Vector
        Map<String, Object> features = buildModel1Features(user);

        // 2. Build Expense History
        List<Map<String, Object>> history = buildExpenseHistory(user);

        // 3. Call FastAPI /api/v1/ai/analyze
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("userId", user.getId());
        requestPayload.put("features", features);
        requestPayload.put("expenseHistory", history);
        requestPayload.put("forecastMonths", 6);

        FinancialRiskResponse riskResponse = null;
        ExpenseForecastResponse forecastResponse = null;
        AiRecommendationResponse recommendationResponse = null;

        try {
            String url = aiServiceUrl + "/api/v1/ai/analyze";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Parse Risk
                Map<String, Object> riskMap = (Map<String, Object>) body.get("risk");
                Map<String, Object> expMap = (Map<String, Object>) body.get("explanation");
                riskResponse = parseRiskResponse(riskMap, expMap);

                // Parse Forecast
                Map<String, Object> fcMap = (Map<String, Object>) body.get("forecast");
                forecastResponse = parseForecastResponse(fcMap);

                // Parse Recommendation
                Map<String, Object> recMap = (Map<String, Object>) body.get("recommendation");
                recommendationResponse = parseRecommendationResponse(recMap);
            }
        } catch (Exception e) {
            log.warn("FastAPI service connection error at {}: {}. Generating rule-based analytical baseline.", aiServiceUrl, e.getMessage());
            riskResponse = generateFallbackRisk(features);
            forecastResponse = generateFallbackForecast(history);
            recommendationResponse = generateFallbackRecommendation(riskResponse, features);
        }

        // 4. Persist AI Results
        persistAiResults(user, riskResponse, forecastResponse, recommendationResponse);

        return AiAnalysisResponse.builder()
                .risk(riskResponse)
                .forecast(forecastResponse)
                .recommendation(recommendationResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialRiskResponse getLatestRiskPrediction(User user) {
        return financialPredictionRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(this::mapToRiskResponse)
                .orElseGet(() -> {
                    // Compute on-the-fly if not present
                    Map<String, Object> features = buildModel1Features(user);
                    return generateFallbackRisk(features);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseForecastResponse getLatestForecast(User user) {
        List<ExpenseForecast> forecasts = expenseForecastRepository.findByUserOrderByForecastDateAsc(user);
        if (forecasts.isEmpty()) {
            List<Map<String, Object>> history = buildExpenseHistory(user);
            return generateFallbackForecast(history);
        }

        List<ExpenseForecastResponse.ForecastItem> food = new ArrayList<>();
        List<ExpenseForecastResponse.ForecastItem> nonFood = new ArrayList<>();
        List<ExpenseForecastResponse.ForecastItem> total = new ArrayList<>();

        for (ExpenseForecast ef : forecasts) {
            String dateStr = ef.getForecastDate().toString();
            food.add(ExpenseForecastResponse.ForecastItem.builder()
                    .date(dateStr)
                    .predictedAmount(ef.getFoodAmount())
                    .lowerBound(ef.getFoodAmount().multiply(new BigDecimal("0.92")))
                    .upperBound(ef.getFoodAmount().multiply(new BigDecimal("1.08")))
                    .build());

            nonFood.add(ExpenseForecastResponse.ForecastItem.builder()
                    .date(dateStr)
                    .predictedAmount(ef.getNonFoodAmount())
                    .lowerBound(ef.getNonFoodAmount().multiply(new BigDecimal("0.90")))
                    .upperBound(ef.getNonFoodAmount().multiply(new BigDecimal("1.10")))
                    .build());

            total.add(ExpenseForecastResponse.ForecastItem.builder()
                    .date(dateStr)
                    .predictedAmount(ef.getTotalAmount())
                    .lowerBound(ef.getLowerBound())
                    .upperBound(ef.getUpperBound())
                    .build());
        }

        return ExpenseForecastResponse.builder()
                .food(food)
                .nonFood(nonFood)
                .total(total)
                .forecastMonths(forecasts.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AiRecommendationResponse getLatestRecommendation(User user) {
        return aiRecommendationRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(this::mapToRecommendationResponse)
                .orElseGet(() -> {
                    Map<String, Object> features = buildModel1Features(user);
                    FinancialRiskResponse risk = generateFallbackRisk(features);
                    return generateFallbackRecommendation(risk, features);
                });
    }

    private Map<String, Object> buildModel1Features(User user) {
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalIncome = incomeRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0 && profile != null && profile.getMonthlyIncome() != null) {
            totalIncome = profile.getMonthlyIncome();
        }
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            totalIncome = new BigDecimal("100000.00");
        }

        BigDecimal foodExp = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, ExpenseCategory.FOOD, startOfMonth, endOfMonth);
        BigDecimal totalExp = expenseRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        if (totalExp.compareTo(BigDecimal.ZERO) == 0 && profile != null && profile.getMonthlyExpense() != null) {
            totalExp = profile.getMonthlyExpense();
            foodExp = totalExp.multiply(new BigDecimal("0.45"));
        }
        if (totalExp.compareTo(BigDecimal.ZERO) == 0) {
            totalExp = totalIncome.multiply(new BigDecimal("0.60"));
            foodExp = totalExp.multiply(new BigDecimal("0.45"));
        }

        BigDecimal nonFoodExp = totalExp.subtract(foodExp);
        if (nonFoodExp.compareTo(BigDecimal.ZERO) < 0) nonFoodExp = BigDecimal.ZERO;

        BigDecimal totalDebt = debtRepository.sumTotalActiveDebtByUser(user);
        if (profile != null && profile.getTotalDebt() != null && totalDebt.compareTo(BigDecimal.ZERO) == 0) {
            totalDebt = profile.getTotalDebt();
        }

        BigDecimal totalSavings = savingsRepository.sumTotalSavingsByUser(user);

        BigDecimal surplus = totalIncome.subtract(totalExp);
        double e2i = totalIncome.compareTo(BigDecimal.ZERO) > 0 ? totalExp.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() : 0.6;
        double d2i = totalIncome.compareTo(BigDecimal.ZERO) > 0 ? totalDebt.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() : 0.0;
        double savingsRatio = totalIncome.compareTo(BigDecimal.ZERO) > 0 ? totalSavings.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() : 0.2;

        int householdSize = profile != null && profile.getHouseholdSize() != null ? profile.getHouseholdSize() : 3;
        double perCapitaIncome = totalIncome.doubleValue() / Math.max(1, householdSize);

        Map<String, Object> map = new HashMap<>();
        map.put("age", profile != null && profile.getAge() != null ? profile.getAge() : 35);
        map.put("gender", 1);
        map.put("education", 2);
        map.put("marital_status", 1);
        map.put("household_size_f", householdSize);
        map.put("employment_income", totalIncome.doubleValue() * 0.85);
        map.put("other_income", totalIncome.doubleValue() * 0.15);
        map.put("windfall_income", 0.0);
        map.put("agri_income", 0.0);
        map.put("non_agri_income", totalIncome.doubleValue());
        map.put("transfer_income", 0.0);
        map.put("total_income", totalIncome.doubleValue());
        map.put("food_expenditure", foodExp.doubleValue());
        map.put("nonfood_expenditure", nonFoodExp.doubleValue());
        map.put("total_expenditure", totalExp.doubleValue());
        map.put("expense_to_income_ratio", e2i);
        map.put("financial_surplus", surplus.doubleValue());
        map.put("savings_ratio", savingsRatio);
        map.put("per_capita_income", perCapitaIncome);
        map.put("employment_capacity", 1.0);
        map.put("debt_amount", totalDebt.doubleValue());
        map.put("debt_records", totalDebt.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        map.put("debt_sources", totalDebt.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        map.put("debt_to_income_ratio", d2i);
        map.put("credit_card_debt", 0.0);
        map.put("has_credit_card_debt", 0);
        map.put("has_creditmix_match", 1);
        map.put("credit_score", profile != null && profile.getCreditScore() != null ? profile.getCreditScore() : 720);
        map.put("credit_defaulted", 0);
        map.put("credit_clv", 150000.0);
        map.put("credit_fraud_txn", 0);
        map.put("cc_utilization_ratio", 0.15);
        map.put("cc_late_payments", 0);
        map.put("cc_credit_lines", 1);
        map.put("cc_debt_to_income_ratio", 0.0);
        map.put("cc_total_spend_last_year", totalExp.doubleValue() * 0.5);
        map.put("cc_avg_txn_amount", 3500.0);
        map.put("cc_total_txns", 24);
        map.put("cc_tenure_years", 3.0);
        map.put("vehicle_ownership", 1);
        map.put("instalment_goods_flag", 0);
        map.put("instalment_amount", 0.0);

        return map;
    }

    private List<Map<String, Object>> buildExpenseHistory(User user) {
        List<Map<String, Object>> history = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDate start = monthDate.withDayOfMonth(1);
            LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

            BigDecimal food = expenseRepository.sumAmountByUserAndCategoryAndDateRange(user, ExpenseCategory.FOOD, start, end);
            BigDecimal total = expenseRepository.sumAmountByUserAndDateRange(user, start, end);
            BigDecimal nonFood = total.subtract(food);

            if (total.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", start.toString());
                point.put("food", food.doubleValue());
                point.put("nonFood", Math.max(0, nonFood.doubleValue()));
                point.put("total", total.doubleValue());
                history.add(point);
            }
        }
        return history;
    }

    private void persistAiResults(User user, 
                                  FinancialRiskResponse risk, 
                                  ExpenseForecastResponse forecast, 
                                  AiRecommendationResponse rec) {
        try {
            // Persist Risk
            if (risk != null) {
                String expJson = risk.getDrivers() != null ? objectMapper.writeValueAsString(risk.getDrivers()) : "[]";
                FinancialPrediction fp = FinancialPrediction.builder()
                        .user(user)
                        .financialHealthScore(risk.getFinancialHealthScore())
                        .riskLevel(risk.getRiskLevel())
                        .riskProbability(risk.getRiskProbability())
                        .topDriver(risk.getTopDriver())
                        .topDriverReadable(risk.getTopDriverReadable())
                        .explanationJson(expJson)
                        .build();
                financialPredictionRepository.save(fp);
            }

            // Persist Forecast
            if (forecast != null && forecast.getTotal() != null) {
                expenseForecastRepository.deleteByUser(user);
                for (int i = 0; i < forecast.getTotal().size(); i++) {
                    var tot = forecast.getTotal().get(i);
                    var f = (forecast.getFood() != null && i < forecast.getFood().size()) ? forecast.getFood().get(i) : null;
                    var nf = (forecast.getNonFood() != null && i < forecast.getNonFood().size()) ? forecast.getNonFood().get(i) : null;

                    ExpenseForecast ef = ExpenseForecast.builder()
                            .user(user)
                            .forecastDate(LocalDate.parse(tot.getDate()))
                            .foodAmount(f != null ? f.getPredictedAmount() : BigDecimal.ZERO)
                            .nonFoodAmount(nf != null ? nf.getPredictedAmount() : BigDecimal.ZERO)
                            .totalAmount(tot.getPredictedAmount())
                            .lowerBound(tot.getLowerBound())
                            .upperBound(tot.getUpperBound())
                            .build();
                    expenseForecastRepository.save(ef);
                }
            }

            // Persist Recommendation
            if (rec != null) {
                String actionsJson = rec.getActionItems() != null ? objectMapper.writeValueAsString(rec.getActionItems()) : "[]";
                AiRecommendation r = AiRecommendation.builder()
                        .user(user)
                        .category(rec.getCategory())
                        .topDriver(rec.getTopDriver())
                        .recommendationText(rec.getRecommendationText())
                        .actionItemsJson(actionsJson)
                        .isApplied(false)
                        .build();
                aiRecommendationRepository.save(r);
            }
        } catch (Exception e) {
            log.error("Error persisting AI results: {}", e.getMessage());
        }
    }

    private FinancialRiskResponse parseRiskResponse(Map<String, Object> riskMap, Map<String, Object> expMap) {
        if (riskMap == null) return null;
        BigDecimal score = BigDecimal.valueOf(((Number) riskMap.getOrDefault("financialHealthScore", 75.0)).doubleValue());
        String level = (String) riskMap.getOrDefault("riskLevel", "Low Risk");
        BigDecimal prob = BigDecimal.valueOf(((Number) riskMap.getOrDefault("riskProbability", 0.20)).doubleValue());

        String topDriver = "expense_to_income_ratio";
        String topDriverReadable = "Expense-to-Income Ratio";
        List<FinancialRiskResponse.DriverDetail> drivers = new ArrayList<>();

        if (expMap != null) {
            topDriver = (String) expMap.getOrDefault("topDriver", topDriver);
            topDriverReadable = (String) expMap.getOrDefault("topDriverReadable", topDriverReadable);
            List<Map<String, Object>> drvList = (List<Map<String, Object>>) expMap.get("drivers");
            if (drvList != null) {
                for (Map<String, Object> d : drvList) {
                    drivers.add(FinancialRiskResponse.DriverDetail.builder()
                            .feature((String) d.get("feature"))
                            .impact(((Number) d.getOrDefault("impact", 0.0)).doubleValue())
                            .direction((String) d.get("direction"))
                            .readableName((String) d.get("readableName"))
                            .description((String) d.get("description"))
                            .build());
                }
            }
        }

        return FinancialRiskResponse.builder()
                .financialHealthScore(score)
                .riskLevel(level)
                .riskProbability(prob)
                .topDriver(topDriver)
                .topDriverReadable(topDriverReadable)
                .drivers(drivers)
                .build();
    }

    private ExpenseForecastResponse parseForecastResponse(Map<String, Object> fcMap) {
        if (fcMap == null) return null;
        List<ExpenseForecastResponse.ForecastItem> food = parseForecastItemList((List<Map<String, Object>>) fcMap.get("food"));
        List<ExpenseForecastResponse.ForecastItem> nonFood = parseForecastItemList((List<Map<String, Object>>) fcMap.get("nonFood"));
        List<ExpenseForecastResponse.ForecastItem> total = parseForecastItemList((List<Map<String, Object>>) fcMap.get("total"));
        int months = ((Number) fcMap.getOrDefault("forecastMonths", 6)).intValue();

        return ExpenseForecastResponse.builder()
                .food(food)
                .nonFood(nonFood)
                .total(total)
                .forecastMonths(months)
                .build();
    }

    private List<ExpenseForecastResponse.ForecastItem> parseForecastItemList(List<Map<String, Object>> list) {
        List<ExpenseForecastResponse.ForecastItem> result = new ArrayList<>();
        if (list == null) return result;
        for (Map<String, Object> item : list) {
            result.add(ExpenseForecastResponse.ForecastItem.builder()
                    .date((String) item.get("date"))
                    .predictedAmount(BigDecimal.valueOf(((Number) item.getOrDefault("predictedAmount", 0.0)).doubleValue()))
                    .lowerBound(BigDecimal.valueOf(((Number) item.getOrDefault("lowerBound", 0.0)).doubleValue()))
                    .upperBound(BigDecimal.valueOf(((Number) item.getOrDefault("upperBound", 0.0)).doubleValue()))
                    .build());
        }
        return result;
    }

    private AiRecommendationResponse parseRecommendationResponse(Map<String, Object> recMap) {
        if (recMap == null) return null;
        String cat = (String) recMap.getOrDefault("category", "Expense Optimization");
        String topDriver = (String) recMap.getOrDefault("topDriver", "expense_to_income_ratio");
        String text = (String) recMap.getOrDefault("recommendation", "");
        List<String> actions = (List<String>) recMap.getOrDefault("actionItems", Collections.emptyList());

        return AiRecommendationResponse.builder()
                .category(cat)
                .topDriver(topDriver)
                .recommendationText(text)
                .actionItems(actions)
                .isApplied(false)
                .build();
    }

    private FinancialRiskResponse mapToRiskResponse(FinancialPrediction fp) {
        List<FinancialRiskResponse.DriverDetail> drivers = new ArrayList<>();
        if (fp.getExplanationJson() != null && !fp.getExplanationJson().isBlank()) {
            try {
                drivers = objectMapper.readValue(fp.getExplanationJson(), new TypeReference<List<FinancialRiskResponse.DriverDetail>>() {});
            } catch (Exception ignored) {}
        }

        return FinancialRiskResponse.builder()
                .financialHealthScore(fp.getFinancialHealthScore())
                .riskLevel(fp.getRiskLevel())
                .riskProbability(fp.getRiskProbability())
                .topDriver(fp.getTopDriver())
                .topDriverReadable(fp.getTopDriverReadable())
                .drivers(drivers)
                .build();
    }

    private AiRecommendationResponse mapToRecommendationResponse(AiRecommendation r) {
        List<String> actions = new ArrayList<>();
        if (r.getActionItemsJson() != null && !r.getActionItemsJson().isBlank()) {
            try {
                actions = objectMapper.readValue(r.getActionItemsJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }

        return AiRecommendationResponse.builder()
                .category(r.getCategory())
                .topDriver(r.getTopDriver())
                .recommendationText(r.getRecommendationText())
                .actionItems(actions)
                .isApplied(r.getIsApplied())
                .build();
    }

    private FinancialRiskResponse generateFallbackRisk(Map<String, Object> features) {
        double e2i = ((Number) features.getOrDefault("expense_to_income_ratio", 0.6)).doubleValue();
        double d2i = ((Number) features.getOrDefault("debt_to_income_ratio", 0.0)).doubleValue();
        
        String level = "Low Risk";
        double prob = 0.15;
        double score = 85.0;

        if (e2i > 0.85 || d2i > 0.40) {
            level = "High Risk";
            prob = 0.78;
            score = 36.0;
        } else if (e2i > 0.65 || d2i > 0.20) {
            level = "Medium Risk";
            prob = 0.45;
            score = 62.0;
        }

        List<FinancialRiskResponse.DriverDetail> drivers = List.of(
                FinancialRiskResponse.DriverDetail.builder()
                        .feature("expense_to_income_ratio")
                        .impact(e2i)
                        .direction(e2i > 0.7 ? "increases_risk" : "decreases_risk")
                        .readableName("Expense-to-Income Ratio")
                        .description("Portion of your monthly earnings dedicated to routine living expenses.")
                        .build(),
                FinancialRiskResponse.DriverDetail.builder()
                        .feature("financial_surplus")
                        .impact(0.55)
                        .direction("decreases_risk")
                        .readableName("Financial Surplus")
                        .description("Residual monthly income available for discretionary savings.")
                        .build()
        );

        return FinancialRiskResponse.builder()
                .financialHealthScore(BigDecimal.valueOf(score))
                .riskLevel(level)
                .riskProbability(BigDecimal.valueOf(prob))
                .topDriver("expense_to_income_ratio")
                .topDriverReadable("Expense-to-Income Ratio")
                .drivers(drivers)
                .build();
    }

    private ExpenseForecastResponse generateFallbackForecast(List<Map<String, Object>> history) {
        LocalDate now = LocalDate.now();
        List<ExpenseForecastResponse.ForecastItem> food = new ArrayList<>();
        List<ExpenseForecastResponse.ForecastItem> nonFood = new ArrayList<>();
        List<ExpenseForecastResponse.ForecastItem> total = new ArrayList<>();

        for (int i = 1; i <= 6; i++) {
            LocalDate dt = now.plusMonths(i).withDayOfMonth(1);
            String dStr = dt.toString();
            BigDecimal fVal = new BigDecimal("25500.00").multiply(BigDecimal.valueOf(1.0 + 0.005 * i));
            BigDecimal nfVal = new BigDecimal("22000.00").multiply(BigDecimal.valueOf(1.0 + 0.004 * i));
            BigDecimal totVal = fVal.add(nfVal);

            food.add(ExpenseForecastResponse.ForecastItem.builder().date(dStr).predictedAmount(fVal.setScale(2, RoundingMode.HALF_UP)).build());
            nonFood.add(ExpenseForecastResponse.ForecastItem.builder().date(dStr).predictedAmount(nfVal.setScale(2, RoundingMode.HALF_UP)).build());
            total.add(ExpenseForecastResponse.ForecastItem.builder()
                    .date(dStr)
                    .predictedAmount(totVal.setScale(2, RoundingMode.HALF_UP))
                    .lowerBound(totVal.multiply(new BigDecimal("0.91")).setScale(2, RoundingMode.HALF_UP))
                    .upperBound(totVal.multiply(new BigDecimal("1.09")).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return ExpenseForecastResponse.builder()
                .food(food)
                .nonFood(nonFood)
                .total(total)
                .forecastMonths(6)
                .build();
    }

    private AiRecommendationResponse generateFallbackRecommendation(FinancialRiskResponse risk, Map<String, Object> features) {
        String cat = "Expense Optimization";
        String text = "Your monthly expenditures are consuming a significant portion of income. Setting category budget limits can free up surplus cash flow.";
        List<String> actions = List.of(
                "Review non-essential subscription and dining expenditures.",
                "Set realistic monthly budget caps for top expense categories.",
                "Target a 10% reduction in discretionary spending."
        );

        if ("Low Risk".equals(risk.getRiskLevel())) {
            cat = "Maintain & Grow Wealth";
            text = "Your financial habits are robust with healthy cash reserves. Consider investing surplus capital for inflation-beating long-term growth.";
            actions = List.of(
                    "Maintain your disciplined savings cadence.",
                    "Explore diversified long-term investment options.",
                    "Review asset allocation every 6 months."
            );
        }

        return AiRecommendationResponse.builder()
                .category(cat)
                .topDriver(risk.getTopDriver())
                .recommendationText(text)
                .actionItems(actions)
                .isApplied(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsPlanResponse generateSavingsPlan(Long goalId, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Savings Goal", "id", goalId));

        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal totalIncome = incomeRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0 && profile != null && profile.getMonthlyIncome() != null) {
            totalIncome = profile.getMonthlyIncome();
        }
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            totalIncome = new BigDecimal("100000.00");
        }

        BigDecimal totalExp = expenseRepository.sumAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        if (totalExp.compareTo(BigDecimal.ZERO) == 0 && profile != null && profile.getMonthlyExpense() != null) {
            totalExp = profile.getMonthlyExpense();
        }
        if (totalExp.compareTo(BigDecimal.ZERO) == 0) {
            totalExp = totalIncome.multiply(new BigDecimal("0.60"));
        }

        int targetMonths = 6;
        if (goal.getDeadline() != null) {
            long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(now, goal.getDeadline());
            if (monthsBetween > 0) {
                targetMonths = (int) monthsBetween;
            }
        }

        BigDecimal currentSavings = savingsRepository.sumTotalSavingsByUser(user);
        BigDecimal totalDebt = debtRepository.sumTotalActiveDebtByUser(user);

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("goalTitle", goal.getTitle());
        requestPayload.put("targetAmount", goal.getTargetAmount().doubleValue());
        requestPayload.put("currentAmount", goal.getCurrentAmount() != null ? goal.getCurrentAmount().doubleValue() : 0.0);
        requestPayload.put("targetMonths", targetMonths);
        requestPayload.put("monthlyIncome", totalIncome.doubleValue());
        requestPayload.put("monthlyExpense", totalExp.doubleValue());
        requestPayload.put("currentSavings", currentSavings.doubleValue());
        requestPayload.put("totalDebt", totalDebt.doubleValue());

        try {
            String url = aiServiceUrl + "/api/v1/ai/savings-plan/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), SavingsPlanResponse.class);
            }
        } catch (Exception e) {
            log.warn("FastAPI savings plan endpoint failed ({}), falling back to built-in generator", e.getMessage());
        }

        // Fallback calculation
        BigDecimal netTarget = goal.getTargetAmount().subtract(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
        if (netTarget.compareTo(BigDecimal.ZERO) < 0) netTarget = BigDecimal.ZERO;
        BigDecimal monthlyRequired = netTarget.divide(BigDecimal.valueOf(Math.max(1, targetMonths)), 2, RoundingMode.HALF_UP);
        BigDecimal monthlySurplus = totalIncome.subtract(totalExp);

        double coverage = monthlyRequired.compareTo(BigDecimal.ZERO) > 0 ? monthlySurplus.divide(monthlyRequired, 4, RoundingMode.HALF_UP).doubleValue() : 1.0;
        double score = Math.min(100.0, Math.max(10.0, coverage * 100.0));
        String status = coverage >= 1.0 ? "Highly Achievable" : coverage >= 0.7 ? "Achievable with Minor Budget Adjustments" : "Challenging — Expense Reductions Required";

        List<SavingsPlanResponse.MilestoneItem> milestones = new ArrayList<>();
        for (int m = 1; m <= targetMonths; m++) {
            BigDecimal acc = (goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO)
                    .add(monthlyRequired.multiply(BigDecimal.valueOf(m)));
            if (acc.compareTo(goal.getTargetAmount()) > 0) acc = goal.getTargetAmount();
            double pct = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0 ? acc.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0 : 100.0;
            milestones.add(SavingsPlanResponse.MilestoneItem.builder().month(m).targetAccumulated(acc).completionPercentage(pct).build());
        }

        String report = String.format("## 🎯 AI Savings Roadmap: %s\n\n" +
                "### 📊 Feasibility Summary\n" +
                "- **Target Goal**: Rs. %s over %d months\n" +
                "- **Required Monthly Savings**: **Rs. %s / month**\n" +
                "- **Current Monthly Surplus**: Rs. %s / month\n" +
                "- **Status**: **%s**\n\n" +
                "### 💡 Smart Tactics:\n" +
                "1. Automate an automatic transfer of Rs. %s on your payday into a dedicated savings account.\n" +
                "2. Maintain strict caps on discretionary dining and leisure to preserve your monthly surplus.\n",
                goal.getTitle(), goal.getTargetAmount(), targetMonths, monthlyRequired, monthlySurplus, status, monthlyRequired);

        return SavingsPlanResponse.builder()
                .goalTitle(goal.getTitle())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO)
                .targetMonths(targetMonths)
                .monthlyRequiredSavings(monthlyRequired)
                .monthlySurplus(monthlySurplus)
                .feasibilityScore(score)
                .feasibilityStatus(status)
                .difficultyLevel(coverage >= 1.0 ? "LOW" : coverage >= 0.7 ? "MEDIUM" : "HIGH")
                .milestones(milestones)
                .aiStrategyReport(report)
                .build();
    }
}
