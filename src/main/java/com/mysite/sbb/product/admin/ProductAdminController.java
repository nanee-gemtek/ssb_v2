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

    /* ---------------- 등록 ---------------- */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("topCategories", categoryService.getRootCategories());
        model.addAttribute("producers", producerService.all());
        model.addAttribute("isEdit", false);
        return "admin/product/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "specDoc", required = false) MultipartFile specDoc,
                         @RequestParam(value = "operatingDoc", required = false) MultipartFile operatingDoc,
                         Principal principal) {
        dto.setUsername(principal.getName());
        dto.setImages(images);           // 이미지
        dto.setSpecDoc(specDoc);         // 사양서
        dto.setOperatingDoc(operatingDoc); // 사용설명서
        Integer newId = productService.create(dto); // create가 id 반환하도록 서비스 수정
        return "redirect:/admin/product/detail/" + newId;
    }

    /* ---------------- 수정 ---------------- */

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        ProductDTO dto = productService.getProduct(id); // dto에 specDocPath/operatingDocPath 포함
        model.addAttribute("productDTO", dto);
        model.addAttribute("topCategories", categoryService.getRootCategories());
        model.addAttribute("producers", producerService.all());
        model.addAttribute("existingImages", productService.findImagesByProductId(id));
        model.addAttribute("isEdit", true);
        return "admin/product/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute ProductDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                         @RequestParam(value = "specDoc", required = false) MultipartFile specDoc,
                         @RequestParam(value = "operatingDoc", required = false) MultipartFile operatingDoc,
                         @RequestParam(value = "deleteSpecDoc", defaultValue = "false") boolean deleteSpecDoc,
                         @RequestParam(value = "deleteOperatingDoc", defaultValue = "false") boolean deleteOperatingDoc,
                         Principal principal) {
        dto.setId(id);
        dto.setUsername(principal.getName());
        dto.setImages(images);
        dto.setSpecDoc(specDoc);
        dto.setOperatingDoc(operatingDoc);
        // 서비스 시그니처: update(dto, deleteImageIds, deleteSpecDoc, deleteOperatingDoc)
        productService.update(dto, deleteImageIds, deleteSpecDoc, deleteOperatingDoc);
        return "redirect:/admin/product/detail/" + id;
    }




}
