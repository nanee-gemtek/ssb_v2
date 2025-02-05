package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
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

    @PostMapping("/create")
    public String createAnswer(@ModelAttribute AnswerDTO answerDTO){
        answerService.create(answerDTO);
        return String.format("redirect:/question/detail/%s",answerDTO.getQuestionId());
    }

    @GetMapping("/delete/{id}")
    public String answerDelete( @PathVariable("id") Integer id) {
        AnswerDTO answerDto = answerService.getAnswer(id);
        answerService.delete(answerDto);
        return String.format("redirect:/question/detail/%s", answerDto.getQuestionId());
    }
}
