package cn.sxw.android.base.okhttp;

/**
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/08/20 14:05
 */
public class ErrorEntity {
    /** 错误原因 */
    public String reason;
    /** 错误消息 */
    public String message;
    public Error[] errors;

    public class Error {
        /** 错误码 */
        public String errorCode;
        /** 错误原因(提示) */
        public String reason;
    }
}
