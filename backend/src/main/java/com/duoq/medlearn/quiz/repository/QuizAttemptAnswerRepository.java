package com.duoq.medlearn.quiz.repository;

import com.duoq.medlearn.quiz.entity.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {
    List<QuizAttemptAnswer> findByAttemptId(Long attemptId);
}
