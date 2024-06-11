package com.lec.spring.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)      //부모 클래스의 equals와 hashCode 메서드를 호출하지 않음
@NoArgsConstructor
public class QryCommentList extends QryResult{      // 상속

    @ToString.Exclude
    @JsonProperty("data")       // list 로 정의했지만 JSON 으로 데이터화 할 때는 data 로 나옴
    List<Comment> list;
}
