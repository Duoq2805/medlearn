package com.duoq.medlearn.quiz.mapper;

import com.duoq.medlearn.quiz.dto.response.QuestionResponse;
import com.duoq.medlearn.quiz.dto.response.QuizResponse;
import com.duoq.medlearn.quiz.entity.Question;
import com.duoq.medlearn.quiz.entity.Quiz;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizMapper {

    public QuizResponse toResponse(Quiz quiz, List<QuestionResponse> questions) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .sourceType(quiz.getSourceType() != null ? quiz.getSourceType().name() : null)
                .sourceId(quiz.getSourceId())
                .questionCount(quiz.getQuestionCount())
                .status(quiz.getStatus())
                .createdBy(quiz.getCreatedBy() != null ? quiz.getCreatedBy().getId() : null)
                .questions(questions)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    public QuestionResponse toQuestionResponse(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .quizId(q.getQuiz() != null ? q.getQuiz().getId() : null)
                .content(q.getContent())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .explanation(q.getExplanation())
                .displayOrder(q.getDisplayOrder())
                .createdAt(q.getCreatedAt())
                .build();
    }

    public List<QuestionResponse> toQuestionResponseList(List<Question> questions) {
        return questions.stream().map(this::toQuestionResponse).toList();
    }
}
