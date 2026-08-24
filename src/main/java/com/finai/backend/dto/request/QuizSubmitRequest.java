package com.finai.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {

    // Map of questionId -> selectedOptionId
    @NotNull(message = "Answers map is required")
    private Map<Long, Long> answers;
}
