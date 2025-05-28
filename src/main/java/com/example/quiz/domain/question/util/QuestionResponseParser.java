package com.example.quiz.domain.question.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class QuestionResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ParsedQuestionData> parseMultipleQuestions(String aiResponse) {
        try {
            aiResponse = aiResponse.trim();
            if (aiResponse.startsWith("```")) {
                int firstLineEnd = aiResponse.indexOf("\n");
                int lastCodeBlockStart = aiResponse.lastIndexOf("```");

                if (firstLineEnd != -1 && lastCodeBlockStart != -1 && lastCodeBlockStart > firstLineEnd) {
                    aiResponse = aiResponse.substring(firstLineEnd + 1, lastCodeBlockStart).trim();
                }
            }
            List<Map<String, Object>> rawList = objectMapper.readValue(aiResponse, new TypeReference<>() {});

            List<ParsedQuestionData> parsedList = new ArrayList<>();

            for (Map<String, Object> rawQuestion : rawList) {
                String question = (String) rawQuestion.get("question");
                List<String> optionsList = (List<String>) rawQuestion.get("options");
                int correctAnswer = (Integer) rawQuestion.get("correctAnswer");
                String explanation = (String) rawQuestion.get("explanation");

                // List -> Map<Integer, String>
                Map<Integer, String> optionsMap = new HashMap<>();
                for (int j = 0; j < optionsList.size(); j++) {
                    optionsMap.put(j + 1, optionsList.get(j));  // 1-based index
                }

                ParsedQuestionData parsed = ParsedQuestionData.builder()
                        .problem(question)
                        .options(optionsMap)
                        .answer(correctAnswer)
                        .explanation(explanation)
                        .build();
                parsedList.add(parsed);
            }
            return parsedList;
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage(), e);
            throw new QuestionParsingException("AI 응답 JSON 파싱 실패", e);
        }
    }

    public static class QuestionParsingException extends RuntimeException {
        public QuestionParsingException(String message) {
            super(message);
        }

        public QuestionParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}