package com.mysite.sbb.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 루트 목록만
    List<Category> findByParentIsNullOrderBySortOrderAscNameAsc(); // 상위 카테고리만 조회

    // 특정 부모의 자식들(형제 목록)
    List<Category> findByParentIdOrderBySortOrderAscNameAsc(Long parentId); // 특정 상위의 하위 카테고리 조회


    // 같은 부모 아래에서의 이름 중복 검사 (대소문자 무시 원하면 IgnoreCase)
    boolean existsByParentIdAndName(Long parentId, String name);
    boolean existsByParentIsNullAndName(String name);

    // 제품이 달린 카테고리 id (직접 연결만)
    @Query("select distinct c.id from Category c join Product p on p.category = c")
    List<Long> findCategoryIdsHavingProducts();

    // 필터링된 루트만 (루트 + id in)
    List<Category> findByParentIsNullAndIdInOrderBySortOrderAscNameAsc(Collection<Long> ids);

    // 특정 부모의 '표시 대상 id들'만
    List<Category> findByParentIdAndIdInOrderBySortOrderAscNameAsc(Long parentId, Collection<Long> ids);


    /* admin */
    // 루트 + 직계 자식 한 번에 (N+1 방지)
    @Query("select distinct p from Category p " +
            "left join fetch p.children c " +
            "where p.parent is null " +
            "order by p.sortOrder asc, p.name asc, c.sortOrder asc, c.name asc")
    List<Category> findRootsWithChildren();

    long countByParentId(Long parentId); // ⭐ 선택 부모의 직계 자식 수

    // parentId 검사 편의를 위한 조회
    @Query("select c from Category c where c.id in :ids")
    List<Category> findAllByIds(@Param("ids") Collection<Long> ids);

    // 부모가 기대값과 같은지 검사 (다르면 1 이상)
    @Query("select count(c.id) from Category c " +
            "where c.id in :ids and " +
            "((:parentId is null and c.parent is not null) or (:parentId is not null and c.parent.id <> :parentId))")
    long countIdsWithDifferentParent(@Param("parentId") Long parentId, @Param("ids") Collection<Long> ids);

    // 개별 정렬값 업데이트(불필요한 UPDATE 줄이기 위해)
    @Modifying
    @Query("update Category c set c.sortOrder = :sortOrder where c.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);


    // parentId 범위 내 최대 sortOrder
    @Query("select max(c.sortOrder) from Category c where " +
            "(:parentId is null and c.parent is null) or " +
            "(:parentId is not null and c.parent.id = :parentId)")
    Integer findMaxSortOrderByParentId(@Param("parentId") Long parentId);

}
