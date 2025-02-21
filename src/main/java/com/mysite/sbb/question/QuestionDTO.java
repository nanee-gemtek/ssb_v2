package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private  Integer id;
    private  String subject;
    private  String content;
    private  LocalDateTime createDate;
    private  List<AnswerDTO> answerList;
    private  String username;

}
