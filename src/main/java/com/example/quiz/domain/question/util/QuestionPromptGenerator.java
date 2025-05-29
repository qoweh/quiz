package com.example.quiz.domain.question.util;

import com.example.quiz.domain.question.domain.Difficulty;
import org.springframework.stereotype.Component;

/**
 * AI 문제 생성을 위한 프롬프트 생성 유틸리티
 */
@Component
public class QuestionPromptGenerator {

    /**
     * 배치 문제 생성용 프롬프트 생성
     * @param count 생성할 문제 수
     * @param topic 문제 주제
     * @param difficulty 문제 난이도
     * @return 생성된 프롬프트
     */
    public String createBatchPrompt(int count, String topic, Difficulty difficulty) {
        String difficultyKorean = difficulty.getDescription();

        return String.format("""
                다음 조건으로 객관식 문제를 정확히 %d개 만들어서 JSON 형태로만 반환해줘.
                  주제: %s
                  난이도: %s
                  언어: 한국어(korean)
                  
                  반드시 아래 JSON 형식을 정확히 지켜줘:
                    [
                      {
                        "question": "문제 내용",
                        "options": [
                          "선택지1",
                          "선택지2",
                          "선택지3",
                          "선택지4"
                        ],
                        "correctAnswer": 1,
                        "explanation": "해설"
                      },
                      {
                        "question": "문제 내용",,
                        ...
                        ...
                      },
                      { 
                        "question": "문제 내용",,
                        ...
                        ...
                      },
                    ]
                  요구사항:
                  - 각 문제는 서로 다른 내용이어야 함, 중복없게 유의해
                  - 각 문제의 선택지는 서로 다른 내용이어야 함, 중복없게 유의해
                  - 명확하고 이해하기 쉬운 문제
                  - 4개의 선택지 (1개 정답, 3개 오답), 개수가 4개여야 함을 유의해
                  - correctAnswer는 1,2,3,4 중 하나여야 함
                  - 오답은 그럴듯하지만 확실히 틀려야 함
                  - 해설은 간단명료하게
                  - 다른 텍스트 없이 오직 JSON만 반환
                  - JSON 형식이 올바른지 확인
                  - 마지막으로 강조하는데, JSON 형식의 파일만 주고 다른 텍스트는 절대 포함하지 말 것
            """, count, topic, difficultyKorean, count);
    }
}