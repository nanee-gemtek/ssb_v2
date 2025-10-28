package com.mysite.sbb.category.admin;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long selected, Model model) {
        List<Category> roots = categoryService.getRootTree();
        model.addAttribute("roots", roots);
        model.addAttribute("selectedId", selected);
        model.addAttribute("siblings", categoryService.getSiblings(selected));

        return "admin/category/list";
    }
}