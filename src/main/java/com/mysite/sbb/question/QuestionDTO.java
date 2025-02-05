package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerDTO;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuestionDTO {
    private final Integer id;
    private final String subject;
    private final String content;
    private final LocalDateTime createDate;
    private final List<AnswerDTO> answerList;

}
