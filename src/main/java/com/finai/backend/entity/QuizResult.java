package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "passed", nullable = false)
    @Builder.Default
    private Boolean passed = false;

    @Column(name = "earned_points", nullable = false)
    @Builder.Default
    private Integer earnedPoints = 0;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
}
