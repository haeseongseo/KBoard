package com.lec.spring.controller;

import com.lec.spring.domain.Post;
import com.lec.spring.domain.PostValidator;
import com.lec.spring.service.BoardService;
import com.lec.spring.util.U;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/board")
public class BoardController {

    // 서비스 단 위임을 위해 만든 BoardService 생성, 주입
    @Autowired  // 서비스 인터페이스 자동주입
    private BoardService boardService;

    // 기본 생성자
    public BoardController() {
        System.out.println("BoardController() 생성");
    }

    @GetMapping("/write")
    public void write() {
        // board/write.html (뷰) 리턴
    }

    @PostMapping("/write")
    public String writeOk(
            @RequestParam Map<String, MultipartFile> files,     // 첨부파일들, Map 에는 <name, file>이 담겨 있음
            @Valid Post post,          // @Valid : 유효성 검사 대상
            BindingResult result,
            Model model,              // ⭐️매개변수 선언시 반드시 Model 은 BindingResult 보다 뒤에 둬야 한다.
            RedirectAttributes redirectAttrs) {        // redirect 시 넘겨줄 값들을 담는 객체
        // 입력받은 post 객체(user, subject, content)를 서비스단에 위임하고 서비스단에 있는 save()를 통해서 저장을 하고 성공 하면 1을 리턴해서 컨트롤러에 가져온다.

        if (result.hasErrors()) {        // 만약에 result 에 에러가 있다면 redirect 한다! (원래 페이지로 redirect)

            // addAttribute
            //    request parameters로 값을 전달.  redirect URL에 query string 으로 값이 담김
            //    request.getParameter에서 해당 값에 액세스 가능
            // addFlashAttribute
            //    일회성. 한번 사용하면 Redirect후 값이 소멸
            //    request parameters로 값을 전달하지않음
            //    '객체'로 값을 그대로 전달

            // redirect 시, 기존에 입력했던 값들은 보이게 하기
            redirectAttrs.addFlashAttribute("user", post.getUser());
            redirectAttrs.addFlashAttribute("subject", post.getSubject());
            redirectAttrs.addFlashAttribute("content", post.getContent());

            // 어떤 에러가 있는지 확인 용도
            List<FieldError> errList = result.getFieldErrors();
            for (FieldError err : errList) {
                // Error + 필드번호 + 코드번호로 출력  (ex-error_user, error_subject ....)
                redirectAttrs.addFlashAttribute("error_" + err.getField(), err.getCode());
            }

            return "redirect:/board/write";
        }

        model.addAttribute("result", boardService.write(post, files));      // files 도 같이 서비스단에 넘김

        return "board/writeOk";     // view 를 writeOk.html 로 리턴
    }

    @GetMapping("/detail/{id}")     // detail/글의 ID
    public String detail(@PathVariable Long id, Model model) {       // @PathVariable : URL 경로의 변수를 메서드 매개변수로 매핑


        model.addAttribute("post", boardService.detail(id));

        return "board/detail";      // board 밑에 있는 detail.html(뷰) 리턴
    }

    @GetMapping("/list")
//    public void list(Model model){
//        model.addAttribute("list", boardService.list());
//    }
    public void list(Integer page, Model model) {        // paging 하는 방법
        boardService.list(page, model);
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable Long id, Model model) {

        model.addAttribute("post", boardService.selectById(id));
        return "board/update";
    }

    @PostMapping("/update")
    public String updateOk(
            @RequestParam Map<String, MultipartFile> files,     // 새로 추가될 파일들
            @Valid Post post,
            BindingResult result,
            Long [] delfile,            // 삭제될 파일들 (여러개가 올라올 수 있기 때문에 배열로 받음)
            Model model,
            RedirectAttributes redirectAttrs
    ) {

        if (result.hasErrors()) {        // 만약에 result 에 에러가 있다면 redirect 한다! (원래 페이지로 redirect)

            // redirect 시, 기존에 입력했던 값들은 보이게 하기
            redirectAttrs.addFlashAttribute("subject", post.getSubject());
            redirectAttrs.addFlashAttribute("content", post.getContent());

            // 어떤 에러가 있는지 확인 용도
            List<FieldError> errList = result.getFieldErrors();
            for (FieldError err : errList) {
                // Error + 필드번호 + 코드번호로 출력  (ex-error_user, error_subject ....)
                redirectAttrs.addFlashAttribute("error_" + err.getField(), err.getCode());
            }
            return "redirect:/board/update/" + post.getId();
        }

        model.addAttribute("result", boardService.update(post, files, delfile));

        // 사이트로 이동하기 위한 용도
        return "board/updateOk";
    }

    @PostMapping("/delete")
    public String deleteOk(Long id, Model model) {
        model.addAttribute("result", boardService.deleteById(id));

        // 팝업 띄어주기 위한 용도
        return "board/deleteOk";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        System.out.println("BoardController initBinder() 호출");  // 호출되는지 확인 용도
        binder.setValidator(new PostValidator());       // PostValidator 를 BoardController 에 등록
    }

    // 페이징
    // pageRows 변경시 동작
    @PostMapping("/pageRows")
    public String pageRows(Integer page, Integer pageRows) {
        // pageRows 를 세션에 저장
        U.getSession().setAttribute("pageRows", pageRows);

        // list.html 의 select(몇개로 보기)를 누를 때마다 url 을 재요청
        return "redirect:/board/list?page=" + page;
    }

}   // end controller
