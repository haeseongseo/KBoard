package com.lec.spring.service;

import com.lec.spring.domain.Attachment;
import com.lec.spring.domain.Post;
import com.lec.spring.domain.User;
import com.lec.spring.repository.AttachmentRepository;
import com.lec.spring.repository.PostRepository;
import com.lec.spring.repository.UserRepository;
import com.lec.spring.util.U;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Service        // bean 으로 등록해야하기 때문에 @Service
public class BoardServiceImpl implements BoardService {

    // 첨부파일 업로드
    @Value("${app.upload.path}")
    private String uploadDir;

    // 페이징의 개수
    @Value("${app.pagination.write_pages}")     // application.yml 에 선언한 app.pagination 정보 가져오기
    private int WRITE_PAGES;

    // 페이징에 몇개의 페이지를 담을 것인지
    @Value("${app.pagination.page_rows}")
    private int PAGE_ROWS;

    // 글 작성과 관련된 repository 가져오기
    private PostRepository postRepository;

    // 게시글 생성할 때는 현재 로그인한 정보를 가져와야함
    private UserRepository userRepository;

    // 권한 설정에 대한 정보를 가져와야함
    private AttachmentRepository attachmentRepository;

    // 기본생성자에 sqlSession DI
    @Autowired  // 생성자가 1개인 경우 없어도 되지만 명시 (매개변수를 주입)
    public BoardServiceImpl(SqlSession sqlSession) {     // MyBatis 가 생성한 SqlSession bean 객체 주입
        postRepository = sqlSession.getMapper(PostRepository.class);
        userRepository = sqlSession.getMapper(UserRepository.class);
        attachmentRepository = sqlSession.getMapper(AttachmentRepository.class);

        System.out.println("BoardService() 생성");
    }

    @Override
    public int write(Post post, Map<String, MultipartFile> files) {
        // 현재 로그인 한 작성자의 정보를 가져와야함 (post 에는 제목, 글만 있음)
        User user = U.getLoggedUser();      // session 에서 가져온 정보

        // 위 정보는 session 의 정보이고, 일단 DB 에서 다시 읽어 와야한다.
        user = userRepository.findById(user.getId());
        post.setUser(user);     // 글 작성자 세팅

        int cnt = postRepository.save(post);        // 글 먼저 저장 (그래야 AI 된 PK 값(id)이 post에 담겨서 받아온다)

        // 첨부파일 추가
        addFiles(files, post.getId());      // 매개변수1 : 파일들 , 매개변수2: 어느 글의 첨부파일

        return cnt;
    }

    // 특정 글(id) 에 첨부파일(들) 추가
    private void addFiles(Map<String, MultipartFile> files, Long id) {
        if (files == null) return;      // 파일이 없으면 그냥 리턴

        for (Map.Entry<String, MultipartFile> e : files.entrySet()) {
            // name="upfile##" 인 경우만 첨부파일 등록. (이유, 다른 웹에디터와 섞이지 않도록..ex: summernote)
            if (!e.getKey().startsWith("upfile"))
                continue;        // 키 값의 파일 명이 upfile 로 되어 있지 않은 것만 무시하고 넘긴다. (write.js에 명시한 네임값)

            // 첨부파일 정보 출력 (학습 목적으로 확인용)
            System.out.println("\n첨부파일 정보: " + e.getKey());     // 첨부파일 name 값
            U.prinfFileInfo(e.getValue());      // MultipartFile 정보를 출력하는 메소드
            System.out.println();

            // 물리적인 파일 저장
            Attachment file = upload(e.getValue());

            // 성공하면 DB 에도 저장
            if (file != null) {
                file.setPost_id(id);        // FK 설정(외래키 설정)
                attachmentRepository.save(file);        // INSERT
            }
        }
    }

