package com.mysite.sbb.support.user;

import com.mysite.sbb.support.SupportDTO;
import com.mysite.sbb.support.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping(
            value = "/create-ajax",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String,Object>> createAjax(
            @ModelAttribute SupportDTO dto,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment
    ) {
        Map<String,Object> body = new HashMap<>();
        try {
            dto.setAttachment(attachment);
            Integer newId = supportService.create(dto); // 생성 후 ID 반환

            body.put("success", true);
            body.put("id", newId);
            body.put("message", "문의가 정상적으로 접수되었습니다.");
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            body.put("success", false);
            body.put("message", "등록 중 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}





