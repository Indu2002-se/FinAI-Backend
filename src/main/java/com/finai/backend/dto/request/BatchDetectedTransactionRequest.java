package com.finai.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDetectedTransactionRequest {

    @NotEmpty(message = "Transactions list cannot be empty")
    @Valid
    private List<DetectedTransactionRequest> transactions;
}
