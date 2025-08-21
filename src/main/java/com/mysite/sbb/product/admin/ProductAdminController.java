package com.mysite.sbb.product.admin;

import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.producer.ProducerService;
import com.mysite.sbb.product.ProductDTO;
import com.mysite.sbb.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin/product")
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProducerService producerService;

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
        model.addAttribute("topCategories", categoryService.getRootCategories());
        model.addAttribute("producers", producerService.all());
        return "admin/product/form";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         Principal principal) {
        dto.setUsername(principal.getName());
        dto.setImages(images); // ⇒ 여기서 DTO에 확실히 주입

        productService.create(dto);
        return "redirect:list";
    }




}
