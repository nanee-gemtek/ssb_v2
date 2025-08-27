package com.mysite.sbb.product;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer id;
    private String name;
    private String title;
    private String subtitle;
    private String applicationArea;
    private String advantages;
    private String specifications; //기술 사양
    private String approval;//승인
    private String displayControl;//디스플레이 및 조정
    private String username;
    private LocalDateTime createDate;
    private Long producerId;
    private Long categoryId;
    private String producerName;
    private String categoryName;
    private String configLink;


    // 🔽 [1] 사용자가 업로드한 이미지 수신용
    private List<MultipartFile> images;
    // 🔽 [2] 저장된 이미지 경로 표시용 (화면에 <img>로 출력할 URL들)
    private List<String> imagePaths; // ProductImage의 imagePath 값들

    // 문서 업로드(단일 파일)
    private MultipartFile specDoc;            // 사양서
    private MultipartFile operatingDoc;       // 사용설명서

    // 저장된 문서 경로(URL)
    private String specDocPath;               // 예: /uploadDocs/abcd.pdf
    private String operatingDocPath;

    public String getThumbnailUrl() {
        if (imagePaths == null || imagePaths.isEmpty() || imagePaths.get(0) == null || imagePaths.get(0).isBlank()) {
            return "/images/product-placeholder.png"; // 정적 기본 이미지
        }
        return imagePaths.get(0);
    }
}
