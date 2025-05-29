package com.example.quiz.domain.question.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.quiz.domain.question.domain.Question;
import com.example.quiz.domain.question.domain.QuestionOption;

public record QuestionDTO (
        Long id,
        String question,
        Integer answer,
        String explanation,
        Map<Integer, String> options,
        String difficultyLevel
) {
    // 기존 생성자 (Question만 받는 것)
    public QuestionDTO(Question question) {
        this(
                question.getId(),
                question.getProblem(),
                question.getAnswer(),
                question.getExplanation(),
                question.getOptions().stream()
                        .collect(Collectors.toMap(
                                QuestionOption::getOptionNumber,
                                QuestionOption::getOptionText
                        )),
                question.getDifficulty().name()
        );
    }

    // 새로운 생성자 추가 (Question + List<QuestionOption> 받는 것)
    public QuestionDTO(Question question, List<QuestionOption> options) {
        this(
                question.getId(),
                question.getProblem(),
                question.getAnswer(),
                question.getExplanation(),
                options.stream()
                        .collect(Collectors.toMap(
                                QuestionOption::getOptionNumber,
                                QuestionOption::getOptionText
                        )),
                question.getDifficulty().name()
        );
    }
}