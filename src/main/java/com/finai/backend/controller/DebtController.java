package com.finai.backend.controller;

import com.finai.backend.dto.request.DebtRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.DebtResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.DebtService;
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
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
@Tag(name = "Debts", description = "Debt management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DebtController {

    private final DebtService debtService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get all debts")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getAllDebts() {
        User user = securityUtils.getCurrentUser();
        List<DebtResponse> response = debtService.getAllDebts(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get debt by ID")
    public ResponseEntity<ApiResponse<DebtResponse>> getDebtById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        DebtResponse response = debtService.getDebtById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new debt record")
    public ResponseEntity<ApiResponse<DebtResponse>> createDebt(@Valid @RequestBody DebtRequest request) {
        User user = securityUtils.getCurrentUser();
        DebtResponse response = debtService.createDebt(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Debt record added successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update debt record")
    public ResponseEntity<ApiResponse<DebtResponse>> updateDebt(
            @PathVariable Long id,
            @Valid @RequestBody DebtRequest request) {
        User user = securityUtils.getCurrentUser();
        DebtResponse response = debtService.updateDebt(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Debt updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete debt record")
    public ResponseEntity<ApiResponse<Void>> deleteDebt(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        debtService.deleteDebt(id, user);
        return ResponseEntity.ok(ApiResponse.success("Debt deleted successfully", null));
    }
}
