package com.mysite.sbb.study;


import com.mysite.sbb.user.SiteUser;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Study {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;                  // 게시글 제목

    @Column(columnDefinition = "TEXT")
    private String description;            // 본문/설명글

    private LocalDateTime createDate;      // 생성일
    private LocalDateTime modifyDate;      // 수정일(선택)

    @ManyToOne
    private SiteUser author;               // 작성자

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyImage> images = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean featured;              // 메인 노출 여부

    /* 편의 메서드: 양방향 연관관계 세팅 */
    public void addImage(StudyImage image) {
        if (image == null) return;
        images.add(image);
        image.setStudy(this);
    }

    public void removeImage(StudyImage image) {
        if (image == null) return;
        images.remove(image);
        image.setStudy(null);
    }
}