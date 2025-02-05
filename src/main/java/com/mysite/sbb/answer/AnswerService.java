package com.mysite.sbb.answer;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;

    public void create(AnswerDTO answerDTO) {
        //1. DTO → Entity 변환


        // 2. 실제 Question 엔티티 조회 (옵션)
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                        .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // 2. Answer 객체 생성 (setter 없이 생성자/빌더 사용)
        /*
        Answer answer = Answer.createAnswer(
                answerDTO.getContent(),
                question
        );
         */

        // 2. Answer 객체 생성(빌더패턴만 사용)
        Answer answer = Answer.builder()
                .content(answerDTO.getContent())
                .createDate(LocalDateTime.now())
                .question(question)
                .build();

        //3. 답변저장
        answerRepository.save(answer);
    }

    /*
    public void create(AnswerDTO answerDTO,Integer questionId){
        Question question = questionService.findById(questionId);

        Answer answer = answerMapper.toEntity(answerDTO);
        answer.updateQuestion(question);
        answerRepository.save(answer);
    }
    */


    // 특정 Answer 조회 후 DTO로 반환
    public AnswerDTO getAnswer(Integer id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Answer not found"));
        return answerMapper.toDTO(answer);
        //return convertToDTO(answer);
    }

    public void delete(AnswerDTO answerDto) {
        Answer answer = answerMapper.toEntity(answerDto);
        this.answerRepository.delete(answer);
    }

}
