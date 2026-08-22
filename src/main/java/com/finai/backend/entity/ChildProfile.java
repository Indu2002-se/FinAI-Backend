package com.finai.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "child_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parentUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_user_id")
    private User childUser;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "avatar", length = 100)
    private String avatar;

    @Column(name = "current_savings", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentSavings = BigDecimal.ZERO;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @OneToMany(mappedBy = "childProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SavingsGoal> goals = new HashSet<>();

    @OneToMany(mappedBy = "childProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QuizResult> quizResults = new HashSet<>();

    @OneToMany(mappedBy = "childProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reward> rewards = new HashSet<>();
}
