package com.lec.spring.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // 권한이 없는 유자가 url 접근할 때 호출
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.out.println("### 접근권한 오류 : CustomAccessDeniedHandler : " + request.getRequestURI() + " ###");

        response.sendRedirect("/user/rejectAuth");      // 해당 url 로 redirect 시킴
    }
}
