package com.lec.spring.controller;

import com.lec.spring.domain.Attachment;
import com.lec.spring.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController()       // 데이터 response 하는 컨트롤러
                          //  @Controller + @Responsebody
public class AttachmentController {

    @Value("${app.upload.path}")
    private String uploadDir;

    private AttachmentService attachmentService;

    @Autowired
    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    // 파일 다운로드
    // id: 첨부파일의 id
    // ResponseEntity<T> 를 사용하여         // ResponseEntity<T> : 직접 response 데이터들을 구성해줌
    // '직접' Response data 를 구성
    @RequestMapping("/board/download")
    public ResponseEntity<?> download(Long id){     // <?> 와일드 카드 : 어떠한 타입이든 가능
        if (id == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);      // null 이면 400(BAD_REQUEST) 번 에러 나오게 함

        Attachment file = attachmentService.findById(id);
        if (file == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);       // 파일이 없을 경우 404에러

        String sourceName = file.getSourcename();       //원본이름
        String fileName = file.getFilename();       // 저장된 파일명

        String path = new File(uploadDir, fileName).getAbsolutePath();      // 저장된 파일의 절대경로

        // 파일 유형(MIME type) 추출
        try {
            String mimeType = Files.probeContentType(Paths.get(path));      // ex) "image/png" 처럼 리턴해준다.    (response 할 때 어떤 파일인지 무조건 전송해줘야한다.)

            // 파일 유형이 지정되어 있지 않은 경우 (모든 파일이 지정되어 있지는 않음)
            if (mimeType == null){
                mimeType = "application/octet-stream";  // 일련의 byte 스트림 타입에 대한 기본 타입 (유형이 알려지지 않은 경우 지정해야함)
            }

            // response 할 body 준비
            Path filePath = Paths.get(path);
            // 저장된 파일로부터 인풋스트림을 뽑아내고 리소스 객체로 변환
            Resource resource = new InputStreamResource(Files.newInputStream(filePath));     // newInputStream : inputStream 리턴

            // response 의 header 세팅
            HttpHeaders headers = new HttpHeaders();        // 웹 통신에서 쓰는 헤더 객체
            // 원본파일 이름으로 다운 받기를 지정해줘야함 (⭐️중요) (원본 파일 이름 - sourceName 으로 다운로드 하게 하기 위한 세팅으로 헤더에 저장되어 있음)
                                                                                                // ⭐️ 반드시 URL 인코딩 해야함 (한글파일로 올렸을경우 깨져서 다운 됨)
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(URLEncoder.encode(sourceName, "utf-8")).build());
            headers.setCacheControl("no-cache");
            headers.setContentType(MediaType.parseMediaType(mimeType));     // 파일 유형 지정

            // responseEntity<> 리턴 (매개변수1 body, 매개변수2 header, 매개변수3 status)
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);      // 200 출력 (정상 출력)

        } catch (IOException e) {
            return new ResponseEntity<>(null, null, HttpStatus.CONFLICT);       // 409 파일이 없을 경우
        }
    }
}
