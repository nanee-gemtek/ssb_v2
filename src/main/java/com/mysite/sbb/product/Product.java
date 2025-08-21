package com.mysite.sbb.product;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.producer.Producer;
import com.mysite.sbb.user.SiteUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;            // 제품명
    private String title;           // 타이틀
    private String subtitle;        // 서브타이틀

    @Column(columnDefinition = "TEXT")
    private String applicationArea; // 적용분야

    @Column(columnDefinition = "TEXT")
    private String advantages;      // 장점 (ul/li로 작성)

    @Column(columnDefinition = "TEXT")
    private String specifications;  // 기술사양 (표)

    @Column(columnDefinition = "TEXT")
    private String certification;   // 승인 (표)

    @Column(columnDefinition = "TEXT")
    private String displayControl;  // 디스플레이 및 조정 (표)

    private LocalDateTime createDate;

    @ManyToOne
    private SiteUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 상/하위 카테고리

    @ManyToOne
    private Producer producer; // 제조사

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();
}
