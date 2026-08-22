package com.finai.backend.controller;

import com.finai.backend.dto.request.ChildProfileRequest;
import com.finai.backend.dto.request.SavingsGoalRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.ChildProfileResponse;
import com.finai.backend.dto.response.SavingsGoalResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.ChildService;
import com.finai.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
@Tag(name = "Child Management (Parent)", description = "Parent endpoints for managing child profiles and savings goals")
@SecurityRequirement(name = "bearerAuth")
public class ChildManagementController {

    private final ChildService childService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "List all child profiles for parent")
    public ResponseEntity<ApiResponse<List<ChildProfileResponse>>> getChildren() {
        User parent = securityUtils.getCurrentUser();
        List<ChildProfileResponse> response = childService.getChildrenForParent(parent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{childId}")
    @Operation(summary = "Get child profile by ID")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> getChildById(@PathVariable Long childId) {
        User parent = securityUtils.getCurrentUser();
        ChildProfileResponse response = childService.getChildById(childId, parent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create child profile")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> createChild(
            @Valid @RequestBody ChildProfileRequest request) {
        User parent = securityUtils.getCurrentUser();
        ChildProfileResponse response = childService.createChildProfile(request, parent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Child profile created successfully", response));
    }

    @PutMapping("/{childId}")
    @Operation(summary = "Update child profile")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> updateChild(
            @PathVariable Long childId,
            @Valid @RequestBody ChildProfileRequest request) {
        User parent = securityUtils.getCurrentUser();
        ChildProfileResponse response = childService.updateChildProfile(childId, request, parent);
        return ResponseEntity.ok(ApiResponse.success("Child profile updated successfully", response));
    }

    @DeleteMapping("/{childId}")
    @Operation(summary = "Delete child profile")
    public ResponseEntity<ApiResponse<Void>> deleteChild(@PathVariable Long childId) {
        User parent = securityUtils.getCurrentUser();
        childService.deleteChildProfile(childId, parent);
        return ResponseEntity.ok(ApiResponse.success("Child profile deleted successfully", null));
    }

    @GetMapping("/{childId}/goals")
    @Operation(summary = "Get child savings goals")
    public ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> getChildGoals(@PathVariable Long childId) {
        User parent = securityUtils.getCurrentUser();
        List<SavingsGoalResponse> response = childService.getChildGoals(childId, parent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{childId}/goals")
    @Operation(summary = "Create savings goal for child")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createChildGoal(
            @PathVariable Long childId,
            @Valid @RequestBody SavingsGoalRequest request) {
        User parent = securityUtils.getCurrentUser();
        SavingsGoalResponse response = childService.createChildGoal(childId, request, parent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings goal created for child", response));
    }

    @PutMapping("/{childId}/goals/{goalId}")
    @Operation(summary = "Update child savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateChildGoal(
            @PathVariable Long childId,
            @PathVariable Long goalId,
            @Valid @RequestBody SavingsGoalRequest request) {
        User parent = securityUtils.getCurrentUser();
        SavingsGoalResponse response = childService.updateChildGoal(childId, goalId, request, parent);
        return ResponseEntity.ok(ApiResponse.success("Child savings goal updated", response));
    }

    @PostMapping("/{childId}/savings")
    @Operation(summary = "Deposit savings to child balance")
    public ResponseEntity<ApiResponse<ChildProfileResponse>> depositChildSavings(
            @PathVariable Long childId,
            @RequestBody Map<String, Object> payload) {
        User parent = securityUtils.getCurrentUser();
        BigDecimal amount = BigDecimal.valueOf(((Number) payload.getOrDefault("amount", 0.0)).doubleValue());
        ChildProfileResponse response = childService.depositChildSavings(childId, amount, parent);
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }
}
