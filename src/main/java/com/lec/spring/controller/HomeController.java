package com.lec.spring.controller;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/")
public class HomeController {
    // test 용 핸들러

    @RequestMapping("/")        // / 로 접속을 하면 곧바로 redirect
    public String home(Model model){
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public void home(){}

    //-------------------------------------------------------------------------
    // 현재 Authentication 보기 (디버깅 등 용도로 활용, 컨트롤러에서 활용 가능한 3가지 학습용)
    @RequestMapping("/auth")        // userDetail 이 있고 그 안에 user 객체가 있다.
    @ResponseBody       // JSON 으로 response
    public Authentication auth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // 매개변수에 Authentication 을 명시해도 주입된다.  (인증후에만 가능 - 지금은 500 에러 뜸, authentication 가 null 이기 때문)
    @RequestMapping("/userDetails")
    @ResponseBody
    public PrincipalDetails userDetails(Authentication authentication){
        return (PrincipalDetails) authentication.getPrincipal();        // object 이기 때문에 형변환
    }

    // @AuthenticationPrincipal 을 사용하여 로그인한 사용자 정보 주입받을수 있다.
    // org.springframework.security.core.annotation.AuthenticationPrincipal
    @RequestMapping("/user")
    @ResponseBody
    public User username(@AuthenticationPrincipal PrincipalDetails userDetails){        // 핸들러 호출될 때 로그인 한 객체의 userDetails 가져옴
        return (userDetails != null) ? userDetails.getUser() : null;
    }

    // OAuth2 Client 를 사용하여 로그인 경우.
    // Principal 객체는 OAuth2User 타입으로 받아올수도 있다.
    // AuthenticatedPrincipal(I)
    //  └─ OAuth2AuthenticatedPrincipal(I)
    //       └─ OAuth2User (I)
    @RequestMapping("/oauth2")
    @ResponseBody
    public OAuth2User oauth2(Authentication authentication){
        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        return oAuth2User;
    }

    @RequestMapping("/oauth2user")
    @ResponseBody
    public Map<String, Object> oauth2user(@AuthenticationPrincipal OAuth2User oAuth2User){        // attr 뽑아내기 (attr 은 Map<>으로 되어 있음)
        return (oAuth2User != null) ? oAuth2User.getAttributes() : null;
    }



}
