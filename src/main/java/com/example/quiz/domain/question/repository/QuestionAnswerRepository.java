package com.example.quiz.domain.question.repository;

import com.example.quiz.domain.question.domain.QuestionAnswer;
import com.example.quiz.domain.question.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    Object findByQuestionId(Long id);
}
