package com.mysite.sbb.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("topCategories", categoryService.getRootCategories());
        return "category_list";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name, @RequestParam(required = false) Long parentId) {
        categoryService.saveCategory(name, parentId);
        return "redirect:/admin/category/list";
    }
}
