$(function (){  // document.ready()
    // 페이징 헤더 동작
    $("[name='pageRows']").change(function (){
        // alert($(this).val());   // 확인용

        $("[name='frmPageRows']").attr({        // <form> 의 액션과 메소드 설정해줌 (여러개 줄 때는 {} 사용)
            "method": "POST",
            "action": "/board/pageRows",
        }).submit();
    })
});