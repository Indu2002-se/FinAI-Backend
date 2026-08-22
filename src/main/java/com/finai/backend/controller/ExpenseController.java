package com.finai.backend.controller;

import com.finai.backend.dto.request.ExpenseRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.ExpenseResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.ExpenseService;
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
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get all expense records")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {
        User user = securityUtils.getCurrentUser();
        List<ExpenseResponse> response = expenseService.getAllExpenses(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        ExpenseResponse response = expenseService.getExpenseById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new expense record")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody ExpenseRequest request) {
        User user = securityUtils.getCurrentUser();
        ExpenseResponse response = expenseService.createExpense(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense added successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense record")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        User user = securityUtils.getCurrentUser();
        ExpenseResponse response = expenseService.updateExpense(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense record")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        expenseService.deleteExpense(id, user);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }
}
