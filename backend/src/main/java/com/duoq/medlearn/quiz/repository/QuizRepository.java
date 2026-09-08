package com.duoq.medlearn.quiz.repository;

import com.duoq.medlearn.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    Page<Quiz> findByCreatedById(Long userId, Pageable pageable);
}
