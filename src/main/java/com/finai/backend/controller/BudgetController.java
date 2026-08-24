package com.finai.backend.controller;

import com.finai.backend.dto.request.BudgetRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.BudgetResponse;
import com.finai.backend.dto.response.BudgetStatusResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.BudgetService;
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
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget planning and monitoring endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get budgets for month")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam(required = false) String month) {
        User user = securityUtils.getCurrentUser();
        List<BudgetResponse> response = budgetService.getBudgetsByMonth(month, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    @Operation(summary = "Get overall budget status and usage")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getBudgetStatus(
            @RequestParam(required = false) String month) {
        User user = securityUtils.getCurrentUser();
        BudgetStatusResponse response = budgetService.getBudgetStatus(month, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        BudgetResponse response = budgetService.getBudgetById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new category budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@Valid @RequestBody BudgetRequest request) {
        User user = securityUtils.getCurrentUser();
        BudgetResponse response = budgetService.createBudget(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        User user = securityUtils.getCurrentUser();
        BudgetResponse response = budgetService.updateBudget(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
