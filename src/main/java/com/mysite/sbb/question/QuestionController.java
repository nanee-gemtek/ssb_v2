package com.mysite.sbb.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    @GetMapping("/list")
    public String list(Model model){ //매개변수로 Model을 지정하면 객체가 자동으로 생성된다.
        List<QuestionDTO> questionList = questionService.getList();
        model.addAttribute("questionList",questionList);
        return "question_list";
    }

    @GetMapping(value="/detail/{id}")
    //요청한 URL인 http://localhost:8080/question/detail/2의 숫자 2처럼 변하는 id값을 얻을 때에는 @PathVariable 애너테이션을 사용
    public String detail(Model model, @PathVariable("id") Integer id){
        QuestionDTO question = questionService.getQuestion(id);
        model.addAttribute("question",question);
        return "question_detail";
    }
    @GetMapping("/create")
    public String questionCreate(){
        return "question_form";
    }

    @PostMapping("/create")
    public String questionCreate(Model model,@ModelAttribute QuestionDTO questionDTO){
        //TODO 질문을 저장한다
        questionService.create(questionDTO);
        return "redirect:/question/list";
    }
}
