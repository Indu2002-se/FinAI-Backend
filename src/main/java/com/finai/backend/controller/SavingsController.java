package com.finai.backend.controller;

import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.request.SavingsRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.SavingsGoalResponse;
import com.finai.backend.dto.response.SavingsResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.SavingsService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor
@Tag(name = "Savings", description = "Savings and goals management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SavingsController {

    private final SavingsService savingsService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get all savings accounts")
    public ResponseEntity<ApiResponse<List<SavingsResponse>>> getAllSavings() {
        User user = securityUtils.getCurrentUser();
        List<SavingsResponse> response = savingsService.getAllSavings(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get savings account by ID")
    public ResponseEntity<ApiResponse<SavingsResponse>> getSavingsById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        SavingsResponse response = savingsService.getSavingsById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new savings account")
    public ResponseEntity<ApiResponse<SavingsResponse>> createSavings(@Valid @RequestBody SavingsRequest request) {
        User user = securityUtils.getCurrentUser();
        SavingsResponse response = savingsService.createSavings(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings account added successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update savings account")
    public ResponseEntity<ApiResponse<SavingsResponse>> updateSavings(
            @PathVariable Long id,
            @Valid @RequestBody SavingsRequest request) {
        User user = securityUtils.getCurrentUser();
        SavingsResponse response = savingsService.updateSavings(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Savings account updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete savings account")
    public ResponseEntity<ApiResponse<Void>> deleteSavings(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        savingsService.deleteSavings(id, user);
        return ResponseEntity.ok(ApiResponse.success("Savings account deleted successfully", null));
    }

    // Goals endpoints
    @GetMapping("/goals")
    @Operation(summary = "Get all savings goals")
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> getAllGoals() {
        User user = securityUtils.getCurrentUser();
        List<SavingsGoalResponse> response = savingsService.getAllGoals(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/goals/{id}")
    @Operation(summary = "Get savings goal by ID")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> getGoalById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        SavingsGoalResponse response = savingsService.getGoalById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/goals")
    @Operation(summary = "Create new savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createGoal(@Valid @RequestBody SavingsGoalRequest request) {
        User user = securityUtils.getCurrentUser();
        SavingsGoalResponse response = savingsService.createGoal(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings goal created successfully", response));
    }

    @PutMapping("/goals/{id}")
    @Operation(summary = "Update savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {
        User user = securityUtils.getCurrentUser();
        SavingsGoalResponse response = savingsService.updateGoal(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Savings goal updated successfully", response));
    }

    @DeleteMapping("/goals/{id}")
    @Operation(summary = "Delete savings goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        savingsService.deleteGoal(id, user);
        return ResponseEntity.ok(ApiResponse.success("Savings goal deleted successfully", null));
    }
}
