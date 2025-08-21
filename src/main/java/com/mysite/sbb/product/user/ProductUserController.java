package com.mysite.sbb.product.user;

import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.category.SidebarData;
import com.mysite.sbb.product.ProductDTO;
import com.mysite.sbb.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("user/product")
public class ProductUserController {

    private final ProductService productService;
    private final CategoryService categoryService;

//    @GetMapping("/list")
//    public String list(Model model, @RequestParam(value="page", defaultValue="1") int page) {
//        model.addAttribute("paging", productService.getList(page - 1));
//        return "user/product/list";
//    }

    @GetMapping("/list")
    public String list(@RequestParam(value = "cat", required = false) Long categoryId,
                       @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {

        SidebarData side = categoryService.getSidebarData();
        model.addAttribute("roots", side.getRoots());
        model.addAttribute("childrenMap", side.getChildrenMap());
        model.addAttribute("selectedCategoryId", categoryId);

        model.addAttribute("paging", categoryService.getProductsByCategory(categoryId, pageable));
        return "user/product/list";
    }



    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        ProductDTO product = productService.getProduct(id);
        model.addAttribute("product", product);
        return "user/product/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("topCategories", categoryService.getRootCategories());
        return "product_form";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto, Principal principal) {
        dto.setUsername(principal.getName());

        productService.create(dto);
        return "redirect:/product/list";
    }




}
