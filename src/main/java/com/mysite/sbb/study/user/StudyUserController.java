package com.mysite.sbb.study.user;

import com.mysite.sbb.study.StudyDTO;
import com.mysite.sbb.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("user/study")
public class StudyUserController {
    private final StudyService studyService;


    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="1") int page) {
        model.addAttribute("paging", studyService.getList(page - 1));
        return "user/study/list";
    }


    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {
        StudyDTO study = studyService.getStudy(id);
        model.addAttribute("study", study);
        return "user/study/detail";
    }
}
