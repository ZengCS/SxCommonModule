package cn.sxw.android.base.net;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;
import org.xutils.ex.HttpException;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import cn.sxw.android.R;
import cn.sxw.android.base.net.bean.BaseResponse;
import cn.sxw.android.base.net.bean.LocalTokenCache;
import cn.sxw.android.base.net.bean.RefreshToken;
import cn.sxw.android.base.utils.LogUtil;
import cn.sxw.android.base.utils.NetworkUtil;

@Singleton
public class HttpRequestHelper implements ApiHelper {
    private static final String TAG = "HttpRequestHelper";

    private static final int TIME_OUT = 5 * 1000;// 5秒超时

    private static final int CODE_TOKEN_NOT_FOUND = 8500;// hToken not found

    private static final int CODE_TOKEN_ERROR = 8501;// hToken错误

    private static final int CODE_TOKEN_TIMEOUT = 8502;// Token过期

    private static final int CODE_TOKEN_SIGN_ERROR = 8503;//TOKEN签名错误

    private static final int CODE_NOT_SUPPORT_TOKEN_ERROR = 8504;//不支持的TOKEN

    private static final int CODE_TOKEN_STRING_ERROR = 8505;//TOKEN字符串无效

    public static final int CODE_PASSWORD_ERROR = 8072401;// 密码错误码

    private String token;

    private String refreshToken;

