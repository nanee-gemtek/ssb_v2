package com.mysite.sbb.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Integer> {

    /* 기본 페이지 조회 (JpaRepository에도 있으나, 명시적으로 선언) */
    Page<Study> findAll(Pageable pageable);

    /* 이미지까지 즉시 로딩해서 가져오기 (ID 목록 기반) */
    @Query("select distinct s from Study s left join fetch s.images where s.id in :ids")
    List<Study> findAllWithImagesByIdIn(@Param("ids") List<Integer> ids);

    /* featured Top N ID만 네이티브로 먼저 가져오기 (정렬: 생성일 내림차순) */
    @Query(
            value = "select s.id from study s where s.featured = 1 order by s.create_date desc limit 10",
            nativeQuery = true
    )
    List<Integer> findFeaturedIds();

    /* featured ID들로 연관(이미지)까지 한 번에 페치 */
    @Query("select distinct s from Study s " +
            "left join fetch s.images " +
            "where s.id in :ids")
    List<Study> findByIdInWithAll(@Param("ids") List<Integer> ids);

    /* 선택: featured 페이징(JPQL) - 필요 시 사용 */
    @EntityGraph(attributePaths = {"images"})
    Page<Study> findByFeaturedTrue(Pageable pageable);
}
