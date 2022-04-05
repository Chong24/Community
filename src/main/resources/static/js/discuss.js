$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else{
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop() {
    var btn = this;
	if($(btn).hasClass("btn-info")) {
        $.post(
            CONTEXT_PATH + "/discuss/top",
            {"id":$("#postId").val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        $.post(
            CONTEXT_PATH + "/discuss/unTop",
            {"id":$("#postId").val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                     window.location.reload();
                } else {
                     alert(data.msg);
                }
            }
        );
    }
}

// 加精
function setWonderful() {
    var btn = this;
	if($(btn).hasClass("btn-info")) {
        $.post(
            CONTEXT_PATH + "/discuss/wonderful",
            {"id":$("#postId").val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        $.post(
            CONTEXT_PATH + "/discuss/unWonderful",
            {"id":$("#postId").val()},
            function(data) {
                data = $.parseJSON(data);
                if(data.code == 0) {
                     window.location.reload();
                } else {
                     alert(data.msg);
                }
            }
        );
    }
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}