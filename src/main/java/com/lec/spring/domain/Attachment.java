package com.lec.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    // 첨부파일 관련
    private Long id;
    private Long post_id;       // 어느글의 첨부파일? (FK)

    private String sourcename;      // 원본 파일명
    private String filename;        // 저장된 파일명 (rename 된 파일명)

    private boolean isImage;        // image 인지 여부 파악을 위해 (DB 저장 용도는 아님)

}
