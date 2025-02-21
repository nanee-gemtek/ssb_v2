package com.mysite.sbb.answer;

import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller

public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createAnswer(@ModelAttribute AnswerDTO answerDTO, Principal principal){
        //UserDTO siteUser = userService.getUser(principal.getName());
        answerDTO.setUsername(principal.getName());
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
