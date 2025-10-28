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
        Map<Long, String> fail = new LinkedHashMap<>();

        if (parentIds == null) return new DeleteResult(deleted, fail);

        for (Long id : parentIds) {
            Optional<Category> opt = categoryRepository.findById(id);
            if (opt.isEmpty()) {
                fail.put(id, "존재하지 않음");
                continue;
            }
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
        }
        return new DeleteResult(deleted, fail);
    }

}
