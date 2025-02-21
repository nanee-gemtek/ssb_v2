package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionMapper;
import com.mysite.sbb.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class, UserMapper.class})
public interface AnswerMapper {
    AnswerMapper INSTANCE = Mappers.getMapper(AnswerMapper.class);

    @Mapping(source="question.id", target = "questionId")
    @Mapping(source="author.username", target = "username") // 추가
    AnswerDTO toDTO(Answer answer);

    @Mapping(source = "questionId", target = "question", qualifiedByName = "idToQuestion")
    @Mapping(source = "username", target = "author", qualifiedByName = "nameToSiteUser") // 추가
    Answer toEntity(AnswerDTO answerDTO);

    @Named("idToQuestion")
    default Question idToQuestion(Integer id){
        if(id == null) return null;
        return Question.builder().id(id).build(); //Question의 참조만 생성
    }


}
