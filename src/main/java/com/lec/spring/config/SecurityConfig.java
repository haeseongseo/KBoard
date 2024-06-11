package com.lec.spring.config;

import com.lec.spring.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration          // configuration : 빈으로 생성되고 빈으로 정의도 가능
@EnableWebSecurity      // EnableWebSecurity : Spring Security 기능을 활성화시켜주는 어노테이션
public class SecurityConfig {       // 이게 동작을 할 경우 수동으로 동작시켜줘야 함.

//    @Bean
//    public PasswordEncoder encoder(){       // PasswordEncoder : security 에 있는 객체
//        return new BCryptPasswordEncoder();     // password 객체 중 하나로 가장 많이 쓰임
//    }

    // ⬇️ Security 시키지 않기 위해서 bean 으로 설정 (Security 설정을 하면 어떠한 request 가 오든 로그인 하도록 하기 때문에 잠시 기능을 멈추게 하는 설정)
//    @Bean       // -> 어떠한 request 가 와도 패스해줌
//    public WebSecurityCustomizer webSecurityCustomizer(){
//        // 어떠한 request 도 무시한다 => 검문하지 않는다
//        return web -> web.ignoring().anyRequest();      // WebSecurityCustomizer 을 리턴 (추상메소드가 하나 있는 어떠한 존재)
//    }

    // OAuth2 Client (코드 밑에 있는 후처리를 위해 등록)
    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;


    // ⬇️ SecurityFilterChain 을 Bean 으로 등록해서 사용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())   // CSRF : 활성화 하게 되면 security 에서 post 방식으로 request 할 경우 랜덤으로 어떤 값을 서버쪽에서 클라이언트에 보냄. 해당 값이 response 에 담겨 서버에 와야 정상적인 접속으로 인정하는 것

                 /**********************************************
                 * ① request URL 에 대한 접근 권한 세팅  : authorizeHttpRequests()
                 * .authorizeHttpRequests( AuthorizationManagerRequestMatcherRegistry)
                 **********************************************/
                .authorizeHttpRequests(auth -> auth     // AuthorizationManagerRequestMatcherRegistry 을 람다식으로 짧게 표현
                        // URL 과 접근권한 세팅(들)
                        // ↓ /board/detail/** URL 로 들어오는 요청은 '인증'만 필요. (board/detail 및 그 이하로 들어오는 경로에 대한 설정)
                        .requestMatchers("/board/detail/**").authenticated()
                        // ↓ "/board/write/**", "/board/update/**", "/board/delete/**" URL 로 들어오는 요청은 '인증' 뿐 아니라 ROLE_MEMBER 나 ROLE_ADMIN 권한을 갖고 있어야 한다. ('인가')
                        .requestMatchers("/board/write/**", "/board/update/**", "/board/delete/**").hasAnyRole("MEMBER", "ADMIN")       // hasAnyRole("MEMBER", "ADMIN") : 해당 경로는 member or admin 권한이 있어야 URL 의 권한을 허락 (인가에 대한 값)
                        // ↓ 그 밖의 다른 요청은 모두 permit!
                        .anyRequest().permitAll()       // 그 외에는 굳이 인증인가 따지지 않겠다는 의미. ("/board/list/** 가 해당)
                )
                /********************************************
                 * ② 폼 로그인 설정
                 * .formLogin(HttpSecurityFormLoginConfigurer)
                 *  form 기반 인증 페이지 활성화.
                 *  만약 .loginPage(url) 가 세팅되어 있지 않으면 '디폴트 로그인' form 페이지가 활성화 된다
                 ********************************************/
                .formLogin(form -> form
                        // 로그인 필요한 상황 발생시 매개변수의 url (로그인 폼) 으로 request 발생
                        .loginPage("/user/login")     // 로그인이 필요한 상황이라면 해당 주소로 가게 설정
                        .loginProcessingUrl("/user/login")      // POST 로 리퀘스트하는 url 적음 (컨트롤러에 안 만들었음 - 시큐리티가 자동적으로 해줌)
                                                                // "/user/login" url 로 POST request 가 들어오면 시큐리티가 낚아채서 처리, 대신 로그인을 진행해준다(인증).
                                                                // 이와 같이 하면 Controller 에서 /user/login (POST) 를 굳이 만들지 않아도 된다!
                                                                // 위 요청이 오면 자동으로 UserDetailsService 타입 빈객체의 loadUserByUsername() 가 실행되어 인증여부 확인진행 <- 이를(UserDetailsService) 제공해주어야 한다.
                        .defaultSuccessUrl("/")     // 인증이 성공하면 넘어갈 페이지
                                                    // '직접 /login' → /login(post) 에서 성공하면 "/" 로 이동시키기
                                                    // 만약 다른 특정페이지에 진입하려다 로그인 하여 성공하면 해당 페이지로 이동 (너무 편리!)

//                        .usernameParameter("aaa")           // 기본 name = "username" -> 해당 html 의 input name
//                        .passwordParameter("bbb")           // 기본 name = "password"

                        // 로그인 성공 직후 수행할 코드 (수동으로 설정해줘야함 - CustomLoginSuccessHandler.java 에 명시)
                        //.successHandler(AuthenticationSuccessHandler)  // 로그인 성공후 수행할 코드.
                        .successHandler(new CustomLoginSuccessHandler("/home"))

                        // 로그인 실패하면 수행할 코드
                        // .failureHandler(AuthenticationFailureHandler)
                        .failureHandler(new CustomLoginFailureHandler())

                )
                // 05.30 수업 내용
                /********************************************
                 * ③ 로그아웃 설정
                 * .logout(LogoutConfigurer)
                 ********************************************/
                // ※ 아래 설정 없이도 기본적올 /logout 으로 로그아웃 된다
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .logoutUrl("/user/logout")      // 로그아웃을 수행하는 url 을 설정
                        // .logoutSuccessUrl("/home")       // 로그아웃 성공하면 redirect 하는 URL (로그아웃 하고 특별히 할 것이 없을 경우 이렇게 설정해도 됨)

