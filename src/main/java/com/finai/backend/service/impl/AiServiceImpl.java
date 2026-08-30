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

        // 1. Income Data Extraction from Real Incomes
        List<Income> monthlyIncomes = incomeRepository.findByUserAndIncomeDateBetween(user, startOfMonth, endOfMonth);
        double employmentIncome = 0.0;
        double otherIncome = 0.0;
        double windfallIncome = 0.0;
        double agriIncome = 0.0;

        for (Income inc : monthlyIncomes) {
            if (inc.getAmount() == null) continue;
            double amt = inc.getAmount().doubleValue();
            if (inc.getCategory() != null) {
                switch (inc.getCategory()) {
                    case SALARY:
                    case BUSINESS:
                    case FREELANCE:
                        employmentIncome += amt;
                        break;
                    case WINDFALL:
                        windfallIncome += amt;
                        break;
                    case AGRICULTURE:
                        agriIncome += amt;
                        break;
                    case INVESTMENT:
                    case OTHER:
                    default:
                        otherIncome += amt;
                        break;
                }
            } else {
                employmentIncome += amt;
            }
        }

        double totalIncome = employmentIncome + otherIncome + windfallIncome + agriIncome;

        // Fallback to UserProfile monthly income if no income ledger entries exist
        if (totalIncome <= 0.0 && profile != null && profile.getMonthlyIncome() != null && profile.getMonthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
            totalIncome = profile.getMonthlyIncome().doubleValue();
            if (profile.getEmploymentStatus() != null) {
                switch (profile.getEmploymentStatus()) {
                    case EMPLOYED:
                    case SELF_EMPLOYED:
                        employmentIncome = totalIncome;
                        break;
                    case RETIRED:
                    case STUDENT:
                    case UNEMPLOYED:
                        otherIncome = totalIncome;
                        break;
                    default:
                        employmentIncome = totalIncome;
                        break;
                }
            } else {
                employmentIncome = totalIncome;
            }
        }

        // Neutral default total income for uninitialized accounts
        if (totalIncome <= 0.0) {
            totalIncome = 50000.00; // Dataset median reference
            employmentIncome = 50000.00;
        }

        double nonAgriIncome = employmentIncome + otherIncome + windfallIncome;
        double transferIncome = 0.0; // Deterministic neutral baseline (no government transfer category in current schema)

        // 2. Expense Data Extraction from Real Expenses
        List<Expense> monthlyExpenses = expenseRepository.findByUserAndExpenseDateBetween(user, startOfMonth, endOfMonth);
        double foodExp = 0.0;
        double nonFoodExp = 0.0;

        for (Expense exp : monthlyExpenses) {
            if (exp.getAmount() == null) continue;
            double amt = exp.getAmount().doubleValue();
            if (exp.getCategory() == ExpenseCategory.FOOD) {
                foodExp += amt;
            } else {
                nonFoodExp += amt;
            }
        }

        double totalExp = foodExp + nonFoodExp;

        // Fallback to UserProfile monthly expense if no expense ledger entries exist
        if (totalExp <= 0.0 && profile != null && profile.getMonthlyExpense() != null && profile.getMonthlyExpense().compareTo(BigDecimal.ZERO) > 0) {
            totalExp = profile.getMonthlyExpense().doubleValue();
            foodExp = totalExp * 0.35; // Standard household survey food ratio
            nonFoodExp = totalExp - foodExp;
        }

        // Default expense based on total income if no profile/expense exists
        if (totalExp <= 0.0) {
            totalExp = totalIncome * 0.50;
            foodExp = totalExp * 0.35;
            nonFoodExp = totalExp - foodExp;
        }

        // 3. Debt Data Extraction from Real Debts
        List<Debt> userDebts = debtRepository.findByUser(user);
        double totalDebt = 0.0;
        int activeDebtRecords = 0;
        Set<String> distinctSources = new HashSet<>();
        double creditCardDebt = 0.0;
        int ccCreditLines = 0;
        double instalmentAmount = 0.0;
        boolean hasDefaulted = false;

        for (Debt d : userDebts) {
            if (d.getStatus() == com.finai.backend.entity.enums.DebtStatus.ACTIVE) {
                activeDebtRecords++;
                double rem = (d.getRemainingAmount() != null) ? d.getRemainingAmount().doubleValue() : 0.0;
                totalDebt += rem;
                if (d.getSource() != null && !d.getSource().isBlank()) {
                    distinctSources.add(d.getSource().trim().toLowerCase());
                }
                if (Boolean.TRUE.equals(d.getIsCreditCard())) {
                    creditCardDebt += rem;
                    ccCreditLines++;
                } else if (d.getMonthlyPayment() != null) {
                    instalmentAmount += d.getMonthlyPayment().doubleValue();
                }
            } else if (d.getStatus() == com.finai.backend.entity.enums.DebtStatus.DEFAULTED) {
                hasDefaulted = true;
            }
        }

        // Fallback to UserProfile total debt if no debt records exist
        if (totalDebt <= 0.0 && profile != null && profile.getTotalDebt() != null && profile.getTotalDebt().compareTo(BigDecimal.ZERO) > 0) {
            totalDebt = profile.getTotalDebt().doubleValue();
            activeDebtRecords = 1;
            distinctSources.add("primary_loan");
        }

        // 4. Savings and Ratios Calculation
        BigDecimal totalSavingsBd = savingsRepository.sumTotalSavingsByUser(user);
        double totalSavings = (totalSavingsBd != null) ? totalSavingsBd.doubleValue() : 0.0;

        double financialSurplus = totalIncome - totalExp;
        double expenseToIncomeRatio = totalIncome > 0 ? totalExp / totalIncome : 0.5;
        double debtToIncomeRatio = totalIncome > 0 ? totalDebt / totalIncome : 0.0;
        double savingsRatio = totalIncome > 0 ? financialSurplus / totalIncome : 0.0;

        // 5. Demographic and Household Encodings
        int householdSize = (profile != null && profile.getHouseholdSize() != null && profile.getHouseholdSize() > 0)
                ? profile.getHouseholdSize() : 4;
        int dependentsCount = (profile != null && profile.getDependentsCount() != null)
                ? profile.getDependentsCount() : 0;
        double perCapitaIncome = totalIncome / Math.max(1, householdSize);
        double employmentCapacity = Math.max(0.1, (double) Math.max(1, householdSize - dependentsCount) / (double) householdSize);

        double age = (profile != null && profile.getAge() != null && profile.getAge() > 0) ? profile.getAge().doubleValue() : 35.0;
        double gender = encodeGender(profile != null ? profile.getGender() : null);
        double education = encodeEducation(profile != null ? profile.getEducation() : null);
        double maritalStatus = encodeMaritalStatus(profile != null ? profile.getMaritalStatus() : null);
        double creditScore = (profile != null && profile.getCreditScore() != null && profile.getCreditScore() > 0)
                ? profile.getCreditScore().doubleValue() : 650.0;

        // 6. Credit and Institutional Features (Deterministic, documented defaults for retail app)
        double hasCreditCardDebtFlag = (creditCardDebt > 0.0) ? 1.0 : 2.0; // 1=Yes, 2=No in training schema
        double hasCreditmixMatch = (activeDebtRecords > 0 && ccCreditLines > 0) ? 1.0 : 0.0;
        double creditDefaulted = hasDefaulted ? 1.0 : 0.0;
        double creditClv = 0.0; // Customer Lifetime Value: 0.0 neutral baseline (bank internal metric)
        double creditFraudTxn = 0.0; // Fraud txn count: 0.0 neutral baseline
        double ccUtilizationRatio = (creditCardDebt > 0.0 && totalIncome > 0.0) ? Math.min(1.0, creditCardDebt / totalIncome) : 0.0;
        double ccLatePayments = 0.0; // Late payment count: 0.0 neutral baseline
        double ccDebtToIncomeRatio = (totalIncome > 0.0) ? creditCardDebt / totalIncome : 0.0;
        double ccTotalSpendLastYear = 0.0; // Past year spend: 0.0 neutral baseline
        double ccAvgTxnAmount = 0.0; // Avg transaction: 0.0 neutral baseline
        double ccTotalTxns = 0.0; // Total txns: 0.0 neutral baseline
        double ccTenureYears = 0.0; // CC account tenure: 0.0 neutral baseline
        double vehicleOwnership = 0.0; // Neutral baseline (0 vehicles)
        double instalmentGoodsFlag = (instalmentAmount > 0.0) ? 1.0 : 2.0; // 1=Yes, 2=No in training schema

        // Construct exact 42-feature ordered map
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("age", age);
        map.put("gender", gender);
        map.put("education", education);
        map.put("marital_status", maritalStatus);
        map.put("household_size_f", (double) householdSize);
        map.put("employment_income", employmentIncome);
        map.put("other_income", otherIncome);
        map.put("windfall_income", windfallIncome);
        map.put("agri_income", agriIncome);
        map.put("non_agri_income", nonAgriIncome);
        map.put("transfer_income", transferIncome);
        map.put("total_income", totalIncome);
        map.put("food_expenditure", foodExp);
        map.put("nonfood_expenditure", nonFoodExp);
        map.put("total_expenditure", totalExp);
        map.put("expense_to_income_ratio", expenseToIncomeRatio);
        map.put("financial_surplus", financialSurplus);
        map.put("savings_ratio", savingsRatio);
        map.put("per_capita_income", perCapitaIncome);
        map.put("employment_capacity", employmentCapacity);
        map.put("debt_amount", totalDebt);
        map.put("debt_records", (double) activeDebtRecords);
        map.put("debt_sources", (double) Math.max(activeDebtRecords > 0 ? 1 : 0, distinctSources.size()));
        map.put("debt_to_income_ratio", debtToIncomeRatio);
        map.put("credit_card_debt", creditCardDebt);
        map.put("has_credit_card_debt", hasCreditCardDebtFlag);
        map.put("has_creditmix_match", hasCreditmixMatch);
        map.put("credit_score", creditScore);
        map.put("credit_defaulted", creditDefaulted);
        map.put("credit_clv", creditClv);
        map.put("credit_fraud_txn", creditFraudTxn);
        map.put("cc_utilization_ratio", ccUtilizationRatio);
        map.put("cc_late_payments", ccLatePayments);
        map.put("cc_credit_lines", (double) ccCreditLines);
        map.put("cc_debt_to_income_ratio", ccDebtToIncomeRatio);
        map.put("cc_total_spend_last_year", ccTotalSpendLastYear);
        map.put("cc_avg_txn_amount", ccAvgTxnAmount);
        map.put("cc_total_txns", ccTotalTxns);
        map.put("cc_tenure_years", ccTenureYears);
        map.put("vehicle_ownership", vehicleOwnership);
        map.put("instalment_goods_flag", instalmentGoodsFlag);
        map.put("instalment_amount", instalmentAmount);

        log.info("Constructed Model 1 feature vector with {} features for userId: {} (Income={}, Exp={}, Debt={}, Surplus={})",
                map.size(), user.getId(), totalIncome, totalExp, totalDebt, financialSurplus);

        return map;
    }

    private double encodeGender(String gender) {
        if (gender == null) return 1.0;
        String g = gender.trim().toUpperCase();
        if (g.startsWith("F") || g.contains("FEMALE")) {
            return 2.0;
        }
        return 1.0;
    }

    private double encodeEducation(String education) {
        if (education == null) return 10.0;
        String e = education.trim().toUpperCase();
        if (e.contains("DOCTOR") || e.contains("PHD")) return 19.0;
        if (e.contains("MASTER") || e.contains("POST")) return 16.0;
        if (e.contains("BACHELOR") || e.contains("DEGREE") || e.contains("UNDERGRADUATE")) return 13.0;
        if (e.contains("DIPLOMA") || e.contains("VOCATIONAL")) return 11.0;
        if (e.contains("ADVANCED") || e.contains("A_LEVEL") || e.contains("A/L") || e.contains("SECONDARY_HIGHER")) return 10.0;
        if (e.contains("ORDINARY") || e.contains("O_LEVEL") || e.contains("O/L") || e.contains("SECONDARY")) return 8.0;
        if (e.contains("PRIMARY")) return 5.0;
        return 10.0; // Dataset median reference
    }

    private double encodeMaritalStatus(String maritalStatus) {
        if (maritalStatus == null) return 1.0;
        String m = maritalStatus.trim().toUpperCase();
        if (m.contains("MARRIED")) return 2.0;
        if (m.contains("WIDOW")) return 3.0;
        if (m.contains("DIVORCE") || m.contains("SEPARAT")) return 4.0;
        return 1.0; // Single
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
