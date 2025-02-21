package com.mysite.sbb.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

//@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password; //SHA1

    //private String apiPassword; //BCrypt

    @Column(unique = true)
    private String email;
}
