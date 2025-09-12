package com.mysite.sbb.category;

import com.mysite.sbb.product.Product;
import com.mysite.sbb.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    /** 좌측 사이드바: 루트와(상위) 각 루트의 직계 자식 목록 */
    public SidebarData getSidebarData() {
        List<Category> roots = categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
        Map<Long, List<Category>> childrenMap = new LinkedHashMap<>();
        for (Category r : roots) {
            childrenMap.put(r.getId(), categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(r.getId()));
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
            return productRepository.findAll(pageable);
        }

        // 부모 + 모든 하위 카테고리 id 수집
        List<Long> allIds = collectDescendantIdsBFS(categoryId);

        // 해당 id들의 제품을 한 번에 조회
        return productRepository.findByCategoryIdIn(allIds, pageable);
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
        return categoryRepository.findByParentIdOrderBySortOrderAscNameAsc(parentId);
    }

    public Category saveCategory(String name, Long parentId) {
        Category parent = parentId != null ? categoryRepository.findById(parentId).orElse(null) : null;
        Category category = Category.builder().name(name).parent(parent).build();
        return categoryRepository.save(category);
    }
}
