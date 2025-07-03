package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/sbb")
    @ResponseBody
    public String index(){
        return "안녕하세요. sbb에 오신걸 환영";
    }

//    @GetMapping("/")
//    public String root(){
//        return "redirect:/question/list";
//    }
    @GetMapping("/")
    public String list(Model model){ //매개변수로 Model을 지정하면 객체가 자동으로 생성된다.
        return "index";
    }

}
