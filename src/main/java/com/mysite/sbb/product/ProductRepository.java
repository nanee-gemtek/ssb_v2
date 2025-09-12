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


    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);

    @Query("select distinct p from Product p left join fetch p.images where p.id in :ids")
    List<Product> findAllWithImagesByIdIn(@Param("ids") List<Integer> ids);


    // 문서 경로 수집용(이미지와 별개로)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllForDocPaths(@Param("ids") List<Integer> id);
}
