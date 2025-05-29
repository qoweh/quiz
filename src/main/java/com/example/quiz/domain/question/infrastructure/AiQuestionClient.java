package com.example.quiz.domain.question.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * AI 서비스와의 통신을 담당하는 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiQuestionClient {

    private final OllamaChatModel chatModel;

    /**
     * AI에게 문제 생성 요청
     * @param prompt 생성할 문제에 대한 프롬프트
     * @return AI 응답 텍스트
     * @throws AiCommunicationException AI 통신 실패 시
     */
    public String generateQuestions(String prompt) {
        try {
            log.info("AI 문제 생성 요청 시작");
            ChatResponse response = chatModel.call(new Prompt(prompt));
            String aiResponse = response.getResult().getOutput().getText();

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                throw new AiCommunicationException("AI 응답이 비어있습니다.");
            }

            log.info("AI 응답 수신 완료: {} 문자", aiResponse.length());
            return aiResponse;

        } catch (Exception e) {
            log.error("AI 통신 실패", e);
            throw new AiCommunicationException("AI 서비스 통신에 실패했습니다.", e);
        }
    }

    public String test(String input) {
        return chatModel.call(input);
    }

    public static class AiCommunicationException extends RuntimeException {
        public AiCommunicationException(String message) {
            super(message);
        }

        public AiCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}