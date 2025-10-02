package com.mysite.sbb.study.admin;

import com.mysite.sbb.study.StudyDTO;
import com.mysite.sbb.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin/study")
public class StudyAdminController {

    private final StudyService studyService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        model.addAttribute("paging", studyService.getList(page - 1));
        return "admin/study/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        StudyDTO study = studyService.getStudy(id);
        model.addAttribute("study", study);
        return "admin/study/detail";
    }


    /* ---------------- 등록 ---------------- */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("studyDTO", new StudyDTO());
        model.addAttribute("isEdit", false);
        return "admin/study/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@ModelAttribute StudyDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         Principal principal) {
        dto.setUsername(principal.getName());
        dto.setImages(images);           // 이미지
        Integer newId = studyService.create(dto); // create가 id 반환하도록 서비스 수정
        return "redirect:/admin/study/detail/" + newId;
    }

    /* ---------------- 수정 ---------------- */

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        StudyDTO dto = studyService.getStudy(id); // dto에 specDocPath/operatingDocPath 포함
        model.addAttribute("studyDTO", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("existingImages", studyService.findImagesByStudyId(id));
        return "admin/study/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute StudyDTO dto,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                         Principal principal) {
        dto.setId(id);
        dto.setUsername(principal.getName());
        dto.setImages(images);
        // 서비스 시그니처: update(dto, deleteImageIds, deleteSpecDoc, deleteOperatingDoc)
        studyService.update(dto, deleteImageIds);
        return "redirect:/admin/study/detail/" + id;
    }


    @PostMapping("/delete")
    public String deleteProducts(@RequestParam("studyIds") List<Integer> studyIds,
                                 RedirectAttributes redirectAttributes) {
        studyService.deleteStudy(studyIds);
        redirectAttributes.addFlashAttribute("msg", studyIds.size() + "건 삭제 완료");
        return "redirect:/admin/study/list";
    }

}
