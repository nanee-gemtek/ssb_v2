package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
//@Setter
//@Builder
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    @ManyToOne
    private Question question;



    @Builder
    public Answer(String content,Question question){
        this.content = content;
        this.question = question;
        this.createDate = LocalDateTime.now();
    }

    // jpq를 위한 기본 생성자 (필수)
    protected Answer(){}


}
