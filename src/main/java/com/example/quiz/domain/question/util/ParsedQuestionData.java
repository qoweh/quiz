package com.example.quiz.domain.question.util;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedQuestionData {
    private String problem;
    private Map<Integer, String> options;
    private Integer answer;
    private String explanation;
}
