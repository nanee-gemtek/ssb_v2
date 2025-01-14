package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class QuestionDTO {
    private final Integer id;
    private final String subject;
    private final String content;
    private final LocalDateTime createDate;
    private final List<AnswerDTO> answerList;

    public Question toEntity(){
        return Question.builder()
                .subject(subject)
                .content(content)
                .build();
    }
}
