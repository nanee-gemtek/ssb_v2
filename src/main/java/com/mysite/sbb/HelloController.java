package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @GetMapping("/hello")
    //@ResponseBody 애너테이션은 hello 메서드의 출력결과가 문자열 그 자체임
    @ResponseBody
    public String hello(){
        return "Hello world";
    }
}
