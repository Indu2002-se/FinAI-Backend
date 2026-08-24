package com.finai.backend.entity;

import com.finai.backend.entity.enums.QuizDifficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends BaseEntity {

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "category", nullable = false, length = 50) // e.g. "Budgeting", "Saving", "Needs vs Wants"
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    @Builder.Default
    private QuizDifficulty difficulty = QuizDifficulty.BEGINNER;

    @Column(name = "reward_points", nullable = false)
    @Builder.Default
    private Integer rewardPoints = 50;

    @Column(name = "badge_url", length = 200)
    private String badgeUrl;

    @Column(name = "icon", length = 50)
    private String icon;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();
}
