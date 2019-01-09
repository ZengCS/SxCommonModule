package cn.sxw.android.base.net;


import android.content.Context;

import org.xutils.http.RequestParams;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public interface ApiHelper {
    String REFRESH_TOKEN_API = CustomApiConfig.getCommonServerHost()+"/passport/api/auth/refresh_token";

    void postData(final Context context, String api, Object data, final ApiCallback callback);

    void postData(final Context context, RequestParams params, final ApiCallback callback);

    void getData(final Context context, String api, HashMap<String, String> requestParams, final ApiCallback callback);

    void getData(final Context context, RequestParams params, final ApiCallback callback);

    void postFile(final Context context, String api, HashMap<String, List<File>> requestParams, final ApiCallback callback);

    void setToken(String token);

    void setRefreshToke(String refreshToke);

    // void refreshToken(final Context context, String api);

    void refreshToken(final Context context, String api, final ApiCallback callback);

    interface ApiCallback {
        void onRequestSuccess(String result);

        void onRequestFailed(String message);
    }

    /**
     * 解析Response数据
     */
    void analysisResponseData(Context context, ApiCallback callback, String response);

    /**
     * 解析错误信息
     */
    void analysisErrorInfo(Context context, ApiCallback callback, Throwable ex);
}
