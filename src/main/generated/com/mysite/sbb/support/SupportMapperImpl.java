package com.mysite.sbb.support;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class SupportMapperImpl implements SupportMapper {

    @Override
    public SupportDTO toDTO(Support support) {
        if ( support == null ) {
            return null;
        }

        SupportDTO.SupportDTOBuilder supportDTO = SupportDTO.builder();

        supportDTO.messagePreview( preview40( support.getMessage() ) );
        supportDTO.id( support.getId() );
        supportDTO.companyName( support.getCompanyName() );
        supportDTO.name( support.getName() );
        supportDTO.phone( support.getPhone() );
        supportDTO.email( support.getEmail() );
        supportDTO.message( support.getMessage() );
        supportDTO.privacyConsent( support.isPrivacyConsent() );
        supportDTO.attachmentPath( support.getAttachmentPath() );
        supportDTO.attachmentOriginal( support.getAttachmentOriginal() );
        supportDTO.attachmentSize( support.getAttachmentSize() );
        supportDTO.createDate( support.getCreateDate() );

        return supportDTO.build();
    }

    @Override
    public Support toEntity(SupportDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Support.SupportBuilder support = Support.builder();

        support.id( dto.getId() );
        support.companyName( dto.getCompanyName() );
        support.name( dto.getName() );
        support.phone( dto.getPhone() );
        support.email( dto.getEmail() );
        support.message( dto.getMessage() );
        support.privacyConsent( dto.isPrivacyConsent() );
        support.attachmentPath( dto.getAttachmentPath() );
        support.attachmentOriginal( dto.getAttachmentOriginal() );
        support.attachmentSize( dto.getAttachmentSize() );
        support.createDate( dto.getCreateDate() );

        return support.build();
    }

    @Override
    public List<SupportDTO> toDtoList(List<Support> list) {
        if ( list == null ) {
            return null;
        }

        List<SupportDTO> list1 = new ArrayList<SupportDTO>( list.size() );
        for ( Support support : list ) {
            list1.add( toDTO( support ) );
        }

        return list1;
    }
}
