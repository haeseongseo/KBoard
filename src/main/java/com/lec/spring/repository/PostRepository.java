package com.lec.spring.repository;

import com.lec.spring.domain.Post;

import java.util.List;

// Repository layer(aka. Data layer)
// DataSource (DB) 등에 대한 직접적인 접근 (어떤 목적으로 사용할지 설계하는 부분)
public interface PostRepository {

    // 새 글 작성 (INSERT 동작 발생) <- Post (작성자, 제목, 내용을 받아서 INSERT 할 예정)
    int save(Post post);

    // 특정 id 글 내용 읽기 (SELECT)   =>  Post 객체로 리턴
    // 만약 해당 id 에 글이 없으면 null 리턴
    Post findById(Long id);

    // 특정 id 글 조회수 +1 증가 (UPDATE)
    int incViewCnt(Long id);

    // 전체 글 목록을 최신순으로 읽기 (SELECT)   =>  List<>로 받아옴
    List<Post> findAll();

    // 특정 id 글 수정 (제목, 내용만 수정)  -> UPDATE 쿼리 실행
    int update(Post post);      // Post 안에는 id, 내용, 제목 등이 담겨 있음.

    // 특정 id 글 삭제하기 (DELETE)    <=  id 는 Post 에 담아서 전달할 예정
    int delete(Post post);

    // 05.30 수업내용
    // 페이징과 관련된 동작
    // from(몇번째) 부터 rows(몇개)를 select 할 것이닞
    List<Post> selectFromRow(int from, int row);

    // 전체 글의 개수
    int countAll();

}
