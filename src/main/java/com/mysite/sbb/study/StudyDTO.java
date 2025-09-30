package com.mysite.sbb.study;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDTO {

    private Integer id;

    // 본문 정보
    private String title;           // 게시글 제목
    private String summary;         // 요약(선택)
    private String description;     // 본문/설명글

    // 작성/표시 정보
    private String username;        // 작성자 아이디/닉네임
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    // 메인 노출 여부 (null이면 false로 취급)
    private Boolean featured;

    // [1] 업로드 이미지 수신용
    private List<MultipartFile> images;

    // [2] 저장된 이미지 경로(URL) 표시용
    private List<String> imagePaths;

    /** 썸네일 URL 편의 메서드 */
    public String getThumbnailUrl() {
        if (imagePaths == null || imagePaths.isEmpty()
                || imagePaths.get(0) == null || imagePaths.get(0).isBlank()) {
            return "/images/product-placeholder.png";
        }
        return imagePaths.get(0);
    }

    /** featured null-safe 접근 */
    public boolean isFeaturedOrFalse() {
        return featured != null && featured;
    }
}