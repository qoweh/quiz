package com.example.quiz.domain.question.domain;

import com.example.quiz.domain.question.dto.QuestionDTO;
import com.example.quiz.domain.question.util.ParsedQuestionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;

@Slf4j
@Component
public class QuestionFactory {

    /**
     * 파싱된 데이터로부터 QuestionDTO 생성
     * @param data 파싱된 문제 데이터
     * @param topic 문제 주제
     * @param difficulty 문제 난이도
     * @return 생성된 QuestionDTO
     */
    public Question createQuestion(ParsedQuestionData data, String topic, Difficulty difficulty, Long setNumber) {
        Question question = Question.builder()
                .problem(data.getProblem())
                .explanation(data.getExplanation())
                .topic(topic)
                .answer(data.getAnswer())
                .difficulty(difficulty)
                .setNumber(setNumber)
                .build();

        log.debug("문제 생성 완료: {}", question.getProblem());
        return question;
    }


    public List<QuestionOption> createQuestionOption(ParsedQuestionData data, Question question) {
        List<QuestionOption> options = data.getOptions().entrySet().stream()
                .map(entry -> QuestionOption.builder()
                        .question(question)
                        .optionText(entry.getValue())
                        .optionNumber(entry.getKey())
                        .build())
                .toList();
        options.forEach(question::addOption);
        return options;
    }

    /**
     * AI 생성 실패 시 폴백 문제 생성
     * @param topic 문제 주제
     * @param difficulty 문제 난이도
     * @param questionNumber 문제 번호
     * @return 기본 문제 QuestionDTO
     */
    public Question createFallbackQuestion(String topic, Difficulty difficulty, int questionNumber) {
        log.warn("{}번째 문제를 기본 문제로 대체합니다. 주제: {}, 난이도: {}", questionNumber, topic, difficulty);

        Question question = Question.builder()
                .problem(String.format("[문제 %d] 다음 중 %s에 대한 설명으로 올바른 것은?", questionNumber, topic))
                .explanation("AI 생성에 실패하여 기본 문제로 대체되었습니다.")
                .answer(1)
                .difficulty(difficulty)
                .build();

        // 기본 선택지들
        String[] defaultOptions = {
                "정답입니다 (기본 문제)",
                "오답입니다 (1)",
                "오답입니다 (2)",
                "오답입니다 (3)"
        };

        for (int i = 0; i < defaultOptions.length; i++) {
            QuestionOption option = QuestionOption.builder()
                    .optionText(defaultOptions[i])
                    .optionNumber(i + 1)
                    .build();
            question.addOption(option);
        }

        return question;
    }

    /**
     * 여러 개의 폴백 문제 생성
     * @param count 생성할 문제 수
     * @param topic 문제 주제
     * @param difficulty 문제 난이도
     * @return 기본 문제들의 QuestionDTO 리스트
     */
    public List<QuestionDTO> createFallbackQuestions(int count, String topic, Difficulty difficulty) {
        log.warn("전체 {}개 문제를 기본 문제로 생성합니다. 주제: {}, 난이도: {}", count, topic, difficulty);
        log.warn("구현 미완성 부분");
        List<QuestionDTO> fallbackQuestions = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Question question = createFallbackQuestion(topic, difficulty, i);
            fallbackQuestions.add(new QuestionDTO(question));
        }
        return fallbackQuestions;
    }


    /**
     * 직접 파라미터로 QuestionDTO 생성
     * @param problem 문제 내용
     * @param explanation 해설
     * @param answer 정답 번호
     * @param difficulty 난이도
     * @param options 선택지 맵
     * @return 생성된 QuestionDTO
     */
    public QuestionDTO createQuestion(String problem, String explanation, int answer,
                                      Difficulty difficulty, Map<Integer, String> options) {
        Question question = Question.builder()
                .problem(problem)
                .explanation(explanation)
                .answer(answer)
                .difficulty(difficulty)
                .build();

        options.forEach((number, text) -> {
            QuestionOption option = QuestionOption.builder()
                    .optionText(text)
                    .optionNumber(number)
                    .build();
            question.addOption(option);
        });

        return new QuestionDTO(question);
    }

}