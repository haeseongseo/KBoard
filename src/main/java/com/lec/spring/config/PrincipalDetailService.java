package com.lec.spring.config;

import com.lec.spring.domain.User;
import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// UserDetailsService
// 컨테이너에 등록한다.
// 시큐리티 설정에서 loginProcessingUrl(url) 을 설정해 놓았기에
// 로그인시 위 url 로 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어 있는
// loadUserByUsername() 가 실행되고
// 인증성공하면 결과를 UserDetails 로 리턴
@Service        // Service 도 bean 이기 때문에 UserService 주입 가능
public class PrincipalDetailService implements UserDetailsService {

    @Autowired
    private UserService userService;

    // 사용자 인증을 수행할 때 실행
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("loadUserByUsername(" + username + ") 호출");

        // DB 에 해당 username 이 있는지 조회
        User user = userService.findByUserName(username);       // 정보가 있으면 user 객체를 읽어옴

        // 해당 username 의 user 가 DB 에 있다면
        // UserDetails 생성해서 리턴
        if (user != null){
            PrincipalDetails userDetails = new PrincipalDetails(user);      // 28번째 줄의 user 를 가져와 매개변수에 담음
            userDetails.setUserService(userService);
            return userDetails;     // userDetails 가 만들어져 SpringSecurity 세션 공간에 저장이 된다.
        }
        // 해당 username 의 user 가 없다면?

        // UsernameNotFoundException 을 throw 해주어야 한다. (return 대신 throw 로 호출)
        throw new UsernameNotFoundException(username);

        // 주의!  여기에서 null 리턴하면 예외가 발생!

    }
}
