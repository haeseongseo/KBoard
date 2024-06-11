$(function (){  // document.ready()

    // [추가] 버튼 누르면 될 첨부파일 추가 가능
    let i = 0;
    $("#btnAdd").click(function(){
        $("#files").append(`
           <div class="input-group mb-2">
               <input class="form-control col-xs-3" type="file" name="upfile${i}"/>
               <button type="button" class="btn btn-outline-danger" onclick="$(this).parent().remove()">삭제</button>
           </div>`);
        i++;
    });

    // [삭제] 버튼을 누르면 삭제될 첨부파일 id 값을 담기
    $("[data-fileid-del]").click(function (){
        let fileId = $(this).attr("data-fileid-del");       // data-fileid-del 의 정보 가져오기
        deleteFiles(fileId);        // deleteFiles 함수 실행
        $(this).parent().remove();      // 선택된 해당 파일을 삭제
    });

    // Summernote 추가    (글 작성 부분에 Summernote 로 치환되어서 나옴)
    $("#content").summernote({      // 초기화 옵션을 {} 에 넣어줌 (홈페이지에 자세하게 나와있으니 만들때 참고)
        height: 300,
    });
});

function deleteFiles(fileId){
    // 삭제할 file 의 id 값(들)을 #delFiles 에 담아서 글 수정 완료시 submit 되게 하는 함수
    $("#delFiles").append(`<input type='hidden' name='delfile' value='${fileId}'>`);
}