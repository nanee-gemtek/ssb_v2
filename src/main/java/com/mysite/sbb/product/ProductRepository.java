package com.mysite.sbb.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAll(Pageable pageable);


    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdIn(Collection<Long> categoryIds, Pageable pageable);
}
