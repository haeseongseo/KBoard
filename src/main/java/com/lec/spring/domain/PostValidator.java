package com.lec.spring.domain;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

// implement method 해야함
public class PostValidator implements Validator {
    // Post 객체를 바인딩 할 때 실행 할 유효성 검사

    @Override
    public boolean supports(Class<?> clazz) {
        boolean result = Post.class.isAssignableFrom(clazz);        // Post 객체인지 확인
        return result;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Post post = (Post) target;         // 검증 코드
        System.out.println("validate() 호출 : " + post);

//        로그인한 사람만 글 쓰기로 되어 있기 때문에 아래 코드는 필요없음.
//        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "user", "작성자는 필수입니다.");
//
//        String subject = post.getSubject(); // 제목 정보 가져오기
//        if (subject == null || subject.trim().isEmpty()){
//            errors.rejectValue("subject", "글 제목은 필수입니다.");
//        }

        // 비어있는 것에 대한 유효성 검사를 validator 에서 지원하는 기능이 있음.(위와 동일함)
        // ValidationUtils 사용
        // 단순히 빈(empty) 폼 데이터를 처리할때는 아래 와 같이 사용 가능
        // 두번째 매개변수 "subject" 은 반드시 target 클래스의 필드명 이어야 함 ⭐️(반드시 필드명이어야 함)
        // 게다가 Errors 에 등록될때도 동일한 field 명으로 등록된다.
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subject", "글 제목은 필수입니다.");       // subject 필드가 비어있으면 뒤의 오류코드를 errors 에 추가
        // 입력 후 BoardController 에 등록해야함
    }
}
