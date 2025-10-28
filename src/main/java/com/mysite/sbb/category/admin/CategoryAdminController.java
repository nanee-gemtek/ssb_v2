package com.mysite.sbb.category.admin;

import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.category.DeleteResult;
import com.mysite.sbb.category.OrderRequest;
import com.mysite.sbb.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    private final ProductRepository productRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long selected, Model model) {
        // 1) 부모 트리(우측 표용)
        var roots = categoryService.getRootTree();
        model.addAttribute("roots", roots);

        // 2) 선택 부모 ID 결정 (없으면 첫 루트)
        Long selectedParentId = selected;
        if (selectedParentId == null && !roots.isEmpty()) {
            selectedParentId = roots.get(0).getId();
        }
        model.addAttribute("selectedId", selectedParentId); // 우측 표 링크 강조용

        // 3) 좌측: 선택 부모의 자식 목록
        var children = categoryService.getChildren(selectedParentId);
        model.addAttribute("childrenOfSelected", children);

        // 4) 좌측: 자식별 상품 수(간단 루프)
        Map<Long, Long> productCountByCat = new LinkedHashMap<>();
        for (var ch : children) {
            long cnt = productRepository.countByCategoryId(ch.getId());
            productCountByCat.put(ch.getId(), cnt);
        }
        model.addAttribute("productCountByCat", productCountByCat);

        return "admin/category/list";
    }


    @PostMapping("/delete-parents")
    public String deleteParents(
            @RequestParam(name = "parentIds", required = false) List<Long> parentIds,
            RedirectAttributes rttr) {

        DeleteResult result = categoryService.deleteParents(parentIds);

        if (!result.getDeletedIds().isEmpty()) { // ✅ getDeletedIds()
            // ✅ id (name) 형태로 메시지 구성
            String ok = result.getDeletedIds().stream()
                    .map(id -> "#" + id + " (" + result.getDeletedNames().get(id) + ")")
                    .reduce((a,b) -> a + ", " + b)
                    .orElse("");
            rttr.addFlashAttribute("msgSuccess", "삭제 완료: " + ok);
        }
        if (!result.getFailures().isEmpty()) {    // ✅ getFailures()
            StringBuilder sb = new StringBuilder("삭제 실패:\n");
            result.getFailures().forEach((id, reason) ->
                    sb.append("#").append(id).append(" - ").append(reason).append("\n"));
            rttr.addFlashAttribute("msgError", sb.toString());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete-children")
    public String deleteChildren(
            @RequestParam(name = "childIds", required = false) List<Long> childIds,
            @RequestParam(name = "selectedParentId") Long selectedParentId,
            RedirectAttributes rttr) {

        DeleteResult result = categoryService.deleteChildren(childIds);

        if (result != null && result.getDeletedIds() != null && !result.getDeletedIds().isEmpty()) {
            String ok = result.getDeletedIds().stream()
                    .map(id -> "#" + id + " (" + result.getDeletedNames().get(id) + ")")
                    .reduce((a,b) -> a + ", " + b)
                    .orElse("");
            rttr.addFlashAttribute("msgSuccessChild", "자식 삭제 완료: " + ok);
        }
        if (result != null && result.getFailures() != null && !result.getFailures().isEmpty()) {
            StringBuilder sb = new StringBuilder("자식 삭제 실패:\n");
            result.getFailures().forEach((id, reason) ->
                    sb.append("#").append(id).append(" - ").append(reason).append("\n"));
            rttr.addFlashAttribute("msgErrorChild", sb.toString());
        }
        // 현재 보고 있던 부모를 유지
        return "redirect:/admin/categories?selected=" + selectedParentId;
    }

    /** 생성 */
    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam(required = false) Long parentId) {
        categoryService.create(name, parentId);
        // 부모 선택 유지(부모 없으면 루트)
        String suffix = (parentId == null) ? "" : "?selected=" + parentId;
        return "redirect:/admin/categories" + suffix;
    }

    /** 이름 수정 */
    @PostMapping("/{id}")
    public String rename(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) Long selectedParentId,
                         RedirectAttributes rttr) {
        try {
            categoryService.rename(id, name);
            rttr.addFlashAttribute("msgSuccess", "이름 변경 완료: #" + id + " → " + name);
            return "redirect:/admin/categories?selected=" + selectedParentId; // ✅ 수정한 부모 하이라이트 유지
        } catch (IllegalArgumentException e) { // 중복명 등 서비스에서 던진 경우
            rttr.addFlashAttribute("msgError", "이름 변경 실패: " + e.getMessage());
            return "redirect:/admin/categories?selected=" + selectedParentId;
        }
    }

    @PostMapping("/{parentId}/children/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderChildren(@PathVariable Long parentId,
                                             @RequestBody OrderRequest req) {
        categoryService.reorderSiblings(parentId, req.getOrderedIds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/root/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderRoot(@RequestBody OrderRequest req) {
        categoryService.reorderSiblings(null, req.getOrderedIds());
        return ResponseEntity.ok().build();
    }

}