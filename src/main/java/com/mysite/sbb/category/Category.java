package com.mysite.sbb.category;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    // 정렬용 필드 (기본 0)
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // children 컬렉션도 정렬되게 (JPA @OrderBy는 메모리 정렬)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC, name ASC")
    private List<Category> children = new ArrayList<>();

}
