package com.finai.backend.controller;

import com.finai.backend.dto.request.QuizSubmitRequest;
import com.finai.backend.dto.response.*;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.ChildService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/child")
@RequiredArgsConstructor
@Tag(name = "Child Self-Service", description = "Child user dashboard, quizzes, rewards, and savings endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChildSelfServiceController {

    private final ChildService childService;
    private final SecurityUtils securityUtils;

    @GetMapping("/dashboard")
    @Operation(summary = "Get child dashboard data")
    public ResponseEntity<ApiResponse<ChildDashboardResponse>> getChildDashboard() {
        User user = securityUtils.getCurrentUser();
        ChildDashboardResponse response = childService.getChildDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/savings")
    @Operation(summary = "Get child savings summary")
    public ResponseEntity<ApiResponse<ChildDashboardResponse>> getChildSavings() {
        User user = securityUtils.getCurrentUser();
        ChildDashboardResponse response = childService.getChildDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/goals")
    @Operation(summary = "Get child goals")
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> getChildGoals() {
        User user = securityUtils.getCurrentUser();
        ChildDashboardResponse dash = childService.getChildDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(dash.getSavingsGoals()));
    }

    @GetMapping("/quizzes")
    @Operation(summary = "Get available financial literacy quizzes")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzes() {
        User user = securityUtils.getCurrentUser();
        List<QuizResponse> response = childService.getAvailableQuizzes(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/quizzes/{quizId}")
    @Operation(summary = "Get quiz details by ID")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(@PathVariable Long quizId) {
        QuizResponse response = childService.getQuizById(quizId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/quizzes/{quizId}/attempt")
    @Operation(summary = "Submit quiz answers and receive score/reward")
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request) {
        User user = securityUtils.getCurrentUser();
        QuizResultResponse response = childService.submitQuizAttempt(quizId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Quiz evaluated successfully", response));
    }

    @GetMapping("/rewards")
    @Operation(summary = "Get earned badges and rewards")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getRewards() {
        User user = securityUtils.getCurrentUser();
        List<RewardResponse> response = childService.getChildRewards(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get quiz completion progress and history")
    public ResponseEntity<ApiResponse<List<QuizResultResponse>>> getProgress() {
        User user = securityUtils.getCurrentUser();
        List<QuizResultResponse> response = childService.getChildQuizHistory(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
