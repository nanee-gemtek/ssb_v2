package com.mysite.sbb.product.admin;

import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.product.ProductDTO;
import com.mysite.sbb.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin/product")
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        model.addAttribute("paging", productService.getList(page - 1));
        return "admin/product/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        ProductDTO product = productService.getProduct(id);
        model.addAttribute("product", product);
        return "admin/product/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("topCategories", categoryService.getTopCategories());
        return "admin/product/form";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto, Principal principal) {
        dto.setUsername(principal.getName());

        productService.create(dto);
        return "redirect:list";
    }




}
