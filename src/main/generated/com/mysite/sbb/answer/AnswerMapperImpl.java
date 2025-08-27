package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserMapper;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapperImpl implements AnswerMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public AnswerDTO toDTO(Answer answer) {
        if ( answer == null ) {
            return null;
        }

        AnswerDTO.AnswerDTOBuilder answerDTO = AnswerDTO.builder();

        answerDTO.questionId( answerQuestionId( answer ) );
        answerDTO.username( answerAuthorUsername( answer ) );
        answerDTO.id( answer.getId() );
        answerDTO.content( answer.getContent() );
        answerDTO.createDate( answer.getCreateDate() );

        return answerDTO.build();
    }

    @Override
    public Answer toEntity(AnswerDTO answerDTO) {
        if ( answerDTO == null ) {
            return null;
        }

        Answer.AnswerBuilder answer = Answer.builder();

        answer.question( idToQuestion( answerDTO.getQuestionId() ) );
        answer.author( userMapper.nameToSiteUser( answerDTO.getUsername() ) );
        answer.id( answerDTO.getId() );
        answer.content( answerDTO.getContent() );
        answer.createDate( answerDTO.getCreateDate() );

        return answer.build();
    }

    private Integer answerQuestionId(Answer answer) {
        if ( answer == null ) {
            return null;
        }
        Question question = answer.getQuestion();
        if ( question == null ) {
            return null;
        }
        Integer id = question.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String answerAuthorUsername(Answer answer) {
        if ( answer == null ) {
            return null;
        }
        SiteUser author = answer.getAuthor();
        if ( author == null ) {
            return null;
        }
        String username = author.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
