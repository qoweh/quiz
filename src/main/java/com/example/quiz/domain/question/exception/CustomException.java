package com.example.quiz.domain.question.exception;

/**
 * 문제 도메인 관련 예외 클래스들
 * 각 클래스에서 내부 클래스로 정의해도 되지만,
 * 여러 곳에서 공통으로 사용한다면 별도 파일로 분리 가능
 */


public class CustomException extends RuntimeException {
    /**
     * AI 통신 관련 예외
     */
    static class AiCommunicationException extends RuntimeException {
        public AiCommunicationException(String message) {
            super(message);
        }

        public AiCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    /**
     * 문제 파싱 관련 예외
     */
    static class QuestionParsingException extends RuntimeException {
        public QuestionParsingException(String message) {
            super(message);
        }

        public QuestionParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 문제 데이터 검증 관련 예외
     */
    static class InvalidQuestionDataException extends RuntimeException {
        public InvalidQuestionDataException(String message) {
            super(message);
        }

        public InvalidQuestionDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


