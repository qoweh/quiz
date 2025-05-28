package com.example.quiz.domain.question.dto;

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
}
