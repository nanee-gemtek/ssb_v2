package com.mysite.sbb.support;


import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Support {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 사용자 입력
    @Column(length = 100, nullable = false)
    private String companyName;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 30, nullable = false)
    private String phone;

    @Email
    @Column(length = 150, nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean privacyConsent;

    // 첨부(단일)
    @Column(length = 512)
    private String attachmentPath;
    @Column(length = 255)
    private String attachmentOriginal;
    private Long attachmentSize;

    // 메타
    private LocalDateTime createDate;


    public boolean hasAttachment() {
        return attachmentPath != null && !attachmentPath.isBlank();
    }
}