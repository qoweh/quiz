package com.example.quiz.domain.question.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "questions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    // 퀴즈 : 문제, 답, 풀이
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    private Long setNumber;

    @Lob
    @NotBlank(message = "문제는 필수입니다")
    private String problem;

    @NotNull(message = "답은 필수입니다")
    private Integer answer;

    private String topic;

    @OneToMany(mappedBy = "question")
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    @Lob
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Difficulty difficulty =  Difficulty.EASY;

    // 편의 메서드
    public void addOption(QuestionOption option) {
        this.options.add(option);     // Question → Option
        option.setQuestion(this);     // Option → Question
    }
}
