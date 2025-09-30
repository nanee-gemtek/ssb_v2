package com.mysite.sbb.study.admin;

import com.mysite.sbb.study.StudyDTO;
import com.mysite.sbb.study.StudyService;
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

}
