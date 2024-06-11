package com.lec.spring.service;

import com.lec.spring.domain.Attachment;

public interface AttachmentService {

    Attachment findById(Long id);

    // 대부분의 만든 리파지토리는 보드 서비스에서 사용할 예정
}
