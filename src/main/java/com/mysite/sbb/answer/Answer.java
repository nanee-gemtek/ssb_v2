package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder //객체 생성시 불변성을 유지
@NoArgsConstructor // 기본 생성자 자동으로 생성
@AllArgsConstructor //객체생성시 모든 필드를 한번에 초기화
@DynamicUpdate //업데이트시 변경된 필드만 포함하는 SQL 쿼리 생성
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



    // 정적 팩토리 메서드 추가(빌더와 함께 사용가능)
    // 서비스에서 직접 빌더 사용시 불필요
    /*
    public static Answer createAnswer(String content, Question question){
        return Answer.builder()
                .content(content)
                .createDate(LocalDateTime.now())
                .question(question)
                .build();
    }
     */
}
