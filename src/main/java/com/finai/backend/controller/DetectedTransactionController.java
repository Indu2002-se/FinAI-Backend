package com.finai.backend.controller;

import com.finai.backend.dto.request.BatchDetectedTransactionRequest;
import com.finai.backend.dto.request.ConfirmTransactionRequest;
import com.finai.backend.dto.request.DetectedTransactionRequest;
import com.finai.backend.dto.request.DetectionSettingsRequest;
import com.finai.backend.dto.response.ApiResponse;
import com.finai.backend.dto.response.DetectedTransactionResponse;
import com.finai.backend.dto.response.DetectionSettingsResponse;
import com.finai.backend.entity.User;
import com.finai.backend.service.interfaces.DetectedTransactionService;
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
@RequestMapping("/api/v1/transactions/detected")
@RequiredArgsConstructor
@Tag(name = "Automatic Transaction Detection", description = "Endpoints for detected SMS/Notification transactions")
@SecurityRequirement(name = "bearerAuth")
public class DetectedTransactionController {

    private final DetectedTransactionService detectedTransactionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Record a detected transaction from SMS or notification")
    public ResponseEntity<ApiResponse<DetectedTransactionResponse>> recordDetectedTransaction(
            @Valid @RequestBody DetectedTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        DetectedTransactionResponse response = detectedTransactionService.recordDetectedTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Detected transaction recorded successfully", response));
    }

    @PostMapping("/batch")
    @Operation(summary = "Record multiple detected transactions in batch")
    public ResponseEntity<ApiResponse<List<DetectedTransactionResponse>>> recordBatchDetectedTransactions(
            @Valid @RequestBody BatchDetectedTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        List<DetectedTransactionResponse> response = detectedTransactionService.recordBatchDetectedTransactions(request.getTransactions(), user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch detected transactions recorded successfully", response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending detected transactions waiting for confirmation")
    public ResponseEntity<ApiResponse<List<DetectedTransactionResponse>>> getPendingTransactions() {
        User user = securityUtils.getCurrentUser();
        List<DetectedTransactionResponse> response = detectedTransactionService.getPendingTransactions(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all detected transactions history")
    public ResponseEntity<ApiResponse<List<DetectedTransactionResponse>>> getAllDetectedTransactions() {
        User user = securityUtils.getCurrentUser();
        List<DetectedTransactionResponse> response = detectedTransactionService.getAllDetectedTransactions(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detected transaction by ID")
    public ResponseEntity<ApiResponse<DetectedTransactionResponse>> getDetectedTransactionById(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        DetectedTransactionResponse response = detectedTransactionService.getDetectedTransactionById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update detected transaction details before confirmation")
    public ResponseEntity<ApiResponse<DetectedTransactionResponse>> updateDetectedTransaction(
            @PathVariable Long id,
            @RequestBody DetectedTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        DetectedTransactionResponse response = detectedTransactionService.updateDetectedTransaction(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Detected transaction updated successfully", response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm detected transaction (converts DEBIT -> Expense, CREDIT -> Income)")
    public ResponseEntity<ApiResponse<DetectedTransactionResponse>> confirmTransaction(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        DetectedTransactionResponse response = detectedTransactionService.confirmTransaction(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Transaction confirmed and converted successfully", response));
    }

    @PostMapping("/{id}/ignore")
    @Operation(summary = "Ignore/dismiss a detected transaction")
    public ResponseEntity<ApiResponse<DetectedTransactionResponse>> ignoreTransaction(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        DetectedTransactionResponse response = detectedTransactionService.ignoreTransaction(id, user);
        return ResponseEntity.ok(ApiResponse.success("Transaction ignored successfully", response));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get transaction detection settings (SMS, Notification toggles)")
    public ResponseEntity<ApiResponse<DetectionSettingsResponse>> getSettings() {
        User user = securityUtils.getCurrentUser();
        DetectionSettingsResponse response = detectedTransactionService.getSettings(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update transaction detection settings")
    public ResponseEntity<ApiResponse<DetectionSettingsResponse>> updateSettings(
            @RequestBody DetectionSettingsRequest request) {
        User user = securityUtils.getCurrentUser();
        DetectionSettingsResponse response = detectedTransactionService.updateSettings(request, user);
        return ResponseEntity.ok(ApiResponse.success("Detection settings updated successfully", response));
    }
}
