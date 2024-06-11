SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'db326'
  AND TABLE_NAME LIKE 't5_%'
;

SELECT *
FROM t5_authority;
SELECT *
FROM t5_user
ORDER BY id DESC;
SELECT *
FROM t5_user_authorities; -- user 의 권한 확인
SELECT *
FROM t5_post
ORDER BY id DESC;
SELECT *
FROM t5_comment
ORDER BY id DESC;
SELECT *
FROM t5_attachment
ORDER BY id DESC;

-- 특정 id 의 사용자 조회
SELECT id       "id"
     , username "username"
     , password "password"
     , email    "email"
     , name     "name"
     , regdate  "regdate"
FROM t5_user
WHERE 1 = 1
  AND id = 1
;

-- 특정 사용자의 authority 조회
select a.id "id", a.name "name"
from t5_authority a, -- 중간 테이블 user_authorities 과 조인
     t5_user_authorities u
where a.id = u.authority_id
  AND u.user_id = 3 -- user.id 가 3번이면서 a.id 와 u.authority_id 가 같으면
;

-- 글(Post) 조회 (PostRepository.xml)
SELECT p.id       "p_id",
       p.subject  "p_subject",
       p.content  "p_content",
       p.viewcnt  "p_viewCnt",
       p.regdate  "p_regDate",
       u.id       "u_id",
       u.username "u_username",
       u.name     "u_name",
       u.email    "u_email",
       u.regdate  "u_regdate"
FROM t5_post p,
     t5_user u
WHERE p.user_id = u.id;

-- 페이징 실습용 다량의 데이터
INSERT INTO t5_post(user_id, subject, content)
SELECT user_id, subject, content
FROM t5_post;

SELECT count(*)
from t5_post;

-- id 역순 상위 5개만 출력
select *
from t5_post
order by id desc
limit 5;

-- 5번째부터 5개
select *
from t5_post
order by id desc
limit 5, 5;

-- ------------------------
-- 특정 글의 댓글 및 사용자 정보 (Comment 관련 테스트 - 1번 글에 대한 댓글)
select
    c.id "c_id",
    c.content "c_content",
    c.redgate "c_regdate",
    c.post_id "c_post_id",
    u.id "u_id",
    u.username "u_username",
    u.password "u_password",
    u.name "u_name",
    u.email "u_email",
    u.regdate "u_regdate"
from t5_comment c,
     t5_user u
where c.user_id = u.id AND c.post_id = 1
ORDER BY c.id DESC;