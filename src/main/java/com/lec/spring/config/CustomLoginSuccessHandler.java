package com.lec.spring.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomLoginSuccessHandler(String defaultTargetUrl){

        // SavedRequestAwareAuthenticationSuccessHandler#setDefaultTargetUrl()
        // 로그인후 특별히 redirect 할 url 이 없는경우 기본적으로 redirect 할 url

        setDefaultTargetUrl(defaultTargetUrl);
    }


    // 로그인 성공 직후 수행할 동작
    @Override
    // Authentication : 로그인 하면 세션에 저장 되어 있는 놈
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        System.out.println("#### 로그인 성공: onAuthenticationSuccess() 호출 ###");

        System.out.println("접속IP: " + getClientIp(request));
        PrincipalDetails userDetails = (PrincipalDetails)authentication.getPrincipal();     // getPrincipal : UserDetails 을 꺼내옴 (Object 를 리턴)
        System.out.println("username: " + userDetails.getUsername());
        System.out.println("password: " + userDetails.getPassword());
        List<String> roleNames = new ArrayList<>();   // user 의 권한이름들
        authentication.getAuthorities().forEach(authority -> {
            roleNames.add(authority.getAuthority());
        });
        System.out.println("authorities: " + roleNames);

        // 로그인하고 로그아웃 할 때까지 접속한 시간 계산
        // 로그인 시간을 세션에 저장하기 (※ logout 예제에서 사용)
        LocalDateTime loginTime = LocalDateTime.now();
        System.out.println("로그인 시간: " + loginTime);
        request.getSession().setAttribute("loginTime", loginTime);

        // 로그인 직전 url 로 redirect 하기
        super.onAuthenticationSuccess(request, response, authentication);       // 부모에서 제공하는 super

    }

    // request 를 한 client ip 가져오기 (request 하면서 헤더의 정보가 담겨오는데 그 헤더의 정보들이 있으면 가져오는 관찰용 코드)
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
