package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerDTO;
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

        // 빌더 패턴을 사용하여 AnswerDTO 객체 생성
        AnswerDTO answerDTO = AnswerDTO.builder()
                                .questionId(id)
                                .build();


        model.addAttribute("question",question);
        model.addAttribute("answerDTO", answerDTO); //form에 answerDTO를 바인딩 하기 위해서 answerDTO 객체추가
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
