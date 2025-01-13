package com.mysite.sbb.answer;

import com.mysite.sbb.question.QuestionDTO;
import com.mysite.sbb.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller

public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;

    @PostMapping("/create/{id}")
    public String createAnswer(Model model, @PathVariable("id") Integer id, @ModelAttribute AnswerDTO answerDTO){
        QuestionDTO question = this.questionService.getQuestion(id);
        answerService.create(answerDTO,id);
        return String.format("redirect:/question/detail/%s",id);
    }
}