    // 물리적으로 서버에 파일을 저장 (중복된 파일 이름에 대해서 rename 처리를 해줘야함)
    private Attachment upload(MultipartFile multipartFile) {
        Attachment attachment = null;

        // 첨부된 파일 없으면 pass
        String originalFilename = multipartFile.getOriginalFilename();  // 원본이름
        if (originalFilename == null || originalFilename.isEmpty()) return null;

        // 원본 파일명
        // cleanPath : C:/User/aaa/bbb/gr\argagr\aga.add 와 같은 경로 중 "\"를 "/"로 변경
        String sourceName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

        // 저장될 파일명
        String fileName = sourceName;

        // 파일이 중복되는지 확인
        File file = new File(uploadDir, fileName);
        if (file.exists()) {     // 파일이 존재하는지 확인 (참 -> 존재하는 파일명 즉, 중복되었다. 중복되었다면 다른 이름으로 변경하여 저장해야한다,)
            // a.txt => a_2378142783946.txt  : time stamp 값을 활용할거다! (확장자가 있을 경우 파일명_타임스탬프.확장자로 변경해서 처리할 예정)
            // "a" => "a_2378142783946"  : 확장자 없는 경우

            int pos = fileName.lastIndexOf(".");      // 파일 마지막에 .이 있는지 확인(확장자 파악을 위해)
            if (pos > -1) {      // 확장자가 있는 경우
                String name = fileName.substring(0, pos);       // 파일의 '이름'
                String ext = fileName.substring(pos + 1); // 파일의 '확장자'

                // 중복 방지 회피를 위해 새로운 이름 (타임스탬프, 현재시간 ms) 를 파일명에 추가
                fileName = name + "_" + System.currentTimeMillis() + "." + ext;
            } else {             // 확장자가 없는 파일의 경우
                fileName += "_" + System.currentTimeMillis();
            }
        }
        System.out.println("fileName: " + fileName);

        // java.io.* (inputStream, outputStream, reader ....)
        // java.nio.* (Path, Paths, Files ....)를 사용
        Path copyOfLocation = Paths.get(new File(uploadDir, fileName).getAbsolutePath());        // 경로를 표현한 객체
        System.out.println(copyOfLocation);

        try {
            // inputStream을 가져와서
            // copyOfLocation (저장위치)로 파일을 쓴다.
            // copy의 옵션은 기존에 존재하면 REPLACE(대체한다), 오버라이딩 한다

            // java.nio.file.Files
            Files.copy(
                    multipartFile.getInputStream(),
                    copyOfLocation,                 // 저장할 위치
                    StandardCopyOption.REPLACE_EXISTING // 기존에 존재하면 덮어쓰기
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        attachment = Attachment.builder()
                .filename(fileName)         // 저장된 이름
                .sourcename(sourceName)     // 원본이름
                .build();

        return attachment;
    }

    @Override
    @Transactional      // 구현체에도 Transactional 처리 해준다
    public Post detail(Long id) {
        // 조회수 증가(UPDATE)
        postRepository.incViewCnt(id);
        Post post = postRepository.findById(id);        // 정보 읽어오기

        if (post != null) {      // 글이 있다면
            // 첨부파일(들) 정보 가져오기
            List<Attachment> fileList = attachmentRepository.findByPost(post.getId());

            // 첨부파일이 이미지 파일인지 여부 세팅
            setImage(fileList);

            post.setFileList(fileList);
        }

        return post;        // 읽어온 post 를 리턴
    }

    // 05.31 수업 내용
    // 이미지 파일 여부를 세팅해주는 메소드
    private void setImage(List<Attachment> fileList) {
        // upload 실제 물리적인 경로를 얻어와야함
        String realPath = new File(uploadDir).getAbsolutePath();

        for (Attachment attachment : fileList) {
            BufferedImage imgData = null;
            File f = new File(realPath, attachment.getFilename());      // 저장된 첨부파일에 대한 file 객체

            try {
                imgData = ImageIO.read(f);      // 이미지가 아닌 경우에는 imgData = null 리턴
                // ※ ↑ 파일이 존재 하지 않으면 IOExcepion 발생한다
                //   ↑ 이미지가 아닌 경우는 null 리턴

                if (imgData != null)
                    attachment.setImage(true);     // 이미지 파일이라면 true 로 세팅하도록 설정 (불린 값이 false로 되어 있기 때문에 이미지에 대한 설정을 수정)

            } catch (IOException e) {
                System.out.println("파일 존재 안 함: " + f.getAbsolutePath() + " [" + e.getMessage() + "]");      // 주어진 파일이 없으면 발생
            }
        }
    }

    @Override
    public List<Post> list() {
        return postRepository.findAll();
    }

    @Override
    public List<Post> list(Integer page, Model model) {     // 페이징을 할 때 뷰쪽에 넘겨줘야할 요소가 많아서 model 을 담아서 설정함
        // 현재 페이지
        if (page == null || page < 1) page = 1;     // 디폴트로 1페이지

        // 페이징
        // writePages: 한 [페이징] 당 몇개의 페이지가 표시되나
        // pageRows: 한 '페이지'에 몇개의 글을 리스트 할것인가?
        // 세션에 담아서 관리해야함
        HttpSession session = U.getSession();
        Integer writePages = (Integer) session.getAttribute("writePages");
        if (writePages == null) writePages = WRITE_PAGES;       // 없으면 기본 설정값(1페이지), 있으면 설정한 값
        Integer pageRows = (Integer) session.getAttribute("pageRows");
        if (pageRows == null) pageRows = PAGE_ROWS;             // 세션에 없으면 기본값, 있으면 설정한 값

        session.setAttribute("page", page);     // 현재 페이지 번호는 session 에 저장되어 있음.

        long cnt = postRepository.countAll();      // 전체 글 목록 개수 가져오기
        int totalPage = (int) Math.ceil(cnt / (double) pageRows);     // 총 필요한 페이지 계산 (몇 페이지 분량인지)

        // 하단에 표현될 [페이징] 의 '시작페이지'와 '마지막페이지'
        int startPage = 0;
        int endPage = 0;

        // 해당 페이지의 글 목록을 리스트로 준비
        List<Post> list = null;

        if (cnt > 0) {       // 데이터가 최소 1개 이상인 경우만 페이징

            // page 값 보정 (존재하지 않는 페이지에 대한 설정)
            if (page > totalPage) page = totalPage;

            // 쿼리문에 보내줄 fromRow 계산  (몇번째 데이터부터 select 할지)
            int fromRow = (page - 1) * pageRows;

            // [페이징] 에 표시할 '시작페이지' 와 '마지막페이지' 계산
            startPage = (((page - 1) / writePages) * writePages) + 1;
            endPage = startPage + writePages - 1;
            if (endPage >= totalPage) endPage = totalPage;

            // 해당 페이지의 글 목록을 DB 에서 읽어오기
            list = postRepository.selectFromRow(fromRow, pageRows);
            model.addAttribute("list", list);       // model 에 페이지의 글 목록 담기
        } else {
            page = 0;       // 글 목록이 없을 경우 페이징은 0으로 세팅
        }
        // 현재 선언한 페이지들의 템플릿도 가져와야 한다. (startPage, endPage, page ...)
        model.addAttribute("cnt", cnt);  // 전체 글 개수
        model.addAttribute("page", page); // 현재 페이지
        model.addAttribute("totalPage", totalPage);  // 총 '페이지' 수
        model.addAttribute("pageRows", pageRows);  // 한 '페이지' 에 표시할 글 개수

        // [페이징]
        model.addAttribute("url", U.getRequest().getRequestURI());  // 목록 url
        model.addAttribute("writePages", writePages); // [페이징] 에 표시할 숫자 개수
        model.addAttribute("startPage", startPage);  // [페이징] 에 표시할 시작 페이지
        model.addAttribute("endPage", endPage);   // [페이징] 에 표시할 마지막 페이지

        return list;
    }

    @Override
    public Post selectById(Long id) {
        Post post = postRepository.findById(id);

        if (post != null) {
            // 첨부파일 정보 가져오기
            List<Attachment> fileList = attachmentRepository.findByPost(post.getId());
            setImage(fileList);   // 이미지 파일 여부 세팅
            post.setFileList(fileList);
        }
        return post;
    }

    @Override
    public int update(Post post, Map<String, MultipartFile> files, Long[] delfile) {
        int result = 0;
        // 글 수정
        result = postRepository.update(post);

        // 새로운 첨부파일 추가
        addFiles(files, post.getId());

        // 삭제할 첨부파일들은 삭제하기
        if (delfile != null) {          // 삭제할 첨부파일이 있을 경우
            for (Long fileId : delfile) {
                Attachment file = attachmentRepository.findById(fileId);
                if (file != null){
                    delFile(file);      // 물리적으로 파일 삭제
                    attachmentRepository.delete(file);      // DB 에서 삭제
                }
            }
        }

        return result;
    }

    // 특정 첨부파일 물리적으로 삭제하는 메소드
    private void delFile(Attachment file) {
        String saveDirectory = new File(uploadDir).getAbsolutePath();       // 파일의 저장 경로를 절대경로로 뽑아오기

        File f = new File(saveDirectory, file.getFilename());
        System.out.println("삭제시도--> " + f.getAbsolutePath());       // 확인용

        if (f.exists()){        // 파일이 존재한다면
            if (f.delete()){
                System.out.println("삭제 성공");
            }else {
                System.out.println("삭제 실패");
            }
        }else {
            System.out.println("파일이 존재하지 않습니다.");
        }
    }

    @Override
    public int deleteById(Long id) {
        int result = 0;

        Post post = postRepository.findById(id);        // 존재하는 데이터인지 확인하는 용도
        if (post != null) {
            // 물리적으로 저장된 첨부파일(들) 부터 삭제
            List<Attachment> fileList = attachmentRepository.findByPost(id);
            if (fileList != null || fileList.size() > 0) {       // 하나라도 있다면 삭제
                for (Attachment file : fileList){
                    delFile(file);
                }
            }

            // 글 삭제 (참조하는 첨부파일, 댓글 등도 같이 삭제 될 것 임 -> SQL 에 ON DELETE CASCADE 되어 있기 때문)
            result = postRepository.delete(post);
        }

        return result;
    }
}       // end Service
