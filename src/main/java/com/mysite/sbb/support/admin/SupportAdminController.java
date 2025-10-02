package com.mysite.sbb.support.admin;

import com.mysite.sbb.support.SupportDTO;
import com.mysite.sbb.support.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin/support")
public class SupportAdminController {

    private final SupportService supportService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        model.addAttribute("paging", supportService.getList(page - 1));
        return "admin/support/list";
    }


    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        SupportDTO support = supportService.getSupport(id);
        model.addAttribute("support", support);
        return "admin/support/detail";
    }


    @PostMapping("/delete")
    public String deleteSupport(@RequestParam("supportIds") List<Integer> supportIds,
                                 RedirectAttributes redirectAttributes) {
        supportService.deleteSupports(supportIds);
        redirectAttributes.addFlashAttribute("msg", supportIds.size() + "건 삭제 완료");
        return "redirect:/admin/support/list";
    }


    /* ---------------- 등록 ----------------
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
        Integer newId = supportService.create(dto); // create가 id 반환하도록 서비스 수정
        return "redirect:/admin/product/detail/" + newId;
    }
*/



}
