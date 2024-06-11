$(function (){  // document.ready()
    // [추가] 버튼 누르면 첨부 파일 추가 가능하는 기능 (임의의 개수 첨부파일 추가)
    let i = 0;
    $("#btnAdd").click(function (){
        // files id에 추가 (버튼을 누를 때마다 div id가 files 밑에 해당 태그를 추가)
        // 삭제 버튼을 누르면 <div> 태그를 삭제
        $("#files").append(`
                <div class="input-group mb-2">
                   <input class="form-control col-xs-3" type="file" name="upfile${i}"/>
                   <button type="button" class="btn btn-outline-danger" onclick="$(this).parent().remove()">삭제</button>
               </div>`);
        i++;        // 파일명이 중복되지 않도록 설정
    });

    // Summernote 추가    (글 작성 부분에 Summernote 로 치환되어서 나옴)
    $("#content").summernote({      // 초기화 옵션을 {} 에 넣어줌 (홈페이지에 자세하게 나와있으니 만들때 참고)
        height: 300,
    });
});