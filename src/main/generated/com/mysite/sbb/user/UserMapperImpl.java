package com.mysite.sbb.user;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-22T14:18:38+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 11.0.19 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public SiteUser toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        SiteUser.SiteUserBuilder siteUser = SiteUser.builder();

        siteUser.id( userDTO.getId() );
        siteUser.username( userDTO.getUsername() );
        siteUser.password( userDTO.getPassword() );
        siteUser.email( userDTO.getEmail() );

        return siteUser.build();
    }

    @Override
    public UserDTO toDTO(SiteUser user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );
        userDTO.password( user.getPassword() );
        userDTO.email( user.getEmail() );

        return userDTO.build();
    }
}
