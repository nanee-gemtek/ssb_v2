package com.mysite.sbb.study;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_studyimage_study_sort", columnNames = {"study_id", "sort_order"})
        }
)
public class StudyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String imagePath;          // 이미지 경로 (예: /uploadImages/uuid.png)

    @Column(length = 255)
    private String altText;            // 대체 텍스트(접근성/SEO용, 선택)

    @Column(length = 255)
    private String caption;            // 캡션(선택)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;     // 정렬 순서

    @PrePersist
    public void prePersist() {
        if (sortOrder == null) sortOrder = 0;
    }
}