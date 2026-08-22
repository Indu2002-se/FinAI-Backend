package com.finai.backend.service.interfaces;

import com.finai.backend.dto.request.ConfirmTransactionRequest;
import com.finai.backend.dto.request.DetectedTransactionRequest;
import com.finai.backend.dto.request.DetectionSettingsRequest;
import com.finai.backend.dto.response.DetectedTransactionResponse;
import com.finai.backend.dto.response.DetectionSettingsResponse;
import com.finai.backend.entity.User;

import java.util.List;

public interface DetectedTransactionService {
    DetectedTransactionResponse recordDetectedTransaction(DetectedTransactionRequest request, User user);
    List<DetectedTransactionResponse> recordBatchDetectedTransactions(List<DetectedTransactionRequest> requests, User user);
    List<DetectedTransactionResponse> getPendingTransactions(User user);
    List<DetectedTransactionResponse> getAllDetectedTransactions(User user);
    DetectedTransactionResponse getDetectedTransactionById(Long id, User user);
    DetectedTransactionResponse updateDetectedTransaction(Long id, DetectedTransactionRequest request, User user);
    DetectedTransactionResponse confirmTransaction(Long id, ConfirmTransactionRequest request, User user);
    DetectedTransactionResponse ignoreTransaction(Long id, User user);
    DetectionSettingsResponse getSettings(User user);
    DetectionSettingsResponse updateSettings(DetectionSettingsRequest request, User user);
}
