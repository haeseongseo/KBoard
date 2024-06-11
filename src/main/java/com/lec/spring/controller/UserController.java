package com.lec.spring.controller;

import com.lec.spring.domain.User;
import com.lec.spring.domain.UserValidator;
import com.lec.spring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.Binding;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    // cmd + n 으로 세터생성
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public void register(){}

    @PostMapping("/register")
    public String registerOk(@Valid User user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttrs){

        // 검증 에러가 있었다면 redirect 한다
        if(result.hasErrors()){
            redirectAttrs.addFlashAttribute("username", user.getUsername());
            redirectAttrs.addFlashAttribute("name", user.getName());
            redirectAttrs.addFlashAttribute("email", user.getEmail());

            List<FieldError> errList = result.getFieldErrors();
            for(FieldError err : errList) {
                redirectAttrs.addFlashAttribute("error", err.getCode());  // 가장 처음에 발견된 에러를 담아ㅣ 보낸다
                break;
            }

            return "redirect:/user/register";
        }

        // 에러가 없으면 회원 등록 진행
        String page = "user/registerOk";
        int cnt = userService.register(user);    // 매개변수 받은 신규 회원 정보를 갖고 데이터 베이스에 insert 수행
        model.addAttribute("result", cnt);

        return page;

    }

    @Autowired  // bean 되어 있기 때문에 자동주입 받아서 아래 initBinder 에 곧바로 사용
    UserValidator userValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder){
        binder.setValidator(userValidator);
    }

    @GetMapping("/login")
    public void login(){}

    // onAuthenticationFailure 에서 로그인 실패시 forwarding 용
    // request 에 담겨진 attribute 는 Thymeleaf 에서 그대로 표현 가능.
    @PostMapping("/loginError")
    public String loginError(){
        return "user/login";
    }

    // 05.30 수업 내용
    @RequestMapping("/rejectAuth")
    public String rejectAuth(){
        return "common/rejectAuth";
    }
}
