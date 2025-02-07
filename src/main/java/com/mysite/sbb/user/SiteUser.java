package com.mysite.sbb.user;

import com.mysite.sbb.answer.Answer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    private String password;

    @Column(unique = true)
    private String email;
}
