package com.mysite.sbb.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAll(Pageable pageable);

    Page<Product> findAllByOrderByCreateDateDesc(Pageable pageable);


    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);


    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdInOrderByCreateDateDesc(Collection<Long> categoryIds, Pageable pageable);


    @Query("select distinct p from Product p left join fetch p.images where p.id in :ids")
    List<Product> findAllWithImagesByIdIn(@Param("ids") List<Integer> ids);


    // 문서 경로 수집용(이미지와 별개로)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllForDocPaths(@Param("ids") List<Integer> id);


    /* ✅ 1단계: featured ID만 Top N (Pageable로 N 조절) */
    @Query(
            value = "select p.id from product p where p.featured = 1 order by p.create_date desc limit 10",
            nativeQuery = true
    )
    List<Integer> findFeaturedIds();

    /* ✅ 2단계: ID들로 필요한 연관을 한 번에 fetch (이미지/제조사/카테고리까지) */
    @Query("select distinct p from Product p " +
            "left join fetch p.images " +
            "left join fetch p.producer " +
            "left join fetch p.category " +
            "where p.id in :ids")
    List<Product> findByIdInWithAll(@Param("ids") List<Integer> ids);
}
