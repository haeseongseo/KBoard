package com.lec.spring.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    // 로그아웃 성공 직후 실행될 메소드
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("### 로그아웃 성공 : CustomLogoutSuccessHandler 동작 ###");

        // 로그아웃 시간 남기기
        LocalDateTime logoutTime = LocalDateTime.now();
        System.out.println("로그아웃시간: " + logoutTime);

        // 로그인 ~ 로그아웃 하는 사용시간 계산
        LocalDateTime loginTime = (LocalDateTime) request.getSession().getAttribute("loginTime");     // Attribute 를 뽑아오면 대부분 object 타입이기 때문에 LocalDateTime으로 변환 필요
        if (loginTime != null){
            long seconds = loginTime.until(logoutTime, ChronoUnit.SECONDS);    // 로그인 ~ 로그아웃까지의 경과 시간을 초단위로 출력
            System.out.println("사용시간: " + seconds + "초");
        }       // 이렇게 한 후 Security Config에 등록
        request.getSession().invalidate();      // session invalidate (수동으로 세션을 없애줌)

        String redirectUrl = "/user/login?logoutHandler";       // 로그아웃 성공하면 이동할 url 수동설정

        // 만약 return url 이 있는 경우 logout 하고 해당 url 로 redirect 하기 (ret_url 이 명시 안 되어 있으면 33번째 줄로 동작함)
        if(request.getParameter("ret_url") != null){
            redirectUrl = request.getParameter("ret_url");
        }

        // redirect 를 수동으로 하는 코드
        response.sendRedirect(redirectUrl);

    }
}
