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
        model.addAttribute("topCategories", categoryService.getRootCategories()); // 부모 + children 로딩되게!
        model.addAttribute("producers", producerService.all());
        model.addAttribute("isEdit", false); // ← 폼에서 삼항 분기
        return "admin/product/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         Principal principal) {
        dto.setUsername(principal.getName());
        dto.setImages(images); // 폼에서 올라온 이미지 세팅
        Integer newId = productService.create(dto); // 생성 후 id 반환하게 해두면 좋음
        return "redirect:/admin/product/detail/" + newId;
        // id 반환이 없다면 기존처럼: return "redirect:/admin/product/list";
    }

    /* ---------------- 수정 ---------------- */

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        ProductDTO dto = productService.getProduct(id); // 수정용 DTO (id/producerId/categoryId/텍스트필드 채워진 상태)
        model.addAttribute("productDTO", dto);
        model.addAttribute("topCategories", categoryService.getRootCategories());
        model.addAttribute("producers", producerService.all());
        model.addAttribute("existingImages", productService.findImagesByProductId(id)); // List<ProductImageDTO> 등
        model.addAttribute("isEdit", true);
        return "admin/product/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute ProductDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                         Principal principal) {
        dto.setId(id);
        dto.setUsername(principal.getName());
        dto.setImages(images);
        productService.update(dto, deleteImageIds); // 신규 이미지 추가 + 삭제 반영까지 서비스에서 처리
        return "redirect:/admin/product/detail/" + id;
    }



}
