package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class})
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target="answerList", source="answerList")
    QuestionDTO toDTO(Question question);

    @Mapping(target="answerList", source="answerList")
    Question toEntity(QuestionDTO questionDTO);

    // 엔티티 리스트 → DTO 리스트 변환
    List<QuestionDTO> toDtoList(List<Question> questions);

    // 엔티티 페이지 → DTO 페이지 변환
    default Page<QuestionDTO> toDTO(Page<Question> questions){
        return questions.map(this::toDTO);
    }

}
