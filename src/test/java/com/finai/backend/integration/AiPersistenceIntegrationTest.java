package com.finai.backend.integration;

import com.finai.backend.dto.response.AiAnalysisResponse;
import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.DebtStatus;
import com.finai.backend.entity.enums.ExpenseCategory;
import com.finai.backend.entity.enums.GoalStatus;
import com.finai.backend.entity.enums.IncomeCategory;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiPersistenceIntegrationTest {

    @Autowired
    private AiService aiService;

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
    private DebtRepository debtRepository;

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

    private User testUser;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_USER).build()));

        testUser = User.builder()
                .firstName("Integration")
                .lastName("Tester")
                .email("persistence-" + suffix + "@example.com")
                .password(passwordEncoder.encode("Pass123!"))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .profileComplete(true)
                .build();
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);

        UserProfile profile = UserProfile.builder()
                .user(testUser)
                .monthlyIncome(new BigDecimal("95000.00"))
                .monthlyExpense(new BigDecimal("45000.00"))
                .savingsGoal(new BigDecimal("20000.00"))
                .totalDebt(new BigDecimal("5000.00"))
                .householdSize(2)
                .dependentsCount(0)
                .age(28)
                .gender("Female")
                .creditScore(700)
                .build();
        userProfileRepository.save(profile);

        Income income = Income.builder()
                .user(testUser)
                .source("Software Engineering")
                .amount(new BigDecimal("95000.00"))
                .category(IncomeCategory.SALARY)
                .incomeDate(LocalDate.now())
                .isRecurring(true)
                .build();
        incomeRepository.save(income);

        Expense expense = Expense.builder()
                .user(testUser)
                .description("Groceries")
                .amount(new BigDecimal("20000.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.now())
                .build();
        expenseRepository.save(expense);
    }

    /**
     * TC010 – AI Result Persistence
     * Verify generated AI results are persisted correctly.
     */
    @Test
    @DisplayName("TC010: fullAnalysisShouldPersistAiResultsCorrectly")
    void fullAnalysisShouldPersistAiResultsCorrectly() {
        AiAnalysisResponse response = aiService.runFullAnalysis(testUser);

        assertNotNull(response, "Analysis response should not be null");
        assertNotNull(response.getRisk(), "Risk response should not be null");
        assertNotNull(response.getForecast(), "Forecast response should not be null");
        assertNotNull(response.getRecommendation(), "Recommendation response should not be null");

        // Verify FinancialPrediction persistence
        Optional<FinancialPrediction> persistedRisk = financialPredictionRepository.findFirstByUserOrderByCreatedAtDesc(testUser);
        assertTrue(persistedRisk.isPresent(), "FinancialPrediction should be persisted in database");
        assertEquals(testUser.getId(), persistedRisk.get().getUser().getId());
        assertNotNull(persistedRisk.get().getFinancialHealthScore());
        assertNotNull(persistedRisk.get().getRiskLevel());

        // Verify ExpenseForecast persistence
        List<ExpenseForecast> persistedForecasts = expenseForecastRepository.findByUserOrderByForecastDateAsc(testUser);
        assertFalse(persistedForecasts.isEmpty(), "ExpenseForecast records should be persisted in database");
        assertEquals(6, persistedForecasts.size(), "Should persist exactly 6 monthly forecast records");
        for (ExpenseForecast ef : persistedForecasts) {
            assertEquals(testUser.getId(), ef.getUser().getId());
            assertNotNull(ef.getForecastDate());
            assertNotNull(ef.getTotalAmount());
        }

        // Verify AiRecommendation persistence
        Optional<AiRecommendation> persistedRec = aiRecommendationRepository.findFirstByUserOrderByCreatedAtDesc(testUser);
        assertTrue(persistedRec.isPresent(), "AiRecommendation should be persisted in database");
        assertEquals(testUser.getId(), persistedRec.get().getUser().getId());
        assertNotNull(persistedRec.get().getCategory());
        assertNotNull(persistedRec.get().getRecommendationText());
    }

    /**
     * TC011 – Service/Repository Interaction with MySQL/H2 Test Database
     * Verify service/repository interaction with test database.
     */
    @Test
    @DisplayName("TC011: serviceShouldReadAndWriteDataUsingTestDatabase")
    void serviceShouldReadAndWriteDataUsingTestDatabase() {
        // Write new Debt
        Debt debt = Debt.builder()
                .user(testUser)
                .debtName("Credit Card Debt")
                .source("Credit Card Bank")
                .totalAmount(new BigDecimal("15000.00"))
                .remainingAmount(new BigDecimal("12000.00"))
                .interestRate(new BigDecimal("18.5"))
                .monthlyPayment(new BigDecimal("1500.00"))
                .dueDate(LocalDate.now().plusMonths(12))
                .status(DebtStatus.ACTIVE)
                .isCreditCard(true)
                .build();
        Debt savedDebt = debtRepository.save(debt);
        assertNotNull(savedDebt.getId(), "Saved debt should have generated ID");

        // Read Debt
        List<Debt> userDebts = debtRepository.findByUser(testUser);
        assertEquals(1, userDebts.size());
        assertEquals("Credit Card Bank", userDebts.get(0).getSource());
        assertEquals("Credit Card Debt", userDebts.get(0).getDebtName());

        // Write Savings Goal
        SavingsGoal goal = SavingsGoal.builder()
                .user(testUser)
                .title("New Laptop")
                .targetAmount(new BigDecimal("250000.00"))
                .currentAmount(new BigDecimal("50000.00"))
                .deadline(LocalDate.now().plusMonths(5))
                .category("Electronics")
                .status(GoalStatus.IN_PROGRESS)
                .build();
        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        assertNotNull(savedGoal.getId(), "Saved goal should have generated ID");

        // Read Savings Goal
        Optional<SavingsGoal> retrievedGoal = savingsGoalRepository.findById(savedGoal.getId());
        assertTrue(retrievedGoal.isPresent());
        assertEquals("New Laptop", retrievedGoal.get().getTitle());
        assertEquals(new BigDecimal("250000.00"), retrievedGoal.get().getTargetAmount());

        // Update Goal
        retrievedGoal.get().setCurrentAmount(new BigDecimal("75000.00"));
        savingsGoalRepository.save(retrievedGoal.get());

        SavingsGoal updatedGoal = savingsGoalRepository.findById(savedGoal.getId()).orElseThrow();
        assertEquals(new BigDecimal("75000.00"), updatedGoal.getCurrentAmount());
    }
}
