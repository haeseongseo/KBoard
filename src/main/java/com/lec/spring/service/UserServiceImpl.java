package com.lec.spring.service;

import com.lec.spring.domain.Authority;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AuthorityRepository;
import com.lec.spring.repository.UserRepository;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserRepository userRepository;
    private AuthorityRepository authorityRepository;

    // 기본생성자
    public UserServiceImpl(SqlSession sqlSession){
        userRepository = sqlSession.getMapper(UserRepository.class);
        authorityRepository = sqlSession.getMapper(AuthorityRepository.class);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUsername(userName.toUpperCase());   // id 는 대문자로 저장해서 검색할 때도 이대로 사용(소문자로 입력 받아도 대문자로 저장 및 사용)
    }

    @Override
    public boolean isExist(String username) {
        User user = findByUserName(username.toUpperCase());
        return user != null;        // null 이 아니면 true 리턴
    }

    @Override
    public int register(User user) {
        user.setUsername(user.getUsername().toUpperCase()); // DB 에는 username 을 대문자로 저장한다.
        user.setPassword(passwordEncoder.encode(user.getPassword()));       // PW 는 암호화(encode) 해서 저장
        userRepository.save(user);      // 신규회원 저장. 새로운 id 값 받아옴

        // 신규회원은 ROLE_MEMBER 권한을 기본적으로 부여하기
        Authority auth = authorityRepository.findByName("ROLE_MEMBER");
        authorityRepository.addAuthority(user.getId(), auth.getId());

        return 1;       // 성공하면 1 리턴
    }


    // 특정 id 의 사용자 권한들 가져오기
    @Override
    public List<Authority> selectAuthoritiesById(Long id) {
        User user = userRepository.findById(id);

        return authorityRepository.findByUser(user);
    }
}
