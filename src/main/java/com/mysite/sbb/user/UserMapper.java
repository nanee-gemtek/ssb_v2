package com.mysite.sbb.user;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel ="spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    SiteUser toEntity(UserDTO userDTO);


    // 엔티티 페이지 → DTO 페이지 변환
    /*@Mapping(source="question.id", target = "questionId")*/

    UserDTO toDTO(SiteUser user);

    @Named("nameToSiteUser")
    default SiteUser nameToSiteUser(String userName){
        if(userName == null) return null;
        return SiteUser.builder().username(userName).build(); //SiteUser의 참조만 생성
    }


}
