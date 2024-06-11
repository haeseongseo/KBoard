package com.lec.spring.util;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class U {

    // 현재 로그인 한 사용자 user
    public static User getLoggedUser(){
        // 로그인 하지 않은 상태면 해당 코드는 에러가 나올 수 있음
        PrincipalDetails userDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();      // userDetail 객체 가져오기(getPrincipal)
        User user = userDetails.getUser();
        return user;
    }

    // 현재 request 구하기
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }


    // 현재 session 구하기
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    // 첨부파일 정보 (MultipartFile) 출력하기
    public static void prinfFileInfo(MultipartFile file) {
        String originalFileName = file.getOriginalFilename(); // 최초의 업로드했을 당시의 원본 파일 명

        if (originalFileName == null || originalFileName.isEmpty()){
            System.out.println("\t파일이 없습니다.");
            return;
        }
        System.out.println("""
                Original File Name : %s
                CleanPath : %s
                File Size : %s
                MIME : %s
                """.formatted(
                        originalFileName,
                        StringUtils.cleanPath(originalFileName),
                        file.getSize() + " bytes",  // 용량(byte)
                        file.getContentType()       // content type (MIME type)
                ));       // originalFileName 출력

        // 이미지 파일인지 여부를 판정
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());       // 업로드된 파일에 대해서 stream 뽑아서 저장할 용도(InputStream 에서 이미지를 읽어내고 이미지일 경우 bufferImage 리턴)
                                                                        // 이미지가 아닐 경우 null return
            if (bufferedImage != null){     // 이미지인경우
                System.out.println("""
                            이미지 파일입니다: %d x %d
                        """.formatted(bufferedImage.getWidth(), bufferedImage.getHeight()));
            }else { // 이미지가 아닌 경우
                System.out.println("\t이미지 파일이 아닙니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
