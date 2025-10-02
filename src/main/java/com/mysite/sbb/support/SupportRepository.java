package com.mysite.sbb.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupportRepository extends JpaRepository<Support, Integer> {
    Page<Support> findAll(Pageable pageable);


    // 문서 경로 수집용(이미지와 별개로)
    @Query("select p from Support p where p.id in :ids")
    List<Support> findAllForDocPaths(@Param("ids") List<Integer> id);


}
