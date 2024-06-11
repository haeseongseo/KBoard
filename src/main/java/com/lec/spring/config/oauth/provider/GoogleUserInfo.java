package com.lec.spring.config.oauth.provider;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo{

    // ↓ loadUser() 로 받아온 OAuth2User.getAttributes() 결과를 담을거다
    private Map<String, Object> attributes;

    // 생성자
    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 사이트마다 provider 구조가 다 다르니 이렇게 하나씩 만들어서 상속
    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");      // Object 리턴하기 때문에 String 으로 변환
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");    // Object 리턴하기 때문에 String 으로 변환
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");     // Object 리턴하기 때문에 String 으로 변환
    }
}
