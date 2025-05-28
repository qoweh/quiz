package com.example.quiz.domain.question.domain;

import com.example.quiz.domain.question.util.ParsedQuestionData;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 문제 데이터 검증을 담당하는 도메인 서비스
 */
@Component
public class QuestionValidator {

    /**
     * 파싱된 문제 데이터 전체 검증
     * @param data 검증할 문제 데이터
     * @throws InvalidQuestionDataException 검증 실패 시
     */
    public void validate(ParsedQuestionData data) {
        validateProblem(data.getProblem());
        validateOptions(data.getOptions());
        validateAnswer(data.getAnswer(), data.getOptions());
        validateExplanation(data.getExplanation());
    }

    /**
     * 문제 내용 검증
     */
    public void validateProblem(String problem) {
        if (problem == null || problem.trim().isEmpty()) {
            throw new InvalidQuestionDataException("문제가 비어있습니다.");
        }

        if (problem.length() < 5) {
            throw new InvalidQuestionDataException("문제가 너무 짧습니다. (최소 5자 이상)");
        }

        if (problem.length() > 1000) {
            throw new InvalidQuestionDataException("문제가 너무 깁니다. (최대 1000자 이하)");
        }
    }

    /**
     * 선택지 검증
     */
    public void validateOptions(Map<Integer, String> options) {
        if (options == null || options.isEmpty()) {
            throw new InvalidQuestionDataException("선택지가 없습니다.");
        }

        if (options.size() != 4) {
            throw new InvalidQuestionDataException("선택지가 4개가 아닙니다. 현재: " + options.size() + "개");
        }

        // 1, 2, 3, 4번이 모두 있는지 확인
        for (int i = 1; i <= 4; i++) {
            if (!options.containsKey(i)) {
                throw new InvalidQuestionDataException(i + "번 선택지가 없습니다.");
            }

            String optionText = options.get(i);
            if (optionText == null || optionText.trim().isEmpty()) {
                throw new InvalidQuestionDataException(i + "번 선택지가 비어있습니다.");
            }

            if (optionText.length() > 200) {
                throw new InvalidQuestionDataException(i + "번 선택지가 너무 깁니다. (최대 200자 이하)");
            }
        }

        // 중복 선택지 확인
        long distinctCount = options.values().stream()
                .map(String::trim)
                .distinct()
                .count();

        if (distinctCount != 4) {
            throw new InvalidQuestionDataException("중복된 선택지가 있습니다.");
        }
    }

    /**
     * 정답 검증
     */
    public void validateAnswer(Integer answer, Map<Integer, String> options) {
        if (answer == null) {
            throw new InvalidQuestionDataException("정답이 지정되지 않았습니다.");
        }

        if (answer < 1 || answer > 4) {
            throw new InvalidQuestionDataException("정답이 유효하지 않습니다: " + answer + " (1~4 범위여야 함)");
        }

        if (options != null && !options.containsKey(answer)) {
            throw new InvalidQuestionDataException("정답에 해당하는 선택지가 없습니다: " + answer + "번");
        }
    }

    /**
     * 해설 검증
     */
    public void validateExplanation(String explanation) {
        if (explanation == null || explanation.trim().isEmpty()) {
            throw new InvalidQuestionDataException("해설이 비어있습니다.");
        }

        if (explanation.length() > 500) {
            throw new InvalidQuestionDataException("해설이 너무 깁니다. (최대 500자 이하)");
        }
    }

    /**
     * 완성된 Question 엔티티 검증
     */
    public void validateQuestion(Question question) {
        if (question == null) {
            throw new InvalidQuestionDataException("Question 객체가 null입니다.");
        }

        validateProblem(question.getProblem());
        validateAnswer(question.getAnswer(), null);
        validateExplanation(question.getExplanation());

        if (question.getDifficulty() == null) {
            throw new InvalidQuestionDataException("문제 난이도가 설정되지 않았습니다.");
        }

        if (question.getOptions() == null || question.getOptions().size() != 4) {
            throw new InvalidQuestionDataException("문제 옵션이 4개가 아닙니다.");
        }
    }

    /**
     * 문제 데이터 검증 예외
     */
    public static class InvalidQuestionDataException extends RuntimeException {
        public InvalidQuestionDataException(String message) {
            super(message);
        }

        public InvalidQuestionDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}