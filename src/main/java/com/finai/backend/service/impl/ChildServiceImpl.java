package com.finai.backend.service.impl;

import com.finai.backend.dto.request.ChildProfileRequest;
import com.finai.backend.dto.request.QuizSubmitRequest;
import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.response.*;
import com.finai.backend.entity.*;
import com.finai.backend.entity.enums.GoalStatus;
import com.finai.backend.entity.enums.RewardType;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.exception.BadRequestException;
import com.finai.backend.exception.ResourceNotFoundException;
import com.finai.backend.repository.*;
import com.finai.backend.service.interfaces.ChildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChildServiceImpl implements ChildService {

    private final ChildProfileRepository childProfileRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<ChildProfileResponse> getChildrenForParent(User parentUser) {
        return childProfileRepository.findByParentUser(parentUser)
                .stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChildProfileResponse getChildById(Long childId, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));
        return mapToProfileResponse(child);
    }

    @Override
    @Transactional
    public ChildProfileResponse createChildProfile(ChildProfileRequest request, User parentUser) {
        User childAccount = null;
        if (request.getUsernameOrEmail() != null && !request.getUsernameOrEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getUsernameOrEmail())) {
                throw new BadRequestException("Username/email already registered for another account.");
            }

            // Validate password is provided
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new BadRequestException("Password is required for child account");
            }

            Role childRole = roleRepository.findByName(RoleType.ROLE_CHILD)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_CHILD).build()));

            // Create child user with encoded password
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            
            childAccount = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName() != null ? request.getLastName() : "Child")
                    .email(request.getUsernameOrEmail())
                    .password(encodedPassword)
                    .enabled(true)
                    .emailVerified(true)
                    .profileComplete(true)
                    .provider("LOCAL")
                    .build();
            childAccount.addRole(childRole);
            childAccount = userRepository.save(childAccount);
            
            log.info("Created child user account: {} with email: {}", childAccount.getId(), childAccount.getEmail());
        }

        ChildProfile profile = ChildProfile.builder()
                .parentUser(parentUser)
                .childUser(childAccount)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .avatar(request.getAvatar() != null ? request.getAvatar() : "avatar_default.png")
                .currentSavings(request.getInitialSavings() != null ? request.getInitialSavings() : BigDecimal.ZERO)
                .totalPoints(0)
                .build();

        ChildProfile savedProfile = childProfileRepository.save(profile);
        log.info("Created child profile: {} linked to user: {}", savedProfile.getId(), childAccount != null ? childAccount.getId() : null);
        
        return mapToProfileResponse(savedProfile);
    }

    @Override
    @Transactional
    public ChildProfileResponse updateChildProfile(Long childId, ChildProfileRequest request, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));

        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setAge(request.getAge());
        if (request.getAvatar() != null) {
            child.setAvatar(request.getAvatar());
        }

        return mapToProfileResponse(childProfileRepository.save(child));
    }

    @Override
    @Transactional
    public void deleteChildProfile(Long childId, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));
        childProfileRepository.delete(child);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getChildGoals(Long childId, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));
        return savingsGoalRepository.findByChildProfile(child)
                .stream()
                .map(this::mapToGoalResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SavingsGoalResponse createChildGoal(Long childId, SavingsGoalRequest request, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));

        SavingsGoal goal = SavingsGoal.builder()
                .user(parentUser)
                .childProfile(child)
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .deadline(request.getDeadline())
                .status(GoalStatus.IN_PROGRESS)
                .category(request.getCategory() != null ? request.getCategory() : "Toy / Reward")
                .icon(request.getIcon() != null ? request.getIcon() : "star")
                .notes(request.getNotes())
                .build();

        return mapToGoalResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateChildGoal(Long childId, Long goalId, SavingsGoalRequest request, User parentUser) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .filter(g -> g.getChildProfile() != null && g.getChildProfile().getId().equals(childId) && g.getUser().getId().equals(parentUser.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", goalId));

        goal.setTitle(request.getTitle());
        goal.setTargetAmount(request.getTargetAmount());
        if (request.getCurrentAmount() != null) {
            goal.setCurrentAmount(request.getCurrentAmount());
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
                goal.setStatus(GoalStatus.COMPLETED);
                // Unlock reward badge for child
                Reward reward = Reward.builder()
                        .childProfile(goal.getChildProfile())
                        .title("Goal Achieved: " + goal.getTitle())
                        .description("Successfully reached savings goal of Rs." + goal.getTargetAmount())
                        .badgeIcon("trophy_gold")
                        .rewardType(RewardType.SAVINGS_MILESTONE)
                        .pointsAwarded(100)
                        .unlockedAt(LocalDateTime.now())
                        .build();
                rewardRepository.save(reward);

                goal.getChildProfile().setTotalPoints(goal.getChildProfile().getTotalPoints() + 100);
                childProfileRepository.save(goal.getChildProfile());
            }
        }
        goal.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        return mapToGoalResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public ChildProfileResponse depositChildSavings(Long childId, BigDecimal amount, User parentUser) {
        ChildProfile child = childProfileRepository.findByIdAndParentUser(childId, parentUser)
                .orElseThrow(() -> new ResourceNotFoundException("ChildProfile", "id", childId));

        child.setCurrentSavings(child.getCurrentSavings().add(amount));
        return mapToProfileResponse(childProfileRepository.save(child));
    }

    @Override
    @Transactional(readOnly = true)
    public ChildDashboardResponse getChildDashboard(User user) {
        ChildProfile child = resolveChildProfile(user);

        List<SavingsGoalResponse> goals = savingsGoalRepository.findByChildProfile(child)
                .stream()
                .map(this::mapToGoalResponse)
                .collect(Collectors.toList());

        List<QuizResponse> quizzes = quizRepository.findAll()
                .stream()
                .map(q -> mapToQuizResponse(q, child))
                .collect(Collectors.toList());

        List<RewardResponse> rewards = rewardRepository.findByChildProfileOrderByUnlockedAtDesc(child)
                .stream()
                .map(this::mapToRewardResponse)
                .collect(Collectors.toList());

        return ChildDashboardResponse.builder()
                .childProfileId(child.getId())
                .childName(child.getFirstName() + (child.getLastName() != null ? " " + child.getLastName() : ""))
                .age(child.getAge())
                .avatar(child.getAvatar())
                .currentSavings(child.getCurrentSavings())
                .totalPoints(child.getTotalPoints())
                .savingsGoals(goals)
                .recommendedQuizzes(quizzes)
                .recentRewards(rewards)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getAvailableQuizzes(User user) {
        ChildProfile child = resolveChildProfile(user);
        return quizRepository.findAll()
                .stream()
                .map(q -> mapToQuizResponse(q, child))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long quizId) {
        Quiz q = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        return mapToQuizResponse(q, null);
    }

    @Override
    @Transactional
    public QuizResultResponse submitQuizAttempt(Long quizId, QuizSubmitRequest request, User user) {
        ChildProfile child = resolveChildProfile(user);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        int correctCount = 0;
        int total = quiz.getQuestions().size();

        for (QuizQuestion q : quiz.getQuestions()) {
            Long selectedOptionId = request.getAnswers().get(q.getId());
            if (selectedOptionId != null) {
                boolean isCorrect = q.getOptions().stream()
                        .anyMatch(opt -> opt.getId().equals(selectedOptionId) && Boolean.TRUE.equals(opt.getIsCorrect()));
                if (isCorrect) {
                    correctCount++;
                }
            }
        }

        double scorePct = total > 0 ? ((double) correctCount / total) * 100.0 : 0.0;
        boolean passed = scorePct >= 60.0;
        int earnedPoints = passed ? quiz.getRewardPoints() : 0;

        QuizResult result = QuizResult.builder()
                .childProfile(child)
                .quiz(quiz)
                .score(correctCount)
                .totalQuestions(total)
                .passed(passed)
                .earnedPoints(earnedPoints)
                .completedAt(LocalDateTime.now())
                .build();
        quizResultRepository.save(result);

        // If passed, update total points and grant badge
        String earnedBadge = null;
        if (passed) {
            child.setTotalPoints(child.getTotalPoints() + earnedPoints);
            childProfileRepository.save(child);

            Reward reward = Reward.builder()
                    .childProfile(child)
                    .title("Master of " + quiz.getCategory())
                    .description("Passed " + quiz.getTitle() + " with " + correctCount + "/" + total + " score!")
                    .badgeIcon(quiz.getBadgeUrl() != null ? quiz.getBadgeUrl() : "badge_star")
                    .rewardType(RewardType.QUIZ_COMPLETION)
                    .pointsAwarded(earnedPoints)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            rewardRepository.save(reward);
            earnedBadge = reward.getTitle();
        }

        return QuizResultResponse.builder()
                .id(result.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .score(correctCount)
                .totalQuestions(total)
                .scorePercentage(scorePct)
                .passed(passed)
                .earnedPoints(earnedPoints)
                .earnedBadge(earnedBadge)
                .completedAt(result.getCompletedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardResponse> getChildRewards(User user) {
        ChildProfile child = resolveChildProfile(user);
        return rewardRepository.findByChildProfileOrderByUnlockedAtDesc(child)
                .stream()
                .map(this::mapToRewardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getChildQuizHistory(User user) {
        ChildProfile child = resolveChildProfile(user);
        return quizResultRepository.findByChildProfileOrderByCompletedAtDesc(child)
                .stream()
                .map(r -> QuizResultResponse.builder()
                        .id(r.getId())
                        .quizId(r.getQuiz().getId())
                        .quizTitle(r.getQuiz().getTitle())
                        .score(r.getScore())
                        .totalQuestions(r.getTotalQuestions())
                        .scorePercentage(r.getTotalQuestions() > 0 ? ((double) r.getScore() / r.getTotalQuestions()) * 100.0 : 0.0)
                        .passed(r.getPassed())
                        .earnedPoints(r.getEarnedPoints())
                        .completedAt(r.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private ChildProfile resolveChildProfile(User user) {
        // Check if user is child user
        Optional<ChildProfile> asChild = childProfileRepository.findByChildUser(user);
        if (asChild.isPresent()) {
            return asChild.get();
        }

        // Else check if parent has a child profile
        List<ChildProfile> forParent = childProfileRepository.findByParentUser(user);
        if (!forParent.isEmpty()) {
            return forParent.get(0);
        }

        // Auto-create a default child profile for the parent
        ChildProfile defaultChild = ChildProfile.builder()
                .parentUser(user)
                .firstName("Alex")
                .lastName("Silva")
                .age(10)
                .avatar("avatar_default.png")
                .currentSavings(new BigDecimal("5000.00"))
                .totalPoints(150)
                .build();
        return childProfileRepository.save(defaultChild);
    }

    private ChildProfileResponse mapToProfileResponse(ChildProfile c) {
        ChildProfileResponse.ChildUserInfo childUserInfo = null;
        
        if (c.getChildUser() != null) {
            childUserInfo = ChildProfileResponse.ChildUserInfo.builder()
                    .id(c.getChildUser().getId())
                    .email(c.getChildUser().getEmail())
                    .firstName(c.getChildUser().getFirstName())
                    .lastName(c.getChildUser().getLastName())
                    .enabled(c.getChildUser().getEnabled())
                    .emailVerified(c.getChildUser().getEmailVerified())
                    .profileComplete(c.getChildUser().getProfileComplete())
                    .build();
        }
        
        return ChildProfileResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .age(c.getAge())
                .avatar(c.getAvatar())
                .currentSavings(c.getCurrentSavings())
                .totalPoints(c.getTotalPoints())
                .childUser(childUserInfo)
                .activeGoalsCount((int) c.getGoals().stream().filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS).count())
                .completedQuizzesCount(c.getQuizResults().size())
                .totalRewardsCount(c.getRewards().size())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private SavingsGoalResponse mapToGoalResponse(SavingsGoal g) {
        double progress = 0.0;
        if (g.getTargetAmount() != null && g.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = g.getCurrentAmount().divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            progress = Math.min(100.0, Math.max(0.0, progress));
        }

        return SavingsGoalResponse.builder()
                .id(g.getId())
                .childProfileId(g.getChildProfile() != null ? g.getChildProfile().getId() : null)
                .childName(g.getChildProfile() != null ? g.getChildProfile().getFirstName() : null)
                .title(g.getTitle())
                .targetAmount(g.getTargetAmount())
                .currentAmount(g.getCurrentAmount())
                .progressPercentage(progress)
                .deadline(g.getDeadline())
                .status(g.getStatus())
                .category(g.getCategory())
                .icon(g.getIcon())
                .notes(g.getNotes())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }

    private QuizResponse mapToQuizResponse(Quiz q, ChildProfile child) {
        boolean completed = false;
        int lastScore = 0;
        if (child != null) {
            List<QuizResult> results = quizResultRepository.findByChildProfileAndQuiz(child, q);
            if (!results.isEmpty()) {
                completed = results.stream().anyMatch(QuizResult::getPassed);
                lastScore = results.get(0).getScore();
            }
        }

        List<QuizResponse.QuestionDTO> questions = q.getQuestions().stream()
                .map(quest -> QuizResponse.QuestionDTO.builder()
                        .id(quest.getId())
                        .questionText(quest.getQuestionText())
                        .explanation(quest.getExplanation())
                        .orderIndex(quest.getOrderIndex())
                        .options(quest.getOptions().stream()
                                .map(opt -> QuizResponse.OptionDTO.builder()
                                        .id(opt.getId())
                                        .optionText(opt.getOptionText())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return QuizResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .category(q.getCategory())
                .description(q.getDescription())
                .difficulty(q.getDifficulty())
                .rewardPoints(q.getRewardPoints())
                .badgeUrl(q.getBadgeUrl())
                .icon(q.getIcon())
                .totalQuestions(q.getQuestions().size())
                .isCompleted(completed)
                .lastScore(lastScore)
                .questions(questions)
                .build();
    }

    private RewardResponse mapToRewardResponse(Reward r) {
        return RewardResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .badgeIcon(r.getBadgeIcon())
                .rewardType(r.getRewardType())
                .pointsAwarded(r.getPointsAwarded())
                .unlockedAt(r.getUnlockedAt())
                .build();
    }
}
