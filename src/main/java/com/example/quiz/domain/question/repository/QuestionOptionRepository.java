package com.example.quiz.domain.question.repository;

import java.util.List;
import com.example.quiz.domain.question.domain.Question;
import com.example.quiz.domain.question.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionId(Long Id);
}
