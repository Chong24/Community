$(function(){
    $("#uploadForm").submit(upload);
});

function upload() {
    $.ajax({
        //在七牛云中根据地区可以查询到
        url: "http://upload-z2.qiniup.com",
        //提交数据，所以是post请求
        method: "post",
        //表示不要将表单转为字符串
        processData: false,
        //不让jquery设置上传的类型，由浏览器设置
        contentType: false,
        //封装表单数据，得到的jQuery对象，取数组为dom对象
        data: new FormData($("#uploadForm")[0]),
        success: function(data) {
            if(data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
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
                alert("上传失败!");
            }
        }
    });
    //取消默认的操作，要不然还是会提交表单，但又没写action就会报错
    return false;
}