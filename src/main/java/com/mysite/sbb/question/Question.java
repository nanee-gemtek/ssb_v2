package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

//@Data
@Getter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto increse 적용
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT") //텍스트를 열데이터로 넣을수 있고, 글자수를 제한 할 수 없는 경우 사용
    private String content;

    private LocalDateTime createDate;

    //CascadeType.REMOVE 질문삭제시 그에 달린 답변도 삭제
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answerList;

    //엔티티 카멜표기 -> 언더바로 변경

    //Builder 패턴으로  초기화(객체생성) 시 아래 코드 불필요


    @Builder
    public Question(String subject, String content,List<Answer> answerList){
        this.subject = subject;
        this.content = content;
        this.answerList = answerList;
        this.createDate = LocalDateTime.now();
    }

    public void updateContent(String subject, String content){
        this.subject = subject;
        this.content = content;
    }

    // JPA를 위한 기본 생성자 (필수)
    protected Question() {}


}
