package com.mysite.sbb.support;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 문서 업로드(단일 파일)
    private MultipartFile attachment;
    // 저장된 문서 경로(URL)
    private String attachmentPath; // 예: /uploadDocs/abcd.pdf
    private Long attachmentSize;
    private String attachmentOriginal;

    private LocalDateTime createDate;


    // 계산 프로퍼티: 필드 없이 제공
    public boolean isHasAttachment() {
        return attachmentPath != null && !attachmentPath.isBlank();
    }
}