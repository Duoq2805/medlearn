package com.duoq.medlearn.quiz.service;

import com.duoq.medlearn.quiz.dto.request.QuizGenerateRequest;
import com.duoq.medlearn.quiz.dto.request.QuizSubmitRequest;
import com.duoq.medlearn.quiz.dto.response.QuizAttemptResponse;
import com.duoq.medlearn.quiz.dto.response.QuizGenerateResponse;
import com.duoq.medlearn.quiz.dto.response.QuizResponse;
import com.duoq.medlearn.quiz.dto.response.QuizSubmitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizGenerateResponse generate(QuizGenerateRequest request, Long userId);
    QuizResponse getById(Long quizId, Long userId);
    Page<QuizResponse> listByUser(Long userId, Pageable pageable);
    QuizSubmitResponse submit(Long quizId, QuizSubmitRequest request, Long userId);
    Page<QuizAttemptResponse> listAttempts(Long userId, Pageable pageable);
}
