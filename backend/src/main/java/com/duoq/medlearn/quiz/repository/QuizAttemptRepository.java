package com.duoq.medlearn.quiz.repository;

import com.duoq.medlearn.quiz.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Page<QuizAttempt> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<QuizAttempt> findByQuizIdAndUserIdOrderByCreatedAtDesc(Long quizId, Long userId);
}
