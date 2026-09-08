package com.duoq.medlearn.quiz.repository;

import com.duoq.medlearn.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByDisplayOrderAsc(Long quizId);
    void deleteByQuizId(Long quizId);
}
