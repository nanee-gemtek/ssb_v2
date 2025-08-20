package com.mysite.sbb.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/producer")
public class ProducerController {

    private final ProducerService producerService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("producers", producerService.all());
        return "producer_list";
    }

//    @PostMapping("/create")
//    public String create(@RequestParam String name, @RequestParam(required = false) Long parentId) {
//        producerService.saveCategory(name, parentId);
//        return "redirect:/admin/category/list";
//    }
}
