$(function (){  // document.ready()
    // 글 (삭제) 버튼
    $("#btnDel").click(function () {
        let answer = confirm("삭제하시겠습니까?")
        answer && $("form[name='frmDelete']").submit();
    });

    // 현재 글의 id 값 (삭제 기능 부분에 있음)
    const id = $("input[name='id']").val().trim();

    // 현재 글의 댓글들을 불러온다
    loadComment(id);

    // 댓글 작성 버튼 누르면 댓글 등록 하기.
    // 1. 어느글에 대한 댓글인지? --> 위에 id 변수에 담겨있다
    // 2. 어느 사용자가 작성한 댓글인지? --> logged_id 값
    // 3. 댓글 내용은 무엇인지?  --> 아래 content
    $("#btn_comment").click(function (){
        const content = $("#input_comment").val().trim();       // 댓글

        // 검증 (비어있을 경우 제출 못 하게)
        if (!content){
            alert("댓글 입력을 하세요");
            $("#input_comment").focus();
            return;
        }

        // POST 방식으로 전달할 parameter 준비
        const data = {
            "post_id": id,      // CommentController 에 지정한 이름으로 사용해야 함
            "user_id": logged_id,
            "content": content,
        };

        $.ajax({
            url: "/comment/write",
            type: "POST",
            data: data,
            cache: false,
            success: function (data, status){
                if (status == "success"){
                    if (data.status !== "OK"){      // OK 가 아니면 리턴
                        alert(data.status);
                        return;
                    }
                    loadComment(id);        // 댓글 목록 다시 업데이트
                    $("#input_comment").val(''); // 그 후 입력 칸은 비우기
                }
            },
        })
    });
});

// 특정 글(post)id) 의 댓글 목록 읽어오기
function loadComment(post_id){

    $.ajax({
        url: "/comment/list/" + post_id,
        type: "GET",
        cache: false,
        success: function (data, status){
            if (status == "success"){
                // 서버에 에러 메세지 있는 경우 (공통적으로 status 를 받기 때문에 해당 객체로 비교)
                if (data.status !== "OK"){
                    alert(data.status);
                    return;
                }
                buildComment(data);     // 전달 받은 response 를 갖고 댓글 화면 렌더링

                // 이벤트 요소가 바뀌었을 때 이벤트가 등록되게 해줘야 함 (댓글에 삭제버튼이 로딩되어야 해야함)
                // ⭐️ 댓글목록을 불러오고 난뒤에 삭제에 대한 이벤트 리스너를 등록해야 한다
                addDelete();
            }
        },
    });
}

function buildComment(result){
    $("#cmt_cnt").text(result.count);       // 댓글의 총 개수

    const out = [];
    result.data.forEach(comment => {        // data 는 배열로 설계함

        let id = comment.id;        // 댓글의 id
        let content = comment.content.trim();
        let regdate = comment.regdate;

        let user_id = parseInt(comment.user.id);
        let username = comment.user.username;
        let name = comment.user.name;

        // 삭제 버튼 여부 (로그인한 유저가 댓글의 유저가 다르면 비어있고, 일치한다면 삭제할 수 있는 버튼을 보여줌)
        const delBtn = (logged_id !== user_id) ? '' : `
            <i class="btn fa-solid fa-delete-left text-danger" data-bs-toggle="tooltip"  data-cmtdel-id="${id}" title="삭제"></i>
        `;
        // data-cmtdel-id="${id}" : 몇번 데이터의 댓글을 삭제할 것인지에 대해서

        const row = `
            <tr>
                <td><span><strong>${username}</strong><br><small class="text-secondary">(${name})</small></span></td>
                <td>
                    <span>${content}</span>${delBtn}
                </td>
                <td><span><small class="text-secondary">${regdate}</small></span></td>
            </tr>
        `;
        out.push(row);
    });

    $("#cmt_list").html(out.join('\n'));        // 배열을 줄 바꿈으로 묶어서 출력 (cmt_list -> 댓글의 목록)
}


// 댓글 삭제 버튼이 눌렸을 때, 해당 댓글이 삭제하는 이벤트를 등록
function addDelete(){
    // 현재 글의 id
    const id = $("input[name='id']").val().trim();

    // 댓글 삭제 버튼마다 준 attribute 가져옴
    $("[data-cmtdel-id]").click(function (){
        if (!confirm("댓글을 삭제하시겠습니까?")) return;

        // 삭제할 댓글의 id
        const comment_id = $(this).attr("data-cmtdel-id");      // 여기에(data-cmtdel-id) 댓글의 id 를 담아둠

        $.ajax({
            url: "/comment/delete",
            type: "POST",
            cache: false,
            data: {"id": comment_id},
            success: function (data, status){
                if (status == "success"){
                    if (data.status !== "OK"){
                        alert(data.status);
                        return;
                    }

                    // 삭제 후에도 댓글 목록 불러와야함
                    loadComment(id);
                }
            },
        });
    })
}