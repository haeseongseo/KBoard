package com.lec.spring.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder        // builder pattern 사용 가능 (추후 설명 예정)
public class Post {
    private Long id;
    private String subject; // 글제목
    private String content; // 내용
    private LocalDateTime regDate;  // 작성일자
    private Long viewCnt;   // 조회수

    private User user;      // 글 작성자 (FK, 전에 했던 문자열 아님)

    // 첨부파일
    @ToString.Exclude       // toString 값 없애기
    @Builder.Default        // builder 제공 안 함
    private List<Attachment> fileList = new ArrayList<>();
}
