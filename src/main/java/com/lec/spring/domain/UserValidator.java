package com.lec.spring.domain;

import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component          // DB 확인해야하기 때문에 설정(username 중복여부 확인)
public class UserValidator implements Validator {

    @Autowired
    UserService userService;        // username 에 저장되어 있는 정보를 확인하고 가져오기 위해 설정

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);      // User 클래스인 경우에만 validate 실행
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User)target;

        // username 필수, 중복된 username 여부 확인
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            errors.rejectValue("username", "username 은 필수입니다.");
        }else if(userService.isExist(username)){        // 이미 등록되어 있는 ID 일 경우
            errors.rejectValue("username", "이미 존재하는 아이디(username) 입니다.");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name 은 필수입니다");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password 는 필수입니다");

        // email
        // 입력이 되어 있으면 정규표현식 패턴 체크
        // TODO

        // 입력 password, re_password 가 동일한지 비교
        if (!user.getPassword().equals(user.getRe_password())){     // pw 와 re_pw 가 같지 않을 경우 에러문구
            errors.rejectValue("re_password", "비밀번호와 비밀번호 확인 입력값은 같아야 합니다.");
        }

    }
}
