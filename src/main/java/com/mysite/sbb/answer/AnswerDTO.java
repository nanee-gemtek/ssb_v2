package com.mysite.sbb.answer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
//DTO에 setter 추가한 이유
//https://www.inflearn.com/community/questions/161417/dto-%EC%82%AC%EC%9A%A9%EC%97%90%EB%8C%80%ED%95%B4-%EA%B6%81%EA%B8%88%ED%95%A9%EB%8B%88%EB%8B%A4?srsltid=AfmBOop0X9hziYTTYZ_jSaes1SxMpDAtyn-VLkZcTZrsdj6G45jyuRG1
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AnswerDTO {
    private  Integer id;
    private  String content;
    private  LocalDateTime createDate;
    private  Integer questionId; //Question과의 연관 관계는 questionId로 표현(연관관계 단순화)
    private  String username;

}