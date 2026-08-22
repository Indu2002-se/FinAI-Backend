package com.finai.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer totalQuestions;
    private Double scorePercentage;
    private Boolean passed;
    private Integer earnedPoints;
    private String earnedBadge;
    private LocalDateTime completedAt;
}
