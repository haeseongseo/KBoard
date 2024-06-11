package com.lec.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QryResult {
    Integer count;     // 결과 값
    String status;  // 처리 결과 메시지 (오류가 생길 경우 적절한 메시지 담음)
}
