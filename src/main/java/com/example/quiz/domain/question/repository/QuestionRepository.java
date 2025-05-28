package com.example.quiz.domain.question.repository;

import java.util.List;
import com.example.quiz.domain.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q.setNumber FROM Question q ORDER BY q.setNumber DESC LIMIT 1")
    Long findMaxSetNumber();

    List<Question> findAllBySetNumber(Long recentSetNumber);
}
