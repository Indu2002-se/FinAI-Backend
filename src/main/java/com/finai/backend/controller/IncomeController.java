package com.finai.backend.controller;

import com.finai.backend.dto.request.IncomeRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.IncomeResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.IncomeService;
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
@RequestMapping("/api/v1/income")
@RequiredArgsConstructor
@Tag(name = "Income", description = "Income management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class IncomeController {

    private final IncomeService incomeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get all income records")
    public ResponseEntity<ApiResponse<List<IncomeResponse>>> getAllIncome() {
        User user = securityUtils.getCurrentUser();
        List<IncomeResponse> response = incomeService.getAllIncomes(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get income by ID")
    public ResponseEntity<ApiResponse<IncomeResponse>> getIncomeById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        IncomeResponse response = incomeService.getIncomeById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new income record")
    public ResponseEntity<ApiResponse<IncomeResponse>> createIncome(@Valid @RequestBody IncomeRequest request) {
        User user = securityUtils.getCurrentUser();
        IncomeResponse response = incomeService.createIncome(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income added successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update income record")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest request) {
        User user = securityUtils.getCurrentUser();
        IncomeResponse response = incomeService.updateIncome(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Income updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete income record")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        incomeService.deleteIncome(id, user);
        return ResponseEntity.ok(ApiResponse.success("Income deleted successfully", null));
    }
}
