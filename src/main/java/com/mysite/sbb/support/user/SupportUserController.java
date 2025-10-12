package com.mysite.sbb.support.user;

import com.mysite.sbb.support.SupportDTO;
import com.mysite.sbb.support.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("user/support")
public class SupportUserController {

    private final SupportService supportService;





    /* ---------------- 등록 ----------------*/
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("supportDTO", new SupportDTO());
        return "user/support/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute SupportDTO dto,
                         @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                         Principal principal) {

        dto.setAttachment(attachment); // 첨부파일
        Integer newId = supportService.create(dto); // create가 id 반환하도록 서비스 수정
        return "redirect:/user/support/form";
    }




}
