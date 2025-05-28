package com.example.quiz.domain.question.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum Difficulty {
    EASY("쉬움"),
    MEDIUM("보통"),
    HARD("어려움");

    private String description;
}