                        .invalidateHttpSession(false)       // 로그아웃 하면 session 이 없어져 로그인 했던 정보가 사라지는데 그 과정을 보류하는 것 (session invalidate 수행 안 함)
                        // 이따가 CustomLogoutSuccessHandler 에서 꺼낼 정보가 있기 때문에
                        // false 로 세팅한다

                        // .deleteCookies("JSESSIONID")        // 로그아웃하면 JSESSIONID (특정 쿠키) 쿠키 제거

                        // 로그아웃 성공후 수행할 코드
                        // ⬇️.logoutSuccessHandler(LogoutSuccessHandler)
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler())     // 핸들러가 있기 때문에 logoutSuccessUrl 이 실행되지 않음 (핸들러가 있다는 것은 수동으로 만들어 주겠다는 의미)
                )
                /********************************************
                 * ④ 예외처리 설정
                 * .exceptionHandling(ExceptionHandlingConfigure)       -> 권한 없는 사용자가 해당 페이지지로 이동할 경우 처리할 핸들러
                 ********************************************/
                // ※ 아래 설정이 없이 user2 로 /board/write 접근하면 403 에러 발생
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        // 권한(Authorization) 오류 발생시 수행할 코드
                        // .accessDeniedHandler(AccessDeniedHandler)
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )

                /********************************************
                 * OAuth2 로그인
                 * .oauth2Login(OAuth2LoginConfigurer)
                 ********************************************/
                .oauth2Login(httpSecurityOAuth2LoginConfigurer -> httpSecurityOAuth2LoginConfigurer
                        .loginPage("/user/login")           // 로그인 페이지를 기존과 동일한 url 로 지정 (구글 로그인 누를 경우 구글의 인증 화면으로 리다이렉트 해줌 -> 회원가입과 로그인 후 세션 처리 해주는 코드를 해야함)
                        // ⬆️ 구글 로그인 완료된 뒤에 후처리가 필요하다.

                        // code 를 받아오는 것이 아니라, 'AccessToken' 과 사용자 '프로필정보'를 한번에 받아온다
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                // 인증 서버에 userinfo endpoint 설정을 해줘야한다.(매개변수에)
                                .userService(principalOauth2UserService)          // 처리에 필요한 서비스를 여기에 등록 (userService(OAuth2UserService)를 등록해야함)
                        )
                )

                .build();
    }   // end filterChain()

    // --------------------------------------------------------ㅍ
    // OAuth 로그인
    // AuthenticationManager Bean 생성
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
