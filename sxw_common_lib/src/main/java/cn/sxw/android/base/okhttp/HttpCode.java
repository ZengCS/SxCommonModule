package cn.sxw.android.base.okhttp;

public interface HttpCode {
    // 成功
    String OK = "200";

    // 找不到服务器或者接口
    String NOT_FOUND = "404";

    // 服务器错误
    String SERVER_ERROR = "500";

    // 网络没有接通
    String NETWORK_ERROR = "-1";

    // JSON格式错误
    String JSON_ERROR = "-2";

    // 文件创建无权限
    String FILE_NO_PRE = "-3";

    // 文件保存失败
    String FILE_SAVE_ERROR = "-4";

    // 其他错误
    String OTHER_ERROR = "-5";
}