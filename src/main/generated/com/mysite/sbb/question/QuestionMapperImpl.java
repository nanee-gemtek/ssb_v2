package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerDTO;
import com.mysite.sbb.answer.AnswerMapper;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserMapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-03T10:41:37+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 11.0.19 (Oracle Corporation)"
)
@Component
public class QuestionMapperImpl implements QuestionMapper {

    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public QuestionDTO toDTO(Question question) {
        if ( question == null ) {
            return null;
        }

        QuestionDTO.QuestionDTOBuilder questionDTO = QuestionDTO.builder();

        questionDTO.answerList( answerListToAnswerDTOList( question.getAnswerList() ) );
        questionDTO.username( questionAuthorUsername( question ) );
        questionDTO.id( question.getId() );
        questionDTO.subject( question.getSubject() );
        questionDTO.content( question.getContent() );
        questionDTO.createDate( question.getCreateDate() );

        return questionDTO.build();
    }

    @Override
    public Question toEntity(QuestionDTO questionDTO) {
        if ( questionDTO == null ) {
            return null;
        }

        Question.QuestionBuilder question = Question.builder();

        question.answerList( answerDTOListToAnswerList( questionDTO.getAnswerList() ) );
        question.author( userMapper.nameToSiteUser( questionDTO.getUsername() ) );
        question.id( questionDTO.getId() );
        question.subject( questionDTO.getSubject() );
        question.content( questionDTO.getContent() );
        question.createDate( questionDTO.getCreateDate() );

        return question.build();
    }

    @Override
    public List<QuestionDTO> toDtoList(List<Question> questions) {
        if ( questions == null ) {
            return null;
        }

        List<QuestionDTO> list = new ArrayList<QuestionDTO>( questions.size() );
        for ( Question question : questions ) {
            list.add( toDTO( question ) );
        }

        return list;
    }

    protected List<AnswerDTO> answerListToAnswerDTOList(List<Answer> list) {
        if ( list == null ) {
            return null;
        }

        List<AnswerDTO> list1 = new ArrayList<AnswerDTO>( list.size() );
        for ( Answer answer : list ) {
            list1.add( answerMapper.toDTO( answer ) );
        }

        return list1;
    }

    private String questionAuthorUsername(Question question) {
        if ( question == null ) {
            return null;
        }
        SiteUser author = question.getAuthor();
        if ( author == null ) {
            return null;
        }
        String username = author.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    protected List<Answer> answerDTOListToAnswerList(List<AnswerDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Answer> list1 = new ArrayList<Answer>( list.size() );
        for ( AnswerDTO answerDTO : list ) {
            list1.add( answerMapper.toEntity( answerDTO ) );
        }

        return list1;
    }
}
