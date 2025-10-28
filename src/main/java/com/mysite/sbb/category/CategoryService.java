package com.mysite.sbb.category;

import com.mysite.sbb.product.Product;
import com.mysite.sbb.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    @Transactional(readOnly = true)
    public List<Category> getRootTree() {
        return categoryRepository.findRootsWithChildren(); // 루트 + 직계 자식
    }

    @Transactional(readOnly = true)
    public List<Category> getSiblings(Long categoryId) {
        if (categoryId == null) return categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
        Category target = categoryRepository.findById(categoryId).orElse(null);
        if (target == null) return categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
        Long parentId = (target.getParent() == null) ? null : target.getParent().getId();
        return (parentId == null)
                ? categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc()
                : categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(parentId);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public SidebarData getSidebarData() {
        // 1) 제품이 직접 달린 카테고리 id
        List<Long> leafIds = categoryRepository.findCategoryIdsHavingProducts();
        if (leafIds.isEmpty()) {
            // 제품이 하나도 없다면 빈 사이드바
            return new SidebarData(List.of(), Map.of());
        }

        // 2) 조상까지 모두 포함 (findById 캐싱해서 N+1 최소화)
        Set<Long> displayIds = new LinkedHashSet<>(leafIds);
        Map<Long, Category> cache = new HashMap<>();

        for (Long id : leafIds) {
            Category cur = cache.computeIfAbsent(id, i -> categoryRepository.findById(i).orElse(null));
            while (cur != null && cur.getParent() != null) {
                Category parent = cur.getParent();
                Long pid = parent.getId();
                if (!displayIds.add(pid)) {
                    // 이미 올라간 부모면 더 올라갈 필요 없음
                    break;
                }
                // 다음 루프를 위해 부모를 캐시에 채우기
                cur = cache.computeIfAbsent(pid, i -> categoryRepository.findById(i).orElse(null));
            }
        }

        // 3) 루트(부모가 null) 중에서도 표시 대상에 포함된 것만 노출
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIdInOrderBySortOrderAscNameAsc(displayIds);

        // 4) 각 루트의 '표시 대상' 자식만 노출
        Map<Long, List<Category>> childrenMap = new LinkedHashMap<>();
        for (Category r : roots) {
            List<Category> children = categoryRepository
                    .findByParentIdAndIdInOrderBySortOrderAscNameAsc(r.getId(), displayIds);
            childrenMap.put(r.getId(), children);
        }

        return new SidebarData(roots, childrenMap);
    }

    /** 사이드바 루트 카테고리 */
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
    }

    /** 선택 카테고리(자기 자신 + 모든 하위)를 한 번에 제품 조회 */
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            // 카테고리 지정 안 됨 → 전체 조회
            //return productRepository.findAll(pageable);
            return productRepository.findAllByOrderByCreateDateDesc(pageable);
        }

        // 부모 + 모든 하위 카테고리 id 수집
        List<Long> allIds = collectDescendantIdsBFS(categoryId);

        // 해당 id들의 제품을 한 번에 조회
        //return productRepository.findByCategoryIdIn(allIds, pageable);
        return productRepository.findByCategoryIdInOrderByCreateDateDesc(allIds, pageable);
    }

    /**
     * 현재 가진 메서드만 사용한 BFS.
     * 쿼리 횟수 = 각 레벨의 부모 수(노드 수)에 비례.
     * 트리가 큰 경우 아래의 "개선 제안" 참고.
     */
    private List<Long> collectDescendantIdsBFS(Long rootId) {
        List<Long> result = new ArrayList<>();
        List<Long> frontier = List.of(rootId); // 이번 레벨의 부모 후보

        while (!frontier.isEmpty()) {
            result.addAll(frontier);

            // 다음 레벨 수집
            List<Long> next = new ArrayList<>();
            for (Long pid : frontier) {
                // pid의 '직계 자식' 가져와서 id만 추출
                List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(pid);
                for (Category c : children) next.add(c.getId());
            }
            frontier = next;
        }
        return result;
    }

    public List<Category> getChildren(Long parentId) {
        if (parentId == null) return List.of();
        return categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(parentId);
    }

    public Category saveCategory(String name, Long parentId) {
        Category parent = parentId != null ? categoryRepository.findById(parentId).orElse(null) : null;
        Category category = Category.builder().name(name).parent(parent).build();
        return categoryRepository.save(category);
    }

    @Transactional
    public DeleteResult deleteParents(List<Long> parentIds) {
        List<Long> deleted = new ArrayList<>();
        Map<Long, String> deletedNames = new LinkedHashMap<>(); // ✅ 추가
        Map<Long, String> fail = new LinkedHashMap<>();

        if (parentIds == null) return new DeleteResult(deleted,deletedNames ,fail);

        for (Long id : parentIds) {
            Optional<Category> opt = categoryRepository.findById(id);
            if (opt.isEmpty()) {
                fail.put(id, "존재하지 않음");
                continue;
            }
            Category c = opt.get();                 // ✅ 이름 확보는 삭제 전에
            long childCnt = categoryRepository.countByParentId(id);
            long productCnt = productRepository.countByCategoryId(id);

            if (childCnt > 0) {
                fail.put(id, "자식 카테고리가 있어 삭제 불가 (" + childCnt + "개)");
                continue;
            }
            if (productCnt > 0) {
                fail.put(id, "해당 카테고리에 상품이 있어 삭제 불가 (" + productCnt + "개)");
                continue;
            }

            categoryRepository.deleteById(id);
            deleted.add(id);
            deletedNames.put(id, c.getName());      // ✅ 이름 기록
        }
        return new DeleteResult(deleted,deletedNames, fail);
    }


    @Transactional
    public DeleteResult deleteChildren(List<Long> childIds) {
        List<Long> deleted = new ArrayList<>();
        Map<Long, String> deletedNames = new LinkedHashMap<>(); // ✅ 추가
        Map<Long, String> fail = new LinkedHashMap<>();

        if (childIds == null || childIds.isEmpty()) {
            fail.put(-1L, "선택된 자식 카테고리가 없습니다.");
            return new DeleteResult(deleted,deletedNames, fail);
        }

        for (Long id : childIds) {
            var opt = categoryRepository.findById(id);
            if (opt.isEmpty()) { fail.put(id, "존재하지 않음"); continue; }

            Category c = opt.get();                 // ✅ 이름 확보
            long childCnt   = categoryRepository.countByParentId(id);             // 손자 이상 방지
            long productCnt = productRepository.countByCategoryId(id);

            if (childCnt > 0)   { fail.put(id, "하위 카테고리가 있어 삭제 불가 ("+childCnt+"개)"); continue; }
            if (productCnt > 0) { fail.put(id, "해당 카테고리에 상품이 있어 삭제 불가 ("+productCnt+"개)"); continue; }

            categoryRepository.deleteById(id);
            deleted.add(id);
            deletedNames.put(id, c.getName());      // ✅ 이름 기록
        }
        return new DeleteResult(deleted,deletedNames, fail);
    }


    /** 생성: parentId=null 이면 루트. 맨 뒤(최대+10)에 배치 */
    @Transactional
    public Category create(String name, Long parentId) {
        // 중복 이름(같은 부모 내) 방지(필요 없으면 제거 가능)
        if (parentId == null) {
            if (categoryRepository.existsByParentIsNullAndName(name)) {
                throw new IllegalArgumentException("이미 존재하는 루트 카테고리 이름입니다: " + name);
            }
        } else {
            if (categoryRepository.existsByParentIdAndName(parentId, name)) {
                throw new IllegalArgumentException("해당 부모 아래 이미 존재하는 이름입니다: " + name);
            }
        }

        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId).orElseThrow(() -> new NoSuchElementException("부모가 없음: " + parentId));
        }

        Integer max = categoryRepository.findMaxSortOrderByParentId(parentId);
        int next = (max == null ? 10 : max + 10);

        Category c = new Category();
        c.setName(name);
        c.setParent(parent);
        c.setSortOrder(next);

        return categoryRepository.save(c);
    }

    /** 이름 수정만 (부모 변경은 스코프 외) */
    @Transactional
    public void rename(Long id, String newName) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new NoSuchElementException("카테고리 없음: " + id));

        Long parentId = (c.getParent() == null ? null : c.getParent().getId());
        // 같은 부모 내 중복 방지
        boolean dup = (parentId == null)
                ? categoryRepository.existsByParentIsNullAndName(newName)
                : categoryRepository.existsByParentIdAndName(parentId, newName);

        if (dup && !Objects.equals(c.getName(), newName)) {
            throw new IllegalArgumentException("동일 부모 하에 중복 이름입니다: " + newName);
        }

        c.setName(newName);
        // JPA dirty checking으로 업데이트
    }

    /**
     * 형제 재정렬: parentId 아래의 형제들을 orderedIds 순서대로 10,20,30… 부여
     * - 모든 id가 동일 parentId를 가져야 함(안전성 검사)
     */
    @Transactional
    public void reorderSiblings(Long parentId, List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) return;

        // 1) 요청 id들이 전부 같은 부모에 속하는지 검증
        long mismatch = categoryRepository.countIdsWithDifferentParent(parentId, orderedIds);
        if (mismatch > 0) {
            throw new IllegalArgumentException("부모가 다른 항목이 포함되어 있습니다.");
        }

        // 2) 실제 존재 개수 검증(옵션)
        List<Category> found = categoryRepository.findAllByIds(orderedIds);
        if (found.size() != orderedIds.size()) {
            throw new NoSuchElementException("존재하지 않는 카테고리 id가 포함되어 있습니다.");
        }

        // 3) 간격 10으로 일괄 재부여
        int order = 10;
        for (Long id : orderedIds) {
            categoryRepository.updateSortOrder(id, order);
            order += 10;
        }
    }

}
