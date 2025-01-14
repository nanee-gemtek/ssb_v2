package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnswerDTO {
    private final Integer id;
    private final String content;
    private final Question question;
    private final LocalDateTime createDate;

    // DTO → Entity 변환
    public Answer toEntity(){
        return Answer.builder()
                .content(content)
                .question(question)
                .build();
    }
}