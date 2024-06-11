package com.lec.spring.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    private Long id;        // DB에 있는 PK

    @ToString.Exclude
    private User user;      // 댓글 작성자(FK)

    @JsonIgnore     // JSON 으로 변환 할 때 빠짐
    private Long post_id;   // 어느 글의 댓글인지 (FK)

    private String content; // 댓글 내용

    // java.time.* 객체 변환을 위한 annotation 으로 LocalDateTime 출력할 경우 나오는 T 같은 문자 제거하고 원하는 출력방식 설정
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @JsonProperty("regdate")        // JSON 으로 변환될 때 변환하고자 하는 형태로 지정 가능 (regDate -> regdate 로 변환돼서 나옴)
    private LocalDateTime regDate;  // 댓글 작성 시간
}
