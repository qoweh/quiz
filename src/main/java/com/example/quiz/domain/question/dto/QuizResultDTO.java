package com.example.quiz.domain.question.dto;

import java.util.List;
import com.example.quiz.domain.question.domain.Question;
import com.example.quiz.domain.question.domain.QuestionAnswer;
import com.example.quiz.domain.question.domain.QuestionOption;

public record QuizResultDTO(
        String question,
        List<String> options,
        String answer,
        Integer myAnswer,
        Integer rightAnswer,
        Boolean isCorrect
) {
    public static QuizResultDTO from(Question question, QuestionAnswer answer, List<QuestionOption> options) {
        return new QuizResultDTO(
                question.getProblem(),
                options.stream().map(QuestionOption::getOptionText).toList(),
                question.getExplanation(),
                answer.getMyAnswer(),
                answer.getRightAnswer(),
                answer.getIsCorrect()
        );
    }
}