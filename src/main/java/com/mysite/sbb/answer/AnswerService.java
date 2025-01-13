package com.mysite.sbb.answer;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionDTO;
import com.mysite.sbb.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;

    public void create(AnswerDTO answerDTO,Integer id){
        Question question = questionService.findById(id);
        Answer answer = new Answer(answerDTO.getContent(),question);
        answerRepository.save(answer);
    }

    private AnswerDTO convertToDTO(Answer answer){
        return AnswerDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .question(questionService.convertToDTO(answer.getQuestion()))
                .createDate(answer.getCreateDate())
                .build();
    }

    // 특정 Answer 조회 후 DTO로 반환
    public AnswerDTO getAnswer(Integer id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Answer not found"));
        return convertToDTO(answer);
    }

}
