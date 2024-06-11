package com.lec.spring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lec.spring.domain.User;
import com.lec.spring.domain.oauth.KakaoOAuthToken;
import com.lec.spring.domain.oauth.KakaoProfile;
import com.lec.spring.service.UserService;
import com.lec.spring.util.U;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/oauth2")              // OAuth2 클라이언트를 사용하지 않고 직접 구현
public class OAuth2Controller {
    // kakao 로그인
    @Value("${app.oauth2.kakao.client-id}")
    private String kakaoClientId;
    @Value("${app.oauth2.kakao.redirect_uri}")
    private String kakaoRedirectUri;
    @Value("${app.oauth2.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${app.oauth2.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${app.oauth2.password}")
    private String oauth2Password;

    @Autowired          // OAuth 로그인 상태 관리
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;        // 회원가입 유저가 필요하기 때문에 자동주입


    @GetMapping("/kakao/callback")
    public String kakaoCallback(String code) {          // request 되면서 kakao 가 보내준 code 값 받아오기
        //------------------------------------------------------------------
        // ■ code 값 확인용
        //   code 값을 받았다는 것은 인증 완료 되었다는 뜻..
        System.out.println("\n<<카카오 인증 완료>>\ncode: " + code);

        //----------------------------------------------------------------------
        // ■ Access token 받아오기 <= code 값 사용
        // 이 Access token 을 사용하여  Kakao resource server 에 있는 사용자 정보를 받아오기 위함.
        KakaoOAuthToken token = kakaoAccessToken(code);     // 밑의 메소드 실행해서 코드값 받아서 콘솔에 띄어주기 용

        //------------------------------------------------------------------
        // ■ 사용자 정보 요청 <= Access Token 사용
        KakaoProfile profile = kakaoUserInfo(token.getAccess_token());

        //---------------------------------------------------
        // ■ 회원가입 시키기  <= KakaoProfile (사용자 정보) 사용
        User kakaoUser = registerKakaoUser(profile);

        //---------------------------------------------------
        // ■ 로그인 처리
        loginKakaoUser(kakaoUser);


        //-------------------------------------------------
        return "redirect:/";        // /로 리다이렉트
    }



    //------------------------------------------------------------------
    // 카카오 Access Token 받아오기 (카카오개발자 - 카카오로그인 - REST API - 토큰받기)
    public KakaoOAuthToken kakaoAccessToken(String code){
        // kakao 에서 받아온 코드를 post 방식으로 카카오 서버쪽으로 보내야함 (data : key-value 쌍)
        RestTemplate rt = new RestTemplate();

        // header 준비
        HttpHeaders headers = new HttpHeaders();
        // (카카오 개발자 카카오 로그인 - REST APi에 있음)
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // body 준비
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);       // 인증 직후 받은 code 값을 사용(kakaoCallback method 에서 사용)

        // header 와 body 담은 HttpEntity 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        // 요청
        ResponseEntity<String> response = rt.exchange(
                kakaoTokenUri,      // Access Token 발급 URI
                HttpMethod.POST,    // 카카오에서 정의한 POST 방식으로 전송
                kakaoTokenRequest,  // HttpEntity (body, header 가 있음)
                String.class        // 응답 받을 타입
        );

        // body + header 의 값 (Json 객체로 옴)
        System.out.println("카카오 AccessToken 요청 응답: " + response);

        // body 만 확인해보면 (Json 객체)
        System.out.println("카카오 AccessToken 요청 body: " + response.getBody());

        // ------------------------------------------------------------------
        // JSON -> Java Object 로 변환
        ObjectMapper mapper = new ObjectMapper();
        KakaoOAuthToken token = null;

        try {
            token = mapper.readValue(response.getBody(), KakaoOAuthToken.class);     // 1 : Json 문자열, 2 : 변활할 클래스 명시
            // AccessToken 확인
            System.out.println("카카오 Access Token: " + token.getAccess_token());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return token;
    }

    // ------------------------------------------------------------------
    // 카카오 사용자 정보 요청  (카카오개발자 - 카카오로그인 - REST API - 사용자정보가져오기)
    public KakaoProfile kakaoUserInfo(String accessToke) {
        RestTemplate rt = new RestTemplate();

        // header 준비
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToke);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // body 는 생략 (필수요소는 아니여서 생략)

        // HttpEntity 생성
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);      // header 만 담아서 HttpEntity 만들기

        // 요청
        ResponseEntity<String> response = rt.exchange(
              kakaoUserInfoUri,
              HttpMethod.POST,
              kakaoProfileRequest,
              String.class
        );
        // 확인용
        System.out.println("카카오 사용자 profile 요청 응답: " + response);
        System.out.println("카카오 사용자 profile 요청 body: " + response.getBody());

        // ------------------------------------------------------------------
        // 사용자 정보 (JSON -> Java 로 받아내기)
        ObjectMapper mapper = new ObjectMapper();
        KakaoProfile profile = null;

        try {
            profile = mapper.readValue(response.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 확인용
        System.out.println("""
                [카카오 회원정보]
                id: %s
                nickname: %s
                """.formatted(profile.getId(), profile.getKakaoAccount().getProfile().getNickname()));

        return profile;
    }

    //-----------------------------------------------------------------------------
    // 회원가입 시키기  (username, password, email, name 필요)
    // Kakao 로그인 한 회원을 User 에 등록하기
    public User registerKakaoUser(KakaoProfile profile){
        // 새로이 가입시킬 username 을 생성 (중복되지 않게 생성 - unique 해야 한다)
        String provider = "KAKAO";
        String providerId = "" + profile.getId();
        String username = provider + "_" + providerId;
        String name = profile.getKakaoAccount().getProfile().getNickname();
        String password = oauth2Password;

        System.out.println("""
               [카카오 인증 회원 정보]
                 username: %s
                 name: %s
                 password: %s  
                 provider: %s
                 providerId: %s            
               """.formatted(username, name, password, provider, providerId));

        // 회원가입 진행하기 전에
        // 이미 가입한 회원인지, 혹은 비가입자인지 체크해야한다.
        User user = userService.findByUserName(username);           // 특정 유저 네임 확인 (있다면 그 유저를 리턴)
        if (user == null){  // 이런 아이디의 회원이 없으니 회원가입 진행
            User newUser = User.builder()
                    .username(username)
                    .name(name)
                    .password(password)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            int cnt = userService.register(newUser);      // 회원 가입! INSERT!!
            if (cnt > 0){       // 정상적으로 INSERT 되면 1을 리턴
                System.out.println("[kakao 인증 회원 가입 성공]");
                user = userService.findByUserName(username);        // 정보 다시 읽어오기 id, regdate(해당 정보를 가져오려면 다시 읽어와야함)
            }else {
                System.out.println("[kakao 인증 회원 가입 실패]");
            }
        }else {         // 이미 해당 username 이 있을 경우
            System.out.println("[kakao 인증, 이미 가입되어 있는 회원입니다.]");
        }

        return user;
    }


    // ---------------------------------------------------------------
    // 로그인 시키기
    public void loginKakaoUser(User kakaoUser){
        // 로그인시 저장되는 authentication 객체를 수동으로 만드는 방법
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                kakaoUser.getUsername(),            // username
                oauth2Password                      // PW 를 만들어서 인증토큰을 만듬
        );            // security 에 인증토큰을 넘겨줘야하는데 이를 아이디와 토큰을 넘겨주는 역할

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authentication);

        U.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
        System.out.println("Kakao 인증 로그인 처리 완료");
    }



}   // end Controller
