package com.lec.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration {

    @Bean
    public PasswordEncoder encoder(){       // PasswordEncoder : security 에 있는 객체
        return new BCryptPasswordEncoder();     // password 객체 중 하나로 가장 많이 쓰임
    }

    // 첨부파일 관련 클래스
    @Configuration
    public static class LocalMvcConfiguration implements WebMvcConfigurer {

        // 파일 업로드 관련으로 리소스(resource) 경로를 추가
        @Value("${app.upload.path}")
        private String uploadDir;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            System.out.println("LocalMvcConfiguration.addResourceHandlers() 호출");
            //  /upload/** URL 로 request 가 들어오면
            // upload/ 경로의 resource 가 동작케 함.
            // IntelliJ 의 경우 이 경로를 module 이 아닌 project 이하에 생성해야 한다.
            registry        // static 경로를 하나 더 추가 하는 역할
                    .addResourceHandler("/upload/**")           // url 연결
                    .addResourceLocations("file:" + uploadDir + "/");        // 해당 경로 이하에서 resource 를 찾는다 (디렉토리 연결)
        }
    }

}
