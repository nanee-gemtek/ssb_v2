package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class})
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target="answerList", source="answerList")
    QuestionDTO toDTO(Question question);

    @Mapping(target="answerList", source="answerList")
    Question toEntity(QuestionDTO questionDTO);

}
