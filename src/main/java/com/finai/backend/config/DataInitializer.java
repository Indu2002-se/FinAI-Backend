package com.finai.backend.config;

import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.QuizDifficulty;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.QuizRepository;
import com.finai.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer
 * Initializes default roles and financial literacy quizzes on application startup
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final QuizRepository quizRepository;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Initialize roles if not exists
            for (RoleType roleType : RoleType.values()) {
                if (roleRepository.findByName(roleType).isEmpty()) {
                    Role role = Role.builder()
                            .name(roleType)
                            .build();
                    roleRepository.save(role);
                    log.info("Created role: {}", roleType.name());
                }
            }

            // Seed quizzes if none exist
            if (quizRepository.count() == 0) {
                seedQuizzes();
                log.info("Seeded financial literacy quizzes");
            }

            log.info("Data initialization completed");
        };
    }

    private void seedQuizzes() {
        // Quiz 1: Budgeting Basics
        Quiz budgetingQuiz = Quiz.builder()
                .title("Budgeting Basics")
                .category("Budgeting")
                .description("Learn the fundamentals of creating and managing a budget!")
                .difficulty(QuizDifficulty.BEGINNER)
                .rewardPoints(50)
                .badgeUrl("badge_budget_star")
                .icon("budget_icon")
                .build();

        addQuestion(budgetingQuiz, "What is a budget?", 1,
                "A budget is a plan for how you will spend and save your money.",
                new String[]{"A way to count your toys", "A plan for spending and saving money", "A type of piggy bank", "A game you play with coins"},
                1);

        addQuestion(budgetingQuiz, "Why is it important to save money?", 2,
                "Saving money helps you afford things in the future and handle emergencies.",
                new String[]{"So you can buy things you want later", "It is not important", "Only adults need to save", "Money disappears if not spent"},
                0);

        addQuestion(budgetingQuiz, "What should you do FIRST when you receive pocket money?", 3,
                "The best habit is to set aside savings before spending on wants.",
                new String[]{"Save a portion", "Spend it all on sweets", "Give it all away", "Hide it under your bed"},
                0);

        addQuestion(budgetingQuiz, "What is the difference between NEEDS and WANTS?", 4,
                "Needs are essentials like food and shelter. Wants are things you'd like but don't need.",
                new String[]{"There is no difference", "Needs are things you must have; wants are extras", "Wants are more important than needs", "Needs are only for adults"},
                1);

        addQuestion(budgetingQuiz, "If you earn Rs.1000 pocket money and save 20%, how much do you save?", 5,
                "20% of Rs.1000 = Rs.200. Saving a percentage is a great budgeting habit!",
                new String[]{"Rs.100", "Rs.200", "Rs.500", "Rs.20"},
                1);

        quizRepository.save(budgetingQuiz);

        // Quiz 2: Saving Smart
        Quiz savingQuiz = Quiz.builder()
                .title("Saving Smart")
                .category("Saving")
                .description("Discover clever ways to save money and reach your goals!")
                .difficulty(QuizDifficulty.BEGINNER)
                .rewardPoints(50)
                .badgeUrl("badge_saving_champ")
                .icon("savings_icon")
                .build();

        addQuestion(savingQuiz, "What is a savings goal?", 1,
                "A savings goal is a specific amount you want to save for something important.",
                new String[]{"Something you spend money on immediately", "A specific target amount to save for", "A type of bank account", "Money your parents give you"},
                1);

        addQuestion(savingQuiz, "Which is the BEST way to save money?", 2,
                "Setting aside a fixed amount regularly is the most effective saving strategy.",
                new String[]{"Save whatever is left at month end", "Set aside a fixed amount every week", "Only save when you feel like it", "Wait until you're older to start saving"},
                1);

        addQuestion(savingQuiz, "What is compound interest?", 3,
                "Compound interest means you earn interest on your interest, helping money grow faster!",
                new String[]{"Interest you pay on a loan", "Earning interest on your interest", "A type of savings account", "Money you lose each year"},
                1);

        addQuestion(savingQuiz, "If you save Rs.100 every week, how much will you have after 10 weeks?", 4,
                "Rs.100 × 10 weeks = Rs.1,000. Regular saving adds up fast!",
                new String[]{"Rs.500", "Rs.1,000", "Rs.100", "Rs.10,000"},
                1);

        addQuestion(savingQuiz, "What is an emergency fund?", 5,
                "An emergency fund is money saved for unexpected expenses or situations.",
                new String[]{"Money for buying games", "Money saved for emergencies and unexpected expenses", "A savings plan for holidays", "The money in your wallet right now"},
                1);

        quizRepository.save(savingQuiz);

        // Quiz 3: Needs vs Wants
        Quiz needsWantsQuiz = Quiz.builder()
                .title("Needs vs Wants Challenge")
                .category("Needs vs Wants")
                .description("Can you tell the difference between needs and wants? Let's find out!")
                .difficulty(QuizDifficulty.BEGINNER)
                .rewardPoints(50)
                .badgeUrl("badge_wise_spender")
                .icon("balance_icon")
                .build();

        addQuestion(needsWantsQuiz, "Which of these is a NEED?", 1,
                "Food is essential for survival — it's a basic need!",
                new String[]{"A video game", "Food", "A toy car", "Ice cream"},
                1);

        addQuestion(needsWantsQuiz, "Which of these is a WANT?", 2,
                "A designer backpack is nice to have but not essential — it's a want!",
                new String[]{"School uniform", "Drinking water", "A designer backpack", "Shelter"},
                2);

        addQuestion(needsWantsQuiz, "Is a mobile phone a need or a want?", 3,
                "A basic phone for communication can be a need, but the latest smartphone is usually a want.",
                new String[]{"Always a need", "Always a want", "It depends on how it's used", "Neither"},
                2);

        addQuestion(needsWantsQuiz, "Your friend wants you to buy an expensive toy. What should you think about first?", 4,
                "Smart spending means asking yourself if you really need it and if it fits your budget.",
                new String[]{"Buy it immediately to impress your friend", "Think about whether you need it and can afford it", "Borrow money to buy it", "Feel bad because you can't afford it"},
                1);

        addQuestion(needsWantsQuiz, "Why is it important to prioritize needs over wants?", 5,
                "Meeting needs first ensures your essentials are covered before spending on extras.",
                new String[]{"It's not important", "So you always have money for essentials", "Because wants are bad", "Because adults told you so"},
                1);

        quizRepository.save(needsWantsQuiz);

        // Quiz 4: Money Math (Intermediate)
        Quiz moneyMathQuiz = Quiz.builder()
                .title("Money Math Master")
                .category("Money Math")
                .description("Test your math skills with real-world money problems!")
                .difficulty(QuizDifficulty.INTERMEDIATE)
                .rewardPoints(75)
                .badgeUrl("badge_math_wizard")
                .icon("calculator_icon")
                .build();

        addQuestion(moneyMathQuiz, "You have Rs.500 and spend Rs.120 on lunch. How much is left?", 1,
                "Rs.500 - Rs.120 = Rs.380",
                new String[]{"Rs.380", "Rs.320", "Rs.400", "Rs.280"},
                0);

        addQuestion(moneyMathQuiz, "A toy costs Rs.350. You have Rs.200 saved. How much more do you need?", 2,
                "Rs.350 - Rs.200 = Rs.150 more needed.",
                new String[]{"Rs.100", "Rs.150", "Rs.200", "Rs.50"},
                1);

        addQuestion(moneyMathQuiz, "If you save Rs.50 per day for a month (30 days), how much will you have?", 3,
                "Rs.50 × 30 = Rs.1,500",
                new String[]{"Rs.1,000", "Rs.1,500", "Rs.1,200", "Rs.500"},
                1);

        addQuestion(moneyMathQuiz, "You earn Rs.3,000 allowance. You give 10% to charity. How much is that?", 4,
                "10% of Rs.3,000 = Rs.300",
                new String[]{"Rs.30", "Rs.100", "Rs.300", "Rs.3,000"},
                2);

        addQuestion(moneyMathQuiz, "Three friends share a Rs.900 pizza bill equally. How much does each pay?", 5,
                "Rs.900 ÷ 3 = Rs.300 each.",
                new String[]{"Rs.200", "Rs.250", "Rs.300", "Rs.450"},
                2);

        quizRepository.save(moneyMathQuiz);
    }

    private void addQuestion(Quiz quiz, String questionText, int order, String explanation,
                              String[] optionTexts, int correctIndex) {
        QuizQuestion question = QuizQuestion.builder()
                .quiz(quiz)
                .questionText(questionText)
                .explanation(explanation)
                .orderIndex(order)
                .options(new ArrayList<>())
                .build();

        for (int i = 0; i < optionTexts.length; i++) {
            QuizOption option = QuizOption.builder()
                    .question(question)
                    .optionText(optionTexts[i])
                    .isCorrect(i == correctIndex)
                    .build();
            question.getOptions().add(option);
        }

        quiz.getQuestions().add(question);
    }
}
