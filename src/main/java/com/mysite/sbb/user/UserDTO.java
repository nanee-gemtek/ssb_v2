package com.mysite.sbb.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private final Long id;
    private final String username;
    private final String password;
    private final String email;
}
