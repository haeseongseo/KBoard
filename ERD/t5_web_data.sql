-- 기존테이블 삭제 (지우는 순서 중요하다 - 자식 먼저 지우고 부모를 지워야한다.)
DELETE FROM t5_attachment;
ALTER TABLE t5_attachment AUTO_INCREMENT = 1;
DELETE FROM t5_comment;
ALTER TABLE t5_comment AUTO_INCREMENT = 1;
DELETE FROM t5_post;
ALTER TABLE t5_post AUTO_INCREMENT = 1;
DELETE FROM t5_user_authorities;
ALTER TABLE t5_user_authorities AUTO_INCREMENT = 1;
DELETE FROM t5_authority;
ALTER TABLE t5_authority AUTO_INCREMENT = 1;
DELETE FROM t5_user ;
ALTER TABLE t5_user AUTO_INCREMENT = 1;

-- 샘플 authority
INSERT INTO t5_authority (name) VALUES
        -- 스프링 security 에서는 role 을 사용해 적도를 세팅한다. (즉, 여기서는 멤버권한과 admin 권한이다)
       ('ROLE_MEMBER'), ('ROLE_ADMIN')
;

-- 샘플 user table
INSERT INTO t5_user (username, password, name, email) VALUES
    ('USER1', '$2a$10$THjKr8GcjhBiAgKt4IEJAuKtpx/yKyK.Vl88O60JshCD8uRE.PObu', '회원1', 'user1@mail.com'),
    ('USER2', '$2a$10$ptvLmLIgmEB9ekv6mZ8Rn.2CS.fVdEOnNuh23a2UsQfX8fLtYDJkO', '회원2', 'user2@mail.com'),
    ('ADMIN1', '$2a$10$t6p9XxSv7PTT5Z4fcbk4vOVS8lrsNb8NMRBLD7U50s2zLCmPDweKm', '관리자1', 'admin1@mail.com')
-- "1234"   -->  암호화 (encrypt) --> "adfasgar$!5fsadg"
--          <--  복호화 (decryption)  <--      : 암호화한 비밀번호 코드를 입력한 숫자로 변환하는 것 (불가능)
;

-- 샘플 사용자-권한
INSERT INTO t5_user_authorities VALUES
    (1, 1),     -- user1 은 member 권한 (1번 유저에겐 1번 권한 즉, member 권한)
    (3, 1),     -- admin1 은 member 와 admin 권한을 줌 (3번 유저에게 1번, 2번 권한을 줌)
    (3, 2)
;

-- 샘플 글
INSERT INTO t5_post (user_id, subject, content) VALUES
    (1, '제목입니다1', '내용입니다1'),        -- 어떤 유저가(user1) 가 어떤 글을 작성했는지 보임 (2번 유저가 글 작성 안 한 것은 아무 권한이 없기 때문)
    (1, '제목입니다2', '내용입니다2'),
    (3, '제목입니다3', '내용입니다3'),
    (3, '제목입니다4', '내용입니다4')
;

-- 샘플 댓글
INSERT INTO t5_comment(user_id, post_id, content) VALUES
          (1, 1, '1. user1이 1번글에 댓글 작성.'),  -- user1 이 1번 글에 1, 2 댓글을 남김
          (1, 1, '2. user1이 1번글에 댓글 작성.'),
          (1, 2, '3. user1이 2번글에 댓글 작성.'),
          (1, 2, '4. user1이 2번글에 댓글 작성.'),
          (1, 3, '5. user1이 3번글에 댓글 작성.'),
          (1, 3, '6. user1이 3번글에 댓글 작성.'),
          (1, 4, '7. user1이 4번글에 댓글 작성.'),
          (1, 4, '8. user1이 4번글에 댓글 작성.'),
          (3, 1, '9. admin1이 1번글에 댓글 작성.'),
          (3, 1, '10. admin1이 1번글에 댓글 작성.'),
          (3, 2, '11. admin1이 2번글에 댓글 작성.'),
          (3, 2, '12. admin1이 2번글에 댓글 작성.'),
          (3, 3, '13. admin1이 3번글에 댓글 작성.'),
          (3, 3, '14. admin1이 3번글에 댓글 작성.'),
          (3, 4, '15. admin1이 4번글에 댓글 작성.'),
          (3, 4, '16. admin1이 4번글에 댓글 작성.')
;

-- 샘플 첨부파일
INSERT INTO t5_attachment(post_id, sourcename, filename) VALUES
         (1, 'face01.png', 'face01.png'),  -- 1번 글부터 4번 글에 각각 2개씩 첨부파일을 넣음
         (1, 'face02.png', 'face02.png'),
         (2, 'face03.png', 'face03.png'),
         (2, 'face04.png', 'face04.png'),
         (3, 'face05.png', 'face05.png'),
         (3, 'face06.png', 'face06.png'),
         (4, 'face07.png', 'face07.png'),
         (4, 'face08.png', 'face08.png')
;



