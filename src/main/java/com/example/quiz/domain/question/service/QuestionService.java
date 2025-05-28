package com.example.quiz.domain.question.service;

import com.example.quiz.domain.question.domain.*;
import com.example.quiz.domain.question.dto.QuestionDTO;
import com.example.quiz.domain.question.dto.QuizResultDTO;
import com.example.quiz.domain.question.infrastructure.AiQuestionClient;
import com.example.quiz.domain.question.repository.QuestionAnswerRepository;
import com.example.quiz.domain.question.repository.QuestionOptionRepository;
import com.example.quiz.domain.question.repository.QuestionRepository;
import com.example.quiz.domain.question.util.ParsedQuestionData;
import com.example.quiz.domain.question.util.QuestionPromptGenerator;
import com.example.quiz.domain.question.util.QuestionResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final AiQuestionClient aiClient;
    private final QuestionPromptGenerator promptGenerator;
    private final QuestionResponseParser responseParser;
    private final QuestionValidator validator;
    private final QuestionFactory questionFactory;
    private final String DEFAULT_SUBJECT = "동물에 관한 문제";
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    /**
     * 한 번에 n개 문제 생성 (배치 방식)
     * @param count 생성할 문제 수
     * @param topic 문제 주제
     * @param difficulty 문제 난이도
     * @return 생성된 문제 리스트
     */
    public List<QuestionDTO> generateQuizBatch(int count, String topic, Difficulty difficulty) {
        log.info("퀴즈 배치 생성 시작: {}개, 주제: {}, 난이도: {}", count, topic, difficulty);
        Long setNumber = questionRepository.findMaxSetNumber();
        if (setNumber == null) {
            setNumber = 0L;
        }

        try {
            String prompt = promptGenerator.createBatchPrompt(count, topic, difficulty);
            log.debug("프롬프트 생성 완료");

            String aiResponse = aiClient.generateQuestions(prompt);
            log.debug("AI 응답 수신 완료");

            List<ParsedQuestionData> parsedDataList = responseParser.parseMultipleQuestions(aiResponse);
            log.debug("응답 파싱 완료: {}개", parsedDataList.size());

            List<QuestionDTO> questions = new ArrayList<>();
            for (int i = 0; i < parsedDataList.size(); i++) {
                try {
                    ParsedQuestionData data = parsedDataList.get(i);
                    validator.validate(data);

                    Question question = questionFactory.createQuestion(data, topic, difficulty, setNumber + 1);
                    List<QuestionOption> questionOptions = questionFactory.createQuestionOption(data, question);

                    questionRepository.save(question);
                    questionOptionRepository.saveAll(questionOptions);

                    questions.add(new QuestionDTO(question));
                    log.debug("{}번째 문제 생성 성공", i + 1);
                } catch (Exception e) {
                    log.error("{}번째 문제 생성 실패: {}", i + 1, e.getMessage());
                    Question fallback = questionFactory.createFallbackQuestion(topic, difficulty, i + 1);
//                    questionRepository.save(fallback);
                    questions.add(new QuestionDTO(fallback));
                }
            }

            // 부족한 문제는 폴백으로 채움
            while (questions.size() < count) {
                int questionNumber = questions.size() + 1;
                Question fallback = questionFactory.createFallbackQuestion(topic, difficulty, questionNumber);
//                questionRepository.save(fallback);
                questions.add(new QuestionDTO(fallback));
                log.warn("{}번째 문제를 폴백으로 생성", questionNumber);
            }

            log.info("퀴즈 배치 생성 완료: 총 {}개 문제", questions.size());
            return questions.subList(0, Math.min(count, questions.size()));

        } catch (Exception e) {
            log.error("퀴즈 배치 생성 실패", e);
            return questionFactory.createFallbackQuestions(count, topic, difficulty);
        }
    }

    public List<QuestionDTO> getRecentQuestions() {
        log.info("최근 퀴즈 조회 요청");
        Long recentSetNumber = questionRepository.findMaxSetNumber();
        List<Question> questions = questionRepository.findAllBySetNumber(recentSetNumber);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(QuestionDTO::new)
                .collect(Collectors.toList());
        log.info("총 {}개(set:{})의 퀴즈 조회 완료", questionDTOs.size(), recentSetNumber);
        return questionDTOs;
    }

    public void submitAnswer(Map<String, Integer> answers) {
        List<QuestionAnswer> questionAnswers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            Long questionId = Long.parseLong(entry.getKey());
            Integer answer = entry.getValue();
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));
            QuestionAnswer questionAnswer = QuestionAnswer.builder()
                    .question(question)
                    .myAnswer(answer)
                    .rightAnswer(question.getAnswer())
                    .isCorrect(question.getAnswer().equals(answer))
                    .build();
            questionAnswers.add(questionAnswer);
        }
        questionAnswerRepository.saveAll(questionAnswers);
    }

    public List<QuizResultDTO> getQuizResults() {
        Long recentSetNumber = questionRepository.findMaxSetNumber();
        List<Question> questions = questionRepository.findAllBySetNumber(recentSetNumber);
        List<QuestionAnswer> answers = questions.stream()
                .map(q ->
                        (QuestionAnswer) questionAnswerRepository.findByQuestionId(q.getId()))
                .toList();
        List<List<QuestionOption>> options = questions.stream()
                .map(q -> (List<QuestionOption>) questionOptionRepository.findByQuestionId(q.getId()))
                .toList();

        if (questions.isEmpty() || answers.isEmpty() || options.isEmpty()
            || Stream.of(questions, answers, options)
                .mapToInt(List::size).distinct().count() != 1) {
            log.warn("퀴즈 결과 조회 실패");
            throw new IllegalArgumentException("퀴즈 결과 조회 실패");
        }
        return IntStream
                .range(0, questions.size())
                .mapToObj(i ->
                        QuizResultDTO.from(
                                questions.get(i),
                                answers.get(i),
                                options.get(i)
                        )
                )
                .collect(Collectors.toList());
    }

    public void resetQuiz() {
        questionAnswerRepository.deleteAll();
        log.info("퀴즈 풀이 초기화 완료");
    }
}