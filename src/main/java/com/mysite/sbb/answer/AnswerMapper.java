package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = QuestionMapper.class)
public interface AnswerMapper {
    AnswerMapper INSTANCE = Mappers.getMapper(AnswerMapper.class);

    @Mapping(source="question.id", target = "questionId")
    AnswerDTO toDTO(Answer answer);

    // AnswerDTO -> Answer 변환 (Question연결은 Service에서 처리)
    //@Mapping(target = "question", ignore = true) //Question은 수동으로 연결
    @Mapping(source = "questionId", target = "question", qualifiedByName = "idToQuestion")
    Answer toEntity(AnswerDTO answerDTO);

    @Named("idToQuestion")
    default Question idToQuestion(Integer id){
        if(id == null) return null;
        return Question.builder().id(id).build(); //Question의 참조만 생성
    }
}
