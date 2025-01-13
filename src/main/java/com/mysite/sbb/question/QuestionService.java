package com.mysite.sbb.question;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.AnswerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
// final 필드만 포함하는 생성자를 자동으로 생성
// 의존성 주입을 위한 생성자를 간편한게 작성할 수 있다.
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    // 질문 목록 조회 메서드
    public List<QuestionDTO> getList() {
        return this.questionRepository.findAll().stream() //.stream()조회된 질문 목록을 스트림으로 변환(컬렉션 반복 처리에 유용)
                .map(this::convertToDTO) //스트임의 각 Question 엔티티를  QuestionDTO로 변환
                .collect(Collectors.toList()); //변환된 QuestionDTO를 리스트로 수집, 최종적으로 DTO 리스트 반환
    }

    //질문 조회
    public QuestionDTO getQuestion(Integer id){
        Optional<Question> question = this.questionRepository.findById(id);

        if(question.isPresent()){
            return convertToDTO(question.get());
        }else{
            throw new DataNotFoundException("question not found");
        }
    }

    public Question findById(Integer id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Question not found"));
    }

    //Question 엔티티를  QuestionDTO로 변환하는 도우미 메서드
    public QuestionDTO convertToDTO(Question question){

        //답변리스트 변환
        List<AnswerDTO> answerDTOList = question.getAnswerList().stream()//question 객체의 답변 리스트를 스트림으로 변환
                .map(answer -> AnswerDTO.builder()//각 Answer 엔티티를 AnnwerDTO로 변환 -> Builder 패턴으로 DTO 생성
                        .id(answer.getId())
                        .content(answer.getContent())
                        .createDate(answer.getCreateDate())
                        .build())
                .collect(Collectors.toList());//변환된 AnswerDTO 객체들을 리스트로 수집
        return  QuestionDTO.builder() //최종 QuestionDTO 생성, 반환
                .id(question.getId())
                .subject(question.getSubject())
                .content(question.getContent())
                .createDate(question.getCreateDate())
                .answerList(answerDTOList)
                .build();
    }

    public void create(QuestionDTO questionDTO) {
        Question question = new Question(questionDTO.getSubject(), questionDTO.getContent());
        questionRepository.save(question);
    }
}
