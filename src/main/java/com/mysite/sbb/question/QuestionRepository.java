package com.mysite.sbb.question;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/*엔티티가 데이터베이스 테이블을 생성했다면,
리포지터리는 이와 같이 생성된 데이터베이스 테이블의 데이터들을 저장,조회, 수정, 삭제할 수 있도록 도와 주는 인터페이스
* */
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);

    List<Question> findBySubjectLike(String subject);

    Page<Question> findAll(Pageable pageable);//Pageable 객체를 입력받아 Page<Question>타입객체를 리턴하는 findAll 메서드
}


