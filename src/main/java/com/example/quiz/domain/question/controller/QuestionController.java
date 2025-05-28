package com.example.quiz.domain.question.controller;

import com.example.quiz.domain.question.domain.Difficulty;
import com.example.quiz.domain.question.dto.QuestionDTO;
import com.example.quiz.domain.question.dto.QuizResultDTO;
import com.example.quiz.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    /**
     * AI로 n개 퀴즈 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<List<QuestionDTO>> generateQuizzes(
            @RequestParam String topic,
            @RequestParam(defaultValue = "MEDIUM") Difficulty difficulty,
            @RequestParam(defaultValue = "5") int count) {
        log.info("퀴즈 생성 요청 - 주제: {}, 난이도: {}, 개수: {}", topic, difficulty, count);
        return ResponseEntity.ok(questionService.generateQuizBatch(count, topic, difficulty));
    }
    /**
     * 퀴즈 조회
     */
    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        List<QuestionDTO> questions = questionService.getRecentQuestions();
        return ResponseEntity.ok(questions);
    }

    // 문제 풀이 -> QuestionAnswer 객체 생성
    @PostMapping("/solve")
    public ResponseEntity<Void> solveQuiz(@RequestBody Map<String, Integer> answers) {
        log.info("퀴즈 풀이 요청 - 답변 개수: {}", answers.size());
        questionService.submitAnswer(answers);
        return ResponseEntity.ok().build();
    }

    // 결과 조회 + 퀴즈 조회 -> QuestionAnswer 객체 조회 & QuestionDTO 조회
    @GetMapping("/results")
    public ResponseEntity<List<QuizResultDTO>> getQuizResults() {
        log.info("퀴즈 결과 조회 요청");
        List<QuizResultDTO> results = questionService.getQuizResults();
        return ResponseEntity.ok(results);
    }

    // 퀴즈 풀이 초기화
    @PostMapping("/reset")
    public ResponseEntity<Void> resetQuiz() {
        log.info("퀴즈 풀이 초기화 요청");
        questionService.resetQuiz();
        return ResponseEntity.ok().build();
    }
}