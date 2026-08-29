package com.finai.backend.service;

import com.finai.backend.dto.response.AiAnalysisResponse;
import com.finai.backend.dto.response.ExpenseForecastResponse;
import com.finai.backend.dto.response.FinancialRiskResponse;
import com.finai.backend.dto.response.SavingsPlanResponse;
import com.finai.backend.entity.Expense;
import com.finai.backend.entity.SavingsGoal;
import com.finai.backend.entity.User;
import com.finai.backend.entity.UserProfile;
import com.finai.backend.entity.enums.ExpenseCategory;
import com.finai.backend.entity.enums.GoalStatus;
import com.finai.backend.entity.enums.InferenceSource;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.*;
import com.finai.backend.service.interfaces.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(com.finai.backend.entity.Role.builder().name(RoleType.ROLE_USER).build()));

        testUser = User.builder()
                .firstName("Alice")
                .lastName("Walker")
                .email("alice-" + suffix + "@example.com")
                .password(passwordEncoder.encode("Pass12345!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .profileComplete(true)
                .build();
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);

        UserProfile profile = UserProfile.builder()
                .user(testUser)
                .monthlyIncome(new BigDecimal("150000.00"))
                .monthlyExpense(new BigDecimal("70000.00"))
                .savingsGoal(new BigDecimal("100000.00"))
                .totalDebt(new BigDecimal("20000.00"))
                .householdSize(4)
                .dependentsCount(2)
                .age(36)
                .gender("Female")
                .creditScore(750)
                .build();
        userProfileRepository.save(profile);

        // Seed 3 months of expenses for testUser
        expenseRepository.save(Expense.builder()
                .user(testUser)
                .description("Food M0")
                .amount(new BigDecimal("25000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build());
        expenseRepository.save(Expense.builder()
                .user(testUser)
                .description("Food M-1")
                .amount(new BigDecimal("24000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now().minusMonths(1))
                .build());
        expenseRepository.save(Expense.builder()
                .user(testUser)
                .description("Food M-2")
                .amount(new BigDecimal("23000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now().minusMonths(2))
                .build());
    }

    @Test
    @DisplayName("getLatestRiskPrediction should generate valid financial risk response")
    void getLatestRiskPredictionShouldReturnValidResponse() {
        FinancialRiskResponse risk = aiService.getLatestRiskPrediction(testUser);

        assertNotNull(risk);
        assertNotNull(risk.getFinancialHealthScore());
        assertTrue(risk.getFinancialHealthScore().doubleValue() >= 0 && risk.getFinancialHealthScore().doubleValue() <= 100);
        assertNotNull(risk.getRiskLevel());
        assertNotNull(risk.getRiskProbability());
        assertNotNull(risk.getTopDriver());
        assertNotNull(risk.getDrivers());
    }

    @Test
    @DisplayName("getLatestForecast should return 6 months forecast")
    void getLatestForecastShouldReturnSixMonths() {
        ExpenseForecastResponse forecast = aiService.getLatestForecast(testUser);

        assertNotNull(forecast);
        assertEquals(6, forecast.getForecastMonths());
        assertEquals(6, forecast.getTotal().size());
        assertEquals(6, forecast.getFood().size());
        assertEquals(6, forecast.getNonFood().size());
    }

    @Test
    @DisplayName("generateSavingsPlan should compute feasible milestones and report")
    void generateSavingsPlanShouldComputeFeasibilityAndMilestones() {
        SavingsGoal goal = SavingsGoal.builder()
                .user(testUser)
                .title("Vacation Trip")
                .targetAmount(new BigDecimal("120000.00"))
                .currentAmount(new BigDecimal("20000.00"))
                .deadline(LocalDate.now().plusMonths(6))
                .category("Travel")
                .status(GoalStatus.IN_PROGRESS)
                .build();
        goal = savingsGoalRepository.save(goal);

        SavingsPlanResponse plan = aiService.generateSavingsPlan(goal.getId(), testUser);

        assertNotNull(plan);
        assertEquals("Vacation Trip", plan.getGoalTitle());
        assertEquals(new BigDecimal("120000.00"), plan.getTargetAmount());
        assertEquals(new BigDecimal("20000.00"), plan.getCurrentAmount());
        assertNotNull(plan.getMonthlyRequiredSavings());
        assertNotNull(plan.getMonthlySurplus());
        assertNotNull(plan.getFeasibilityScore());
        assertNotNull(plan.getFeasibilityStatus());
        assertNotNull(plan.getMilestones());
        assertFalse(plan.getMilestones().isEmpty());
        assertNotNull(plan.getAiStrategyReport());
    }

    @Test
    @DisplayName("runFullAnalysis with missing user profile should return INSUFFICIENT_DATA")
    void runFullAnalysisWithMissingProfileShouldReturnInsufficientData() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User noProfileUser = User.builder()
                .firstName("Bob")
                .lastName("NoProfile")
                .email("bob-" + suffix + "@example.com")
                .password(passwordEncoder.encode("Pass12345!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .build();
        noProfileUser = userRepository.save(noProfileUser);

        AiAnalysisResponse response = aiService.runFullAnalysis(noProfileUser);

        assertNotNull(response);
        assertNotNull(response.getRisk());
        assertEquals(InferenceSource.INSUFFICIENT_DATA, response.getRisk().getInferenceSource());
        assertNotNull(response.getForecast());
        assertEquals(InferenceSource.INSUFFICIENT_HISTORY, response.getForecast().getInferenceSource());
        assertNotNull(response.getRecommendation());
        assertEquals(InferenceSource.INSUFFICIENT_DATA, response.getRecommendation().getInferenceSource());
    }

    @Test
    @DisplayName("runFullAnalysis with fewer than 3 months of expenses should return INSUFFICIENT_DATA")
    void runFullAnalysisWithInsufficientExpenseHistoryShouldReturnInsufficientData() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User limitedExpenseUser = User.builder()
                .firstName("Dan")
                .lastName("LowExpenses")
                .email("dan-" + suffix + "@example.com")
                .password(passwordEncoder.encode("Pass12345!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .build();
        limitedExpenseUser = userRepository.save(limitedExpenseUser);

        userProfileRepository.save(UserProfile.builder()
                .user(limitedExpenseUser)
                .monthlyIncome(new BigDecimal("80000.00"))
                .monthlyExpense(new BigDecimal("40000.00"))
                .householdSize(2)
                .age(29)
                .build());

        // Only 1 month of expenses seeded
        expenseRepository.save(Expense.builder()
                .user(limitedExpenseUser)
                .amount(new BigDecimal("20000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build());

        AiAnalysisResponse response = aiService.runFullAnalysis(limitedExpenseUser);

        assertNotNull(response);
        assertNotNull(response.getRisk());
        assertEquals(InferenceSource.INSUFFICIENT_DATA, response.getRisk().getInferenceSource());
    }

    @Test
    @DisplayName("runFullAnalysis with complete data and offline FastAPI returns MODEL_UNAVAILABLE")
    void runFullAnalysisWithCompleteDataReturnsModelUnavailableWhenFastApiOffline() {
        AiAnalysisResponse response = aiService.runFullAnalysis(testUser);

        assertNotNull(response);
        assertNotNull(response.getRisk());
        assertEquals(InferenceSource.MODEL_UNAVAILABLE, response.getRisk().getInferenceSource());
        assertNotNull(response.getForecast());
        assertEquals(InferenceSource.MODEL_UNAVAILABLE, response.getForecast().getInferenceSource());
        assertNotNull(response.getRecommendation());
        assertEquals(InferenceSource.MODEL_UNAVAILABLE, response.getRecommendation().getInferenceSource());
    }

    @Test
    @DisplayName("getLatestRiskPrediction without profile returns INSUFFICIENT_DATA")
    void getLatestRiskPredictionWithoutProfileReturnsInsufficientData() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User noProfileUser = User.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie-" + suffix + "@example.com")
                .password(passwordEncoder.encode("Pass12345!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .build();
        noProfileUser = userRepository.save(noProfileUser);

        FinancialRiskResponse risk = aiService.getLatestRiskPrediction(noProfileUser);

        assertNotNull(risk);
        assertEquals(InferenceSource.INSUFFICIENT_DATA, risk.getInferenceSource());
    }
}
