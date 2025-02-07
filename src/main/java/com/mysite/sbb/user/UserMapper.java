package com.mysite.sbb.user;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel ="spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    SiteUser toEntity(UserDTO userDTO);

}
