package com.mysite.sbb.support;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportDTO {

    private Integer id;
    private String companyName;
    private String name;
    private String phone;
    private String email;
    private String message;
    private String messagePreview; // 리스트용 미리보기(서버에서 자름)
    private boolean privacyConsent;

    private String attachmentPath;
    private String attachmentOriginal;
    private Long attachmentSize;

    private LocalDateTime createDate;


    // 계산 프로퍼티: 필드 없이 제공
    public boolean isHasAttachment() {
        return attachmentPath != null && !attachmentPath.isBlank();
    }
}