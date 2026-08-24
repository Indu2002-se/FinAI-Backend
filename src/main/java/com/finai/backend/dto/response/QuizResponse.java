package com.finai.backend.dto.response;

import com.finai.backend.entity.enums.QuizDifficulty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String category;
    private String description;
    private QuizDifficulty difficulty;
    private Integer rewardPoints;
    private String badgeUrl;
    private String icon;
    private Integer totalQuestions;
    private Boolean isCompleted;
    private Integer lastScore;
    private List<QuestionDTO> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDTO {
        private Long id;
        private String questionText;
        private String explanation;
        private Integer orderIndex;
        private List<OptionDTO> options;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDTO {
        private Long id;
        private String optionText;
    }
}
