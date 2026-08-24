package com.finai.backend.repository;

import com.finai.backend.entity.ChildProfile;
import com.finai.backend.entity.Quiz;
import com.finai.backend.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByChildProfileOrderByCompletedAtDesc(ChildProfile childProfile);
    List<QuizResult> findByChildProfileAndQuiz(ChildProfile childProfile, Quiz quiz);
}
