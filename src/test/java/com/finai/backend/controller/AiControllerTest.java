package com.finai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.ExpenseCategory;
import com.finai.backend.entity.enums.GoalStatus;
import com.finai.backend.entity.enums.IncomeCategory;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.*;
import com.finai.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private FinancialPredictionRepository financialPredictionRepository;

    @Autowired
    private ExpenseForecastRepository expenseForecastRepository;

    @Autowired
    private AiRecommendationRepository aiRecommendationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String bearerToken;
    private SavingsGoal testGoal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "ai-test-" + suffix + "@example.com";

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_USER).build()));

        testUser = User.builder()
                .firstName("Finance")
                .lastName("Tester")
                .email(testEmail)
                .password(passwordEncoder.encode("Password123!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .profileComplete(true)
                .build();
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);

        // User Profile
        UserProfile profile = UserProfile.builder()
                .user(testUser)
                .monthlyIncome(new BigDecimal("120000.00"))
                .monthlyExpense(new BigDecimal("65000.00"))
                .savingsGoal(new BigDecimal("50000.00"))
                .totalDebt(new BigDecimal("10000.00"))
                .householdSize(3)
                .dependentsCount(1)
                .age(32)
                .gender("Male")
                .maritalStatus("Married")
                .education("Bachelor Degree")
                .creditScore(720)
                .build();
        userProfileRepository.save(profile);

        // Seed Income Ledger Entry
        Income income = Income.builder()
                .user(testUser)
                .source("Tech Company")
                .amount(new BigDecimal("120000.00"))
                .category(IncomeCategory.SALARY)
                .incomeDate(LocalDate.now())
                .isRecurring(true)
                .build();
        incomeRepository.save(income);

        // Seed Expense Ledger Entries
        Expense foodExp = Expense.builder()
                .user(testUser)
                .description("Supermarket")
                .amount(new BigDecimal("25000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build();
        expenseRepository.save(foodExp);

        Expense utilExp = Expense.builder()
                .user(testUser)
                .description("Utilities and Electricity")
                .amount(new BigDecimal("40000.00"))
                .category(ExpenseCategory.UTILITIES)
                .expenseDate(LocalDate.now())
                .build();
        expenseRepository.save(utilExp);

        // Seed Savings Goal
        testGoal = SavingsGoal.builder()
                .user(testUser)
                .title("Emergency Fund")
                .targetAmount(new BigDecimal("180000.00"))
                .currentAmount(new BigDecimal("30000.00"))
                .deadline(LocalDate.now().plusMonths(6))
                .category("Emergency")
                .status(GoalStatus.IN_PROGRESS)
                .build();
        testGoal = savingsGoalRepository.save(testGoal);

        // Generate valid JWT Bearer token for testUser
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(testUser.getEmail())
                        .password(testUser.getPassword())
                        .authorities("ROLE_USER")
                        .build();
        bearerToken = "Bearer " + jwtService.generateToken(userDetails);
    }

    /**
     * TC003 – Unauthenticated Protected AI Endpoint
     * Verify a protected AI endpoint rejects an unauthenticated request.
     */
    @Test
    @DisplayName("TC003: analyzeWithoutBearerTokenShouldRejectRequest")
    void analyzeWithoutBearerTokenShouldRejectRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ai/analyze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * TC004 – Financial Risk Prediction
     * Verify financial risk prediction service returns a valid risk result.
     */
    @Test
    @DisplayName("TC004: riskPredictionWithValidFinancialDataShouldReturnRiskResult")
    void riskPredictionWithValidFinancialDataShouldReturnRiskResult() throws Exception {
        mockMvc.perform(post("/api/v1/ai/risk")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.financialHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data.riskLevel", notNullValue()))
                .andExpect(jsonPath("$.data.riskProbability", notNullValue()))
                .andExpect(jsonPath("$.data.topDriver", notNullValue()))
                .andExpect(jsonPath("$.data.topDriverReadable", notNullValue()))
                .andExpect(jsonPath("$.data.drivers", notNullValue()));
    }

    /**
     * TC005 – Six-Month Expense Forecast
     * Verify six-month household expense forecast generation.
     */
    @Test
    @DisplayName("TC005: forecastWithValidFinancialDataShouldReturnForecast")
    void forecastWithValidFinancialDataShouldReturnForecast() throws Exception {
        mockMvc.perform(post("/api/v1/ai/forecast")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.forecastMonths", is(6)))
                .andExpect(jsonPath("$.data.total", hasSize(6)))
                .andExpect(jsonPath("$.data.food", hasSize(6)))
                .andExpect(jsonPath("$.data.nonFood", hasSize(6)))
                .andExpect(jsonPath("$.data.total[0].date", notNullValue()))
                .andExpect(jsonPath("$.data.total[0].predictedAmount", notNullValue()));
    }

    /**
     * TC006 – Personalized Financial Recommendation
     * Verify personalized financial recommendation generation.
     */
    @Test
    @DisplayName("TC006: recommendationWithValidFinancialDataShouldReturnRecommendation")
    void recommendationWithValidFinancialDataShouldReturnRecommendation() throws Exception {
        mockMvc.perform(post("/api/v1/ai/recommendation")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.category", notNullValue()))
                .andExpect(jsonPath("$.data.topDriver", notNullValue()))
                .andExpect(jsonPath("$.data.recommendationText", notNullValue()))
                .andExpect(jsonPath("$.data.actionItems", notNullValue()))
                .andExpect(jsonPath("$.data.actionItems", not(empty())));
    }

    /**
     * TC007 – Full Financial Analysis
     * Verify the full analysis endpoint triggers the required analysis components.
     */
    @Test
    @DisplayName("TC007: fullAnalysisWithValidFinancialDataShouldReturnCompleteAnalysis")
    void fullAnalysisWithValidFinancialDataShouldReturnCompleteAnalysis() throws Exception {
        mockMvc.perform(post("/api/v1/ai/analyze")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("AI financial analysis completed successfully")))
                .andExpect(jsonPath("$.data.risk", notNullValue()))
                .andExpect(jsonPath("$.data.risk.financialHealthScore", notNullValue()))
                .andExpect(jsonPath("$.data.risk.riskLevel", notNullValue()))
                .andExpect(jsonPath("$.data.forecast", notNullValue()))
                .andExpect(jsonPath("$.data.forecast.forecastMonths", is(6)))
                .andExpect(jsonPath("$.data.forecast.total", hasSize(6)))
                .andExpect(jsonPath("$.data.recommendation", notNullValue()))
                .andExpect(jsonPath("$.data.recommendation.category", notNullValue()))
                .andExpect(jsonPath("$.data.recommendation.recommendationText", notNullValue()));
    }

    /**
     * TC008 – Latest Persisted AI Results
     * Verify latest persisted AI results can be retrieved.
     */
    @Test
    @DisplayName("TC008: latestResultsShouldReturnLatestPersistedAiResults")
    void latestResultsShouldReturnLatestPersistedAiResults() throws Exception {
        // Pre-populate persisted AI results for user
        FinancialPrediction prediction = FinancialPrediction.builder()
                .user(testUser)
                .financialHealthScore(new BigDecimal("82.00"))
                .riskLevel("Low Risk")
                .riskProbability(new BigDecimal("0.18"))
                .topDriver("expense_to_income_ratio")
                .topDriverReadable("Expense-to-Income Ratio")
                .explanationJson("[]")
                .build();
        financialPredictionRepository.save(prediction);

        ExpenseForecast forecast = ExpenseForecast.builder()
                .user(testUser)
                .forecastDate(LocalDate.now().plusMonths(1))
                .foodAmount(new BigDecimal("22000.00"))
                .nonFoodAmount(new BigDecimal("35000.00"))
                .totalAmount(new BigDecimal("57000.00"))
                .lowerBound(new BigDecimal("52000.00"))
                .upperBound(new BigDecimal("62000.00"))
                .build();
        expenseForecastRepository.save(forecast);

        AiRecommendation recommendation = AiRecommendation.builder()
                .user(testUser)
                .category("Savings Optimization")
                .topDriver("expense_to_income_ratio")
                .recommendationText("Consider increasing automatic savings transfers.")
                .actionItemsJson("[\"Automate 10% monthly savings transfer\"]")
                .isApplied(false)
                .build();
        aiRecommendationRepository.save(recommendation);

        mockMvc.perform(get("/api/v1/ai/latest")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.risk.riskLevel", is("Low Risk")))
                .andExpect(jsonPath("$.data.forecast.total[0].predictedAmount", is(57000.0)))
                .andExpect(jsonPath("$.data.recommendation.category", is("Savings Optimization")));
    }

    /**
     * TC009 – Savings Plan Generation
     * Verify savings-plan generation for a valid savings goal.
     */
    @Test
    @DisplayName("TC009: savingsPlanWithValidGoalShouldReturnPlan")
    void savingsPlanWithValidGoalShouldReturnPlan() throws Exception {
        mockMvc.perform(get("/api/v1/ai/savings-plan/" + testGoal.getId())
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.goalTitle", is("Emergency Fund")))
                .andExpect(jsonPath("$.data.targetAmount", is(180000.0)))
                .andExpect(jsonPath("$.data.currentAmount", is(30000.0)))
                .andExpect(jsonPath("$.data.targetMonths", notNullValue()))
                .andExpect(jsonPath("$.data.monthlyRequiredSavings", notNullValue()))
                .andExpect(jsonPath("$.data.feasibilityScore", notNullValue()))
                .andExpect(jsonPath("$.data.feasibilityStatus", notNullValue()))
                .andExpect(jsonPath("$.data.milestones", notNullValue()))
                .andExpect(jsonPath("$.data.milestones", not(empty())))
                .andExpect(jsonPath("$.data.aiStrategyReport", notNullValue()));
    }

    /**
     * TC012 – Invalid/Missing Financial Data Handling
     * Verify service handles non-existent goal or missing data without unhandled crashes.
     */
    @Test
    @DisplayName("TC012: analysisWithInvalidFinancialDataShouldReturnControlledError")
    void analysisWithInvalidFinancialDataShouldReturnControlledError() throws Exception {
        Long nonExistentGoalId = 999999L;

        mockMvc.perform(get("/api/v1/ai/savings-plan/" + nonExistentGoalId)
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Savings Goal not found")));
    }
}