    @Inject
    public HttpRequestHelper() {

    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRefreshToke(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    private String getRefreshToken() {
        if (TextUtils.isEmpty(refreshToken)) {
            refreshToken = LocalTokenCache.getLocalCacheRefreshToken();
        }
        return refreshToken;
    }

    /**
     * 刷新token 如果刷新失败 发送广播 重新登录
     *
     * @param context
     * @param api
     */
    @Override
    public void refreshToken(final Context context, String api, final ApiCallback callback) {
        RequestParams requestParams = new RequestParams(api);
        requestParams.setHeader("token", getRefreshToken());
        requestParams.setCacheMaxAge(0);// 为请求添加缓存时间
        requestParams.setConnectTimeout(5 * 1000); // 请求超时时间
        x.http().request(HttpMethod.GET, requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.d(TAG, "refreshToken：" + result);
                BaseResponse baseResponse = JSON.parseObject(result, BaseResponse.class);
                if (baseResponse.isRequestSuccess()) {
                    String tokenResult = baseResponse.getData();
                    if (!TextUtils.isEmpty(tokenResult)) {
                        LogUtil.d(TAG, "refreshToken: 刷新TOKEN成功~");
                        RefreshToken refreshToken = JSON.parseObject(tokenResult, RefreshToken.class);
                        setRefreshToke(refreshToken.getRefreshToken());
                        setToken(refreshToken.getToken());
                        if (callback != null)
                            callback.onRequestSuccess(tokenResult);
                    } else {
                        LogUtil.w(TAG, "refreshToken: 刷新TOKEN失败~");
                        Toast.makeText(context, R.string.login_time_out, Toast.LENGTH_SHORT).show();
                        context.sendBroadcast(new Intent("cn.sxw.kt.action.LOGIN_AGAIN"));
                        if (callback != null)
                            callback.onRequestFailed("TOKEN_TIME_OUT");
                    }
                } /*else if (baseResponse.getCode() == TOKEN_TIME_OUT || baseResponse.getCode() == TOKEN_ERROR) {
                    Toast.makeText(context, R.string.login_time_out, Toast.LENGTH_SHORT).show();
                    context.sendBroadcast(new Intent("cn.sxw.kt.action.LOGIN_AGAIN"));
                    if (callback != null)
                        callback.onRequestFailed("TOKEN_TIME_OUT");
                }*/ else {
                    LogUtil.w(TAG, "refreshToken: 刷新TOKEN失败~");
                    Toast.makeText(context, R.string.login_time_out, Toast.LENGTH_SHORT).show();
                    context.sendBroadcast(new Intent("cn.sxw.kt.action.LOGIN_AGAIN"));
                    if (callback != null)
                        callback.onRequestFailed("TOKEN_TIME_OUT");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                // 如果刷新Token失败，不做任何操作
//                Toast.makeText(context, R.string.login_time_out, Toast.LENGTH_SHORT).show();
//                context.sendBroadcast(new Intent("cn.sxw.kt.action.LOGIN_AGAIN"));
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void setTokenHeader(RequestParams params) {
        // 在网络请求时 增加一个head ：字段为Request-Id 值为 用户id+当前时间  类型为long型啊
        String requestId = System.nanoTime() + "" + (int) (Math.random() * 9000 + 1000);
        params.setHeader("Request-Id", requestId);
        LogUtil.d(TAG, "----Request-Id = " + requestId);

        if (!TextUtils.isEmpty(token)) {
            params.setHeader("token", token);
        } else {
            String cacheToken = LocalTokenCache.getLocalCacheToken();
            if (!TextUtils.isEmpty(cacheToken)) {
                params.setHeader("token", cacheToken);
            } else {
                LogUtil.w("Token is empty");
            }
        }
    }

    private void requestData(final Context context, HttpMethod method, RequestParams params, final ApiCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onRequestFailed(context.getResources().getString(R.string.network_access_error));
            return;
        }
        LogUtil.d(TAG, "requestData() called with: context = [" + context + "], method = [" + method + "], params = [" + params + "], callback = [" + callback + "]");
        LogUtil.d(TAG, "-----------api=" + params.getUri());
        LogUtil.d(TAG, "-----------data=" + params.getBodyContent());
        params.setConnectTimeout(TIME_OUT);// 默认值:1000 * 15
        params.setAsJsonContent(true);
        setTokenHeader(params);
        x.http().request(method, params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                analysisResponseData(context, callback, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                analysisErrorInfo(context, callback, ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.d(TAG, "-------------onCancelled");
            }

            @Override
            public void onFinished() {
            }
        });
    }

    @Override
    public void postData(Context context, RequestParams params, ApiCallback callback) {
        requestData(context, HttpMethod.POST, params, callback);
    }

    @Override
    public void getData(Context context, RequestParams params, ApiCallback callback) {
        requestData(context, HttpMethod.GET, params, callback);
    }

    /**
     * HTTP POST
     *
     * @param api
     * @param data
     * @param callback
     */
    public void postData(final Context context, String api, Object data, final ApiHelper.ApiCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onRequestFailed(context.getResources().getString(R.string.network_access_error));
            return;
        }
        LogUtil.d(TAG, "postData() called with: context = [" + context + "], api = [" + api + "], data = [" + data + "], callback = [" + callback + "]");
        RequestParams params = new RequestParams(api);
        setTokenHeader(params);
        params.setBodyContent(JSON.toJSONString(data));
        LogUtil.d(TAG, "-----------api=" + api);
        LogUtil.d(TAG, "-----------data=" + params.getBodyContent());
        params.setConnectTimeout(TIME_OUT);// 默认值:1000 * 15
        params.setAsJsonContent(true);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                analysisResponseData(context, callback, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                analysisErrorInfo(context, callback, ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.d(TAG, "-------------onCancelled");
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * HTTP GET 方法获取数据
     *
     * @param api
     * @param requestParams
     * @param callback
     */
    public void getData(final Context context, String api, HashMap<String, String> requestParams, final ApiHelper.ApiCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onRequestFailed(context.getResources().getString(R.string.network_access_error));
            return;
        }
        LogUtil.d(TAG, "getData() called with: context = [" + context + "], api = [" + api + "], requestParams = [" + requestParams + "], callback = [" + callback + "]");
        RequestParams params = new RequestParams(api);
        if (requestParams != null) {
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                params.addQueryStringParameter((entry.getKey()), (entry.getValue()));
            }
        }
        LogUtil.d(TAG, "getData: api=" + params.getUri());
        setTokenHeader(params);
        params.setConnectTimeout(TIME_OUT);// 默认值:1000 * 15
        params.setAsJsonContent(true);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                analysisResponseData(context, callback, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                analysisErrorInfo(context, callback, ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 文件上传
     *
     * @param api
     * @param requestParams
     * @param callback
     */
    public void postFile(final Context context, String api, HashMap<String, List<File>> requestParams, final ApiHelper.ApiCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onRequestFailed(context.getResources().getString(R.string.network_access_error));
            return;
        }
        RequestParams params = new RequestParams(api);
        if (requestParams != null && requestParams.size() > 0) {
            for (Map.Entry<String, List<File>> entry : requestParams.entrySet()) {
                for (File file : entry.getValue()) {
                    params.addBodyParameter(((String) entry.getKey()), file);
                }
            }
        }
        setTokenHeader(params);
        params.setConnectTimeout(TIME_OUT);// 默认值:1000 * 15
        params.setMultipart(true);
        params.setAsJsonContent(true);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                analysisResponseData(context, callback, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                analysisErrorInfo(context, callback, ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    public void analysisErrorInfo(Context context, ApiCallback callback, Throwable ex) {
        LogUtil.d(TAG, "*********************** 解析Error数据 ***********************");
        ex.printStackTrace();

        if (!TextUtils.isEmpty(ex.getMessage())) {
            if (ex.getMessage().equals("Not Found")) {
                LogUtil.e(TAG, "analysisErrorInfo: API NOT FOUND");
                callback.onRequestFailed(context.getResources().getString(R.string.get_info_failed));
            } else {
                LogUtil.e(TAG, "analysisErrorInfo: " + ex.getMessage());
                callback.onRequestFailed(ex.getMessage());
            }
        } else {
            // TODO 判断错误类型，实施刷新Token的操作
            if (ex instanceof HttpException) {
                HttpException he = (HttpException) ex;
                String result = he.getResult();
                if (!TextUtils.isEmpty(result)) {
                    if (result.contains("Internal Server Error")) {
                        callback.onRequestFailed(context.getResources().getString(R.string.internal_server_error));
                        LogUtil.e(TAG, "analysisErrorInfo: 内部服务器错误");
                        return;
                    }
                    BaseResponse baseResponse = JSON.parseObject(result, BaseResponse.class);
                    if (baseResponse.getCode() == CODE_TOKEN_TIMEOUT) {
                        LogUtil.w(TAG, "---------TOKEN已过期或TOKEN错误");
                        refreshToken(context, REFRESH_TOKEN_API, callback);
                        return;
                    }
                }
            }
            callback.onRequestFailed(context.getResources().getString(R.string.server_error));
            LogUtil.e(TAG, "analysisErrorInfo: 服务器错误");
        }
    }

    @Override
    public void analysisResponseData(Context context, ApiCallback callback, String response) {
        LogUtil.d(TAG, "*********************** 解析Response数据 ***********************");
        LogUtil.d(TAG, String.valueOf(response));
        BaseResponse baseResponse = JSON.parseObject(response, BaseResponse.class);
        if (baseResponse == null) {
            callback.onRequestFailed("ResponseIsEmpty");
            return;
        }
        if (baseResponse.isRequestSuccess()) {
            LogUtil.d(TAG, "---------data=" + baseResponse.getData());
            callback.onRequestSuccess(baseResponse.getData());
        } else if (baseResponse.getCode() == CODE_TOKEN_TIMEOUT) {
            LogUtil.w(TAG, "---------TOKEN已过期或TOKEN错误");
            refreshToken(context, REFRESH_TOKEN_API, callback);
        } else if (baseResponse.getCode() == CODE_TOKEN_NOT_FOUND ||
                baseResponse.getCode() == CODE_TOKEN_ERROR ||
                baseResponse.getCode() == CODE_NOT_SUPPORT_TOKEN_ERROR ||
                baseResponse.getCode() == CODE_TOKEN_SIGN_ERROR ||
                baseResponse.getCode() == CODE_TOKEN_STRING_ERROR) {
            Toast.makeText(context, R.string.login_time_out, Toast.LENGTH_SHORT).show();
            context.sendBroadcast(new Intent("cn.sxw.kt.action.LOGIN_AGAIN"));
        } else if (baseResponse.isFormatError()) {
            LogUtil.w(TAG, "---------API返回了错误格式的JSON数据！");
            callback.onRequestFailed(context.getResources().getString(R.string.json_format_error));
        } else {
            // 增加密码错误码判断
            if (baseResponse.getCode() == CODE_PASSWORD_ERROR) {
                callback.onRequestFailed(String.valueOf(baseResponse.getCode()));
            } else if (baseResponse.getCode() == 200) {// 为了满足那些只有code返回值，没有success返回值的接口
                LogUtil.d(TAG, "---------data=" + response);
                callback.onRequestSuccess(response);
            } else {
                LogUtil.w(TAG, "---------数据加载失败，message=" + baseResponse.getMessage());
                callback.onRequestFailed(baseResponse.getMessage());
            }
        }
    }
}
