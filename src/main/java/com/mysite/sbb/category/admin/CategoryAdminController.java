package com.mysite.sbb.category.admin;

import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.category.DeleteResult;
import com.mysite.sbb.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            rttr.addFlashAttribute("msgSuccess", "삭제 완료: " + result.getDeletedIds());
        }
        if (!result.getFailures().isEmpty()) {    // ✅ getFailures()
            StringBuilder sb = new StringBuilder("삭제 실패:\n");
            result.getFailures().forEach((id, reason) ->
                    sb.append("#").append(id).append(" - ").append(reason).append("\n"));
            rttr.addFlashAttribute("msgError", sb.toString());
        }
        return "redirect:/admin/categories";
    }

}