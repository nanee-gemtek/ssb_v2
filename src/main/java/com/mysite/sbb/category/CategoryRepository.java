package com.mysite.sbb.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull(); // 상위 카테고리만 조회
    List<Category> findByParentId(Long parentId); // 특정 상위의 하위 카테고리 조회
}
