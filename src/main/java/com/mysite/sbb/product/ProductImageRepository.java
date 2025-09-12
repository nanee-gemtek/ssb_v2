package com.mysite.sbb.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    Page<ProductImage> findAll(Pageable pageable);
    // 1) 파일 삭제를 위해 이미지 경로만 미리 수집
    @Query("select pi.imagePath from ProductImage pi where pi.product.id in :productIds")
    List<String> findPathsByProductIdIn(@Param("productIds") List<Integer> productIds);

    // 2) 자식 이미지 벌크 삭제
    @Modifying
    @Query("delete from ProductImage pi where pi.product.id in :productIds")
    void deleteByProductIdIn(@Param("productIds") List<Integer> productIds);
}
