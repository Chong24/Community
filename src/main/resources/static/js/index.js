//绑定发布帖子的单击事件，点击就会执行后面的publish函数
$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    //隐藏框
    $("#publishModal").modal("hide");

    //根据输入框的id属性获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    //发送Ajax请求（post，因为要想服务器发送数据）
    $.post(
        //请求路径
        CONTEXT_PATH + "/discuss/add",
        //请求要发送给服务器的内容，Json形式
        {"title": title, "content": content},
        //相应函数，data为服务器发送给客户端的数据
        function (data) {
            //将数据转为JSON形式
            data = $.parseJSON(data);
            //在提示框显示返回的交互信息
            $("#hintBody").text(data.msg);
            //显示提示框
            $("#hintModal").modal("show");
            //2秒后，自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    )
}