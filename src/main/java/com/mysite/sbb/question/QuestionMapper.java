package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerMapper;
import com.mysite.sbb.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AnswerMapper.class, UserMapper.class})
public interface QuestionMapper {
    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target="answerList", source="answerList")
    @Mapping(source="author.username", target = "username") // 추가
    QuestionDTO toDTO(Question question);

    @Mapping(target="answerList", source="answerList")
    @Mapping(source = "username", target = "author", qualifiedByName = "nameToSiteUser") // 추가
    Question toEntity(QuestionDTO questionDTO);

    // 엔티티 리스트 → DTO 리스트 변환
    List<QuestionDTO> toDtoList(List<Question> questions);

    // 엔티티 페이지 → DTO 페이지 변환
    default Page<QuestionDTO> toDTO(Page<Question> questions){
        return questions.map(this::toDTO);
    }


}
