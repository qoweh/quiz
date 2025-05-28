package com.example.quiz.domain.question.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Setter
@Getter
@Table(name = "question_answer")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private Integer myAnswer;

    private Integer rightAnswer;

    private Boolean isCorrect;

}
