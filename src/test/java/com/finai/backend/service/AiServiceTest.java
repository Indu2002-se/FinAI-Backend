package com.finai.backend.service;

import com.finai.backend.dto.response.ExpenseForecastResponse;
import com.finai.backend.dto.response.FinancialRiskResponse;
import com.finai.backend.dto.response.SavingsPlanResponse;
import com.finai.backend.entity.SavingsGoal;
import com.finai.backend.entity.User;
import com.finai.backend.entity.UserProfile;
import com.finai.backend.entity.enums.GoalStatus;
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
}
