package com.mysite.sbb.study;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserMapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class StudyMapperImpl implements StudyMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public StudyDTO toDTO(Study study) {
        if ( study == null ) {
            return null;
        }

        StudyDTO.StudyDTOBuilder studyDTO = StudyDTO.builder();

        studyDTO.username( studyAuthorUsername( study ) );
        studyDTO.id( study.getId() );
        studyDTO.title( study.getTitle() );
        studyDTO.description( study.getDescription() );
        studyDTO.createDate( study.getCreateDate() );
        studyDTO.modifyDate( study.getModifyDate() );
        studyDTO.featured( study.isFeatured() );

        studyDTO.imagePaths( mapImagePaths(study) );

        return studyDTO.build();
    }

    @Override
    public Study toEntity(StudyDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Study.StudyBuilder study = Study.builder();

        study.author( userMapper.nameToSiteUser( dto.getUsername() ) );
        study.id( dto.getId() );
        study.title( dto.getTitle() );
        study.description( dto.getDescription() );
        study.createDate( dto.getCreateDate() );
        study.modifyDate( dto.getModifyDate() );
        if ( dto.getFeatured() != null ) {
            study.featured( dto.getFeatured() );
        }

        return study.build();
    }

    @Override
    public List<StudyDTO> toDtoList(List<Study> list) {
        if ( list == null ) {
            return null;
        }

        List<StudyDTO> list1 = new ArrayList<StudyDTO>( list.size() );
        for ( Study study : list ) {
            list1.add( toDTO( study ) );
        }

        return list1;
    }

    private String studyAuthorUsername(Study study) {
        if ( study == null ) {
            return null;
        }
        SiteUser author = study.getAuthor();
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
