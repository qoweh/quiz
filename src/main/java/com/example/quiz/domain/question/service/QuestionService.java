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

    // 한 번에 n개 문제 생성 (배치 방식)
    public List<QuestionDTO> generateQuizBatch(Map<String, String> request) {
        String topic = request.get("topic");
        Difficulty difficulty = Difficulty.valueOf(request.get("difficulty").toUpperCase());
        int count = Integer.parseInt(request.get("count"));
        log.info("퀴즈 생성 요청 - 주제: {}, 난이도: {}, 개수: {}", count, topic, difficulty);

        List<Question> questions = new ArrayList<>();
        List<List<QuestionOption>> questionOptions = new ArrayList<>();

        log.info("퀴즈 배치 생성 시작: {}개, 주제: {}, 난이도: {}", count, topic, difficulty);
        Long setNumber = questionRepository.findMaxSetNumber();
        if (setNumber == null) {
            setNumber = 0L;
        }

        while (true) {
            String prompt = promptGenerator.createBatchPrompt(count, topic, difficulty);
            log.debug("프롬프트 생성 완료");

            String aiResponse = aiClient.generateQuestions(prompt);
            log.debug("AI 응답 수신 완료");

//            System.out.println("AI 응답: " + aiResponse);
            List<ParsedQuestionData> parsedDataList = responseParser.parseMultipleQuestions(aiResponse);
            log.debug("응답 파싱 완료: {}개", parsedDataList.size());

            for (int i = 0; i < parsedDataList.size(); i++) {
                    ParsedQuestionData data = parsedDataList.get(i);
                    // validator.validate(data);
                    Question question = questionFactory.createQuestion(data, topic, difficulty, setNumber + 1);
                    questions.add(question);
                    List<QuestionOption> questionOption = questionFactory.createQuestionOption(data, question);
                    questionOptions.add(questionOption);
                    log.debug("{}번째 문제 생성 성공", i + 1);
            }
            log.info("퀴즈 배치 생성 완료: 총 {}개 문제", questions.size());
            if (questions.size() == count) {
                questionRepository.saveAll(questions);
                questionOptions.forEach(questionOptionRepository::saveAll);
                /* quetionsOption 저장하는 방법 2
                questionOptions.stream()
                        .map(questionOptionRepository::saveAll)
                        .collect(Collectors.toList());
                 */
                break;
            }
        }
        return questions.stream().map(QuestionDTO::new).collect(Collectors.toList());
    }

    /*
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
     */
    public List<QuestionDTO> getRecentQuestions() {
        log.info("최근 퀴즈 조회 요청");
        Long recentSetNumber = questionRepository.findMaxSetNumber();
        List<Question> questions = questionRepository.findAllBySetNumber(recentSetNumber);

        List<QuestionDTO> questionDTOs = questions.stream()
                .map(question -> {
                    // 각 Question에 대해 별도로 options 조회
                    List<QuestionOption> options = questionOptionRepository.findByQuestionId(question.getId());
                    return new QuestionDTO(question, options); // 새로운 생성자 사용
                })
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