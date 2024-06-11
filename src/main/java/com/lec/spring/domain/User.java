package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;        // 회원 아이디
    @JsonIgnore             // @JsonIgnore : JSON 으로 response 되면서 화면 출력할 때 정보값 안 보이게
    private String password;        // 회원비밀번호

    @ToString.Exclude       // toString() 에서 제외
    @JsonIgnore
    private String re_password;     // 회원가입시 비밀번호 확인 입력을 위해 (DB 에 저장 안 함)

    private String name;        // 회원 이름
    private String email;       // 이메일

    @JsonIgnore
    private LocalDateTime regDate;

    // OAuth2 Client
    private String provider;
    private String providerId;
}
