package com.lec.spring.repository;

import com.lec.spring.domain.Attachment;

import java.util.List;
import java.util.Map;

public interface AttachmentRepository {

    // 첨부파일 관련 동작들
    /**
     * 특정 글(postId) 에 첨부파일(들) 추가 INSERT
     * 글 insert, update 시 사용됨.
     * @param list :  Map<String, Object> 들의 List
     *                      ↓        ↓
     *                   <"sourcename",원본파일명>
     *                   <"filename", 저장된파일명>
     * @param postId : 첨부될 글
     * @return : DML 수행 결과값
     */
    // 특정 글의 파일들을 insert
    int insert(List<Map<String, Object>> list, Long postId);

    // 첨부파일 저장
    int save(Attachment file);

    // 특정 글(postId) 의 첨부파일들을 List 로 리턴  (SELECT)
    List<Attachment> findByPost(Long postId);

    // 특정 첨부파일(id) 한개 select
    Attachment findById(Long id);

    // 선택된 첨부파일들 SELECT (어떤 특정글의 첨부파일을 글 수정 단계에서 수정할 때)
    // 글 수정 에서 사용
    List<Attachment> findByIds(Long [] ids);        // 배열을 매개변수로 받음

    // 선택된 첨부파일들 DELETE
    // 글 수정 에서 사용
    int deleteByIds(Long [] ids);

    // 특정 첨부 파일(file)을 DB에서 삭제
    int delete(Attachment file);
}
