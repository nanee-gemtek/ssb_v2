package com.mysite.sbb;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.category.SidebarData;
import com.mysite.sbb.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // ★ 필수: 생성자 주입 자동 생성
public class MainController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/sbb")
    @ResponseBody
    public String index(){
        return "안녕하세요. sbb에 오신걸 환영";
    }

//    @GetMapping("/")
//    public String root(){
//        return "redirect:/question/list";
//    }
//    @GetMapping("/")
//    public String userIndx(Model model){ //매개변수로 Model을 지정하면 객체가 자동으로 생성된다.
//        return "user/index";
//    }


    @GetMapping("/")
    public String userIndex(Model model,
                            @RequestParam(value = "parent", required = false) Long parentId,
                            @RequestParam(value = "child",  required = false) Long childId) {

        SidebarData side = categoryService.getSidebarData();

        // 부모 옵션: [{id, name}]
        List<Map<String,Object>> parentOptions = side.getRoots().stream()
                .map(p -> Map.<String,Object>of("id", p.getId(), "name", p.getName()))
                .collect(Collectors.toList());

        // 자식 옵션: { rootId: [{id, name}, ...], ... }
        Map<Long, List<Map<String,Object>>> childrenOptions = new LinkedHashMap<>();
        for (Category root : side.getRoots()) {
            List<Category> kids = side.getChildrenMap().getOrDefault(root.getId(), List.of());
            List<Map<String,Object>> simpleKids = kids.stream()
                    .map(c -> Map.<String,Object>of("id", c.getId(), "name", c.getName()))
                    .collect(Collectors.toList());
            childrenOptions.put(root.getId(), simpleKids);
        }

        model.addAttribute("parentOptions", parentOptions);
        model.addAttribute("childrenOptions", childrenOptions);
        model.addAttribute("selectedParentId", parentId);
        model.addAttribute("selectedChildId",  childId);
        model.addAttribute("featured", productService.getFeatured());
        return "user/index";

}



    @GetMapping("/admin/login")
    public String adminLogin(){ //매개변수로 Model을 지정하면 객체가 자동으로 생성된다.
        return "admin/siteUser/login_form";
    }
    @GetMapping("/product")
    public String product(Model model){ //매개변수로 Model을 지정하면 객체가 자동으로 생성된다.
        return "product";
    }
}
