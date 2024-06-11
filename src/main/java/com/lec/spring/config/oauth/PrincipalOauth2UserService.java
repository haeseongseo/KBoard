package com.lec.spring.config.oauth;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.config.oauth.provider.FacebookUserInfo;
import com.lec.spring.config.oauth.provider.GoogleUserInfo;
import com.lec.spring.config.oauth.provider.NaverUserInfo;
import com.lec.spring.config.oauth.provider.OAuth2UserInfo;
import com.lec.spring.domain.User;
import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2UserService<OAuth2UserRequest, OAuth2User>(I)
 * └─ DefaultOAuth2UserService
 */

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    // 인증 후 '후처리' 를 여기에 작성함  (endpoint)

    @Autowired
    private UserService userService;        // 데이터 베이스에서 회원조회, 가입 시키기 위해 주입 받아옴

//    @Autowired                                    // 필요 없을 것 같아서 주석처리
//    private PasswordEncoder passwordEncoder;        // 회원 가입시 PW 인코딩

    @Value("${app.oauth2.password}")        // OAuth2 로 로그인 한 사람들의 pw 를 정의한 것을 가져옴
    private String oauth2Password;          // OAuth2 회원 가입시 기본 PW

    // opt + n 으로 오버라이드
    // loadUser() : OAuth2 인증 직후 호출됨
    //      provider(현재는 구글) 로부터 받은 userRequest 데이터에 대한 후처리 진행 (후처리 - 회원가입, 로그인)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {    // 매개변수인 userRequest 은 구글쪽에서 넘어오는 것을 가져온다.
        System.out.println("OAuth2UserService.loadUser() 호출");      // 확인용

        OAuth2User oAuth2User = super.loadUser(userRequest);     // 사용자 프로필 정보 가져오는 코드
        // 어떠한 정보가 넘어오는지 확인
        System.out.println("""
                  ClientRegistration: %s
                  RegistrationId: %s
                  AccessToken: %s
                  OAuth2User Attributes : %s
                """.formatted(
                userRequest.getClientRegistration()     // ClientRegistration 을 리턴 (registrationId 는 어떤 아이디로 로그인 했는지인데 현재는 구글)
                , userRequest.getClientRegistration().getRegistrationId()   //
                , userRequest.getAccessToken().getTokenValue()              // access token 이 있음
                , oAuth2User.getAttributes()                // Map<String, Object>를 리턴 <- 사용자 프로필 정보가 담겨있음
        ));

        // 후처리 : 회원가입
        // username : google_xxxx
        // provider : google
        // providerId : sub 값 (구글에서 제공하는 PK 값)
        String provider = userRequest.getClientRegistration().getRegistrationId();        // ex) "google", "facebook", "naver"

        OAuth2UserInfo oAuth2UserInfo = switch (provider.toLowerCase()) {
            case "google" -> new GoogleUserInfo(oAuth2User.getAttributes());      // 만약에 유저가 구글로 로그인 한 것이면 구글의 값을 가져옴
            case "facebook" -> new FacebookUserInfo(oAuth2User.getAttributes());
            case "naver" -> new NaverUserInfo(oAuth2User.getAttributes());
            default -> null;
        };

        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerId;
        String password = oauth2Password;
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        // 회원가입 진행하기 전에
        // 이미 가입한 회원인지, 혹은 비가입자인지 체크
        User user = userService.findByUserName(username);
        if (user == null) {      // 미가입자인 경우, 회원 가입을 진행
            User newUser = User.builder()
                    .username(username)
                    .name(name)
                    .email(email)
                    .password(password)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            int cnt = userService.register(newUser);      // 회원가입 수행
            if (cnt > 0) {       // 회원가입 성공
                System.out.println("[OAuth2 인증. 회원 가입 성공]");
                user = userService.findByUserName(username);        // 성공하면 정보를 읽어와야함 (regDate 처럼 자동생성된 정보들을 가져와서 저장 해야하기 때문)
            } else {
                System.out.println("[OAuth2 인증. 회원 가입 실패]");
            }

        } else {
            System.out.println("[OAuth2 인증. 이미 가입된 회원입니다.]");
        }

        PrincipalDetails principalDetails = new PrincipalDetails(user, oAuth2User.getAttributes());
        principalDetails.setUserService(userService);       // 꼭 해줘야 함

        return principalDetails;
    }
}
