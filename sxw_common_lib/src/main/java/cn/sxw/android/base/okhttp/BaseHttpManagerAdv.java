package cn.sxw.android.base.okhttp;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.sxw.android.base.net.bean.BaseResponse;
import cn.sxw.android.base.utils.JTextUtils;
import cn.sxw.android.base.utils.LogUtil;
import cn.sxw.android.base.utils.NetworkUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/07/16 13:17
 */
public class BaseHttpManagerAdv implements OkApiHelper {
    private static final int METHOD_GET = 0;
    private static final int METHOD_POST = 1;
    private static final int METHOD_PUT = 2;
    private static final int METHOD_DELETE = 3;
    private static final String[] METHOD_NAMES = {"GET", "POST", "PUT", "DELETE"};

    private static BaseHttpManagerAdv sInstance = null;
    // 公用Handler
    private Handler mHandler = new Handler(Looper.getMainLooper());

    // 单例控制
    public static BaseHttpManagerAdv getInstance() {
        if (sInstance == null) {
            synchronized (BaseHttpManagerAdv.class) {
                if (sInstance == null) {
                    sInstance = new BaseHttpManagerAdv();
                }
            }
        }
        return sInstance;
    }

    // 设置统一结果处理回调函数
    private OnResultCallback onResultCallback;

    public BaseHttpManagerAdv setOnResultCallback(OnResultCallback callback) {
        onResultCallback = callback;
        return this;
    }

    @Override
    public void sendGet(BaseRequest request) {
        sendRequest(request, METHOD_GET);
    }

    @Override
    public void sendPost(@NonNull BaseRequest request) {
        sendRequest(request, METHOD_POST);
    }

    public <V> void sendRequest(BaseRequest req, int methodType) {
        // 把部分对象抽出来
        Activity activity = req.getActivity();
        String url = req.getApi();
        Map<String, String> headMap = req.getHeadMap();
        HttpCallback<BaseRequest, V> callback = req.getHttpCallback();

        if (!NetworkUtil.isConnected(activity)) {
            if (canCallback(activity, callback)) {
                mHandler.post(() -> callback.onFail(null, HttpCode.NETWORK_ERROR, "请检查网络是否连接"));
            }
            return;
        }
        if (canCallback(activity, callback)) {
            // 回调onStart，开发者可在onStart中显示Loading状态
            mHandler.post(callback::onStart);
        }

        new Thread(() -> {
            try {
                // 发送请求并得到相应
                String response = execute(url, headMap, req.toJson(), methodType);

                // 解析Response
                if (JTextUtils.isJsonObject(response)) {//是否为Json
                    if (onResultCallback != null) onResultCallback.onResult(response);

                    BaseResponse baseResponse = JSON.parseObject(response, BaseResponse.class);
                    if (baseResponse == null) {
                        if (canCallback(activity, callback)) {
                            mHandler.post(() -> callback.onFail(req, HttpCode.INTERNAL_SERVER_ERROR, response));
                        }
                        return;
                    }
                    if (baseResponse.isRequestSuccess()) {
                        String data = baseResponse.getData();
                        if (JTextUtils.isJsonString(data)) {// 如果是JSON字符串，通过FastJson进行转码
                            V bean = JSON.parseObject(data, req.getTypeReference().getType());
                            if (onResultCallback != null) onResultCallback.onSuccess(bean);
                            if (canCallback(activity, callback)) {
                                mHandler.post(() -> callback.onResult(req, bean));
                            }
                        } else {// 非JSON字符串，直接强转类型返回
                            V bean = (V) data;
                            if (onResultCallback != null) onResultCallback.onSuccess(bean);
                            if (canCallback(activity, callback)) {
                                mHandler.post(() -> callback.onResult(req, bean));
                            }
                        }
                    } else {
                        if (onResultCallback != null)
                            onResultCallback.onError(req, baseResponse.getMessage());
                        if (canCallback(activity, callback)) {
                            mHandler.post(() -> callback.onFail(req, String.valueOf(baseResponse.getCode()), baseResponse.getMessage()));
                        }
                    }
                } else if (response.contains("403") || response.contains("Forbidden")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.FORBIDDEN, "没有访问权限!"));
                    }
                } else if (response.contains("404") || response.contains("Not Found") || response.contains("NotFound")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.NOT_FOUND, "接口地址不存在!"));
                    }
                } else if (response.contains("405") || response.contains("Not Allowed")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.NOT_ALLOWED, "当前接口不支持[" + METHOD_NAMES[methodType] + "]方式请求!"));
                    }
                } else if (response.contains("500") || response.contains("Internal Server Error")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.INTERNAL_SERVER_ERROR, "内部服务器错误!"));
                    }
                } else if (response.contains("502") || response.contains("Bad Gateway")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.BAD_GATEWAY, "Bad Gateway!"));
                    }
                } else if (response.contains("504") || response.contains("Gateway Timeout")) {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.BAD_GATEWAY, "访问接口超时!"));
                    }
                } else {
                    if (canCallback(activity, callback)) {
                        mHandler.post(() -> callback.onFail(null, HttpCode.JSON_ERROR, "数据格式不正确!"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (canCallback(activity, callback)) {
                    mHandler.post(() -> callback.onFail(null, HttpCode.NOT_FOUND, "接口地址不存在！"));
                }
            } catch (JsonSyntaxException | JSONException e) {
                e.printStackTrace();
                if (canCallback(activity, callback)) {
                    mHandler.post(() -> {
                        String errorMsg = e.getMessage();
                        if (!TextUtils.isEmpty(errorMsg)) {
                            if (errorMsg.startsWith("exepct '[', but {")) {
                                errorMsg = "无法将对象解析成列表";
                            } else if (errorMsg.startsWith("exepct '{', but [")) {
                                errorMsg = "无法将对象解析成列表";
                            }
                        } else {
                            errorMsg = "JSON格式错误";
                        }
                        callback.onFail(null, HttpCode.JSON_ERROR, errorMsg);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (canCallback(activity, callback)) {
                    mHandler.post(() -> callback.onFail(null, HttpCode.OTHER_ERROR, e.getMessage()));
                }
            }

            //运行结束
            if (canCallback(activity, callback)) {
                mHandler.post(callback::onFinish);
            }
        }).start();
    }

    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15000, TimeUnit.MILLISECONDS)
            .readTimeout(15000, TimeUnit.MILLISECONDS)
            // .writeTimeout(15000, TimeUnit.MILLISECONDS)
            // 禁用缓存
            // .cache(new Cache(BaseApplication.getContext().getCacheDir(), 10 * 1024 * 1024))
            .build();

    private String execute(String url, Map<String, String> headMap, String jsonString, int methodType) throws IOException {
        LogUtil.methodStartHttp("发送Http请求");
        LogUtil.methodStepHttp("API = " + url);
        LogUtil.methodStepHttp("bodyParam = " + jsonString);

        // 设置请求参数体
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);

        // 设置Request对象
        Request.Builder requestBuilder = new Request.Builder().url(url);
        switch (methodType) {
            case METHOD_GET:
                requestBuilder.get();
                break;
            case METHOD_POST:
                requestBuilder.post(requestBody);
                break;
            case METHOD_PUT:
                requestBuilder.put(requestBody);
                break;
            case METHOD_DELETE:
                requestBuilder.delete(requestBody);
                break;
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (headMap == null)
            headMap = new HashMap<>();
        // 设置全局Request-ID
        String requestId = System.nanoTime() + "" + (int) (Math.random() * 9000 + 1000);
        headMap.put("Request-Id", requestId);
        // ********* Log打印Header参数 *********
        for (String key : headMap.keySet()) {
            requestBuilder.addHeader(key, headMap.get(key));
            stringBuilder.append("\n* --> ").append(key).append(" = ").append(headMap.get(key));
        }
        LogUtil.methodStepHttp("HEADERS" + stringBuilder.toString());

        Response response = httpClient.newCall(requestBuilder.build()).execute();
        ResponseBody body = response.body();
        String bodyString = "";
        if (body != null)
            bodyString = body.string().trim();
        LogUtil.methodStepHttp(bodyString);
        return bodyString;
    }

    private String executeFile(String url, Map<String, String> headMap, Object param, List<File> files) throws IOException {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (File file : files) {
            requestBody.addFormDataPart(file.getName(), file.getName(),
                    RequestBody.create(MediaType.parse("image/jpeg"), file)
            );
        }
        Map<String, String> paramMap = objectToMap(param);
        StringBuffer paramSb = new StringBuffer();
        paramSb.append("?");
        for (String key : paramMap.keySet()) {
            paramSb.append(key).append("=").append(paramMap.get(key)).append("&");
        }
        Request.Builder request = new Request.Builder().url(url + paramSb.substring(0, paramSb.length() - 1)).post(requestBody.build());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url + paramSb.toString());
        if (headMap != null && headMap.size() > 0) {
            for (String key : headMap.keySet()) {
                request.addHeader(key, headMap.get(key));
                stringBuilder.append("\n").append(key + "=" + headMap.get(key));
            }
        }
        LogUtil.methodStepHttp(stringBuilder.toString());

        Response response = httpClient.newCall(request.build()).execute();
        String bodyString = response.body().string().trim();
        LogUtil.methodStepHttp(bodyString);
        return bodyString;
    }

    private Map<String, String> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        Field[] fields = object.getClass().getFields();
        String key;
        Object value;
        for (Field field : fields) {
            key = field.getName();
            try {
                value = field.get(object);
                if (value == null || Modifier.isTransient(field.getModifiers())) {//空值&Transient标签不解析
                    continue;
                }
                map.put(key, value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    public interface OnResultCallback {
        /**
         * 返回为JSON数据都会回调
         */
        void onResult(String json);

        /**
         * 执行错误回调
         */
        void onError(Object req, String msg);

        /**
         * 执行成功回调
         */
        void onSuccess(Object json);
    }

    public boolean downloadFile(Activity activity, String req, File outFile, HttpFileCallBack callback) {
        if (!NetworkUtil.isConnected(activity)) {
            if (canCallback(activity, callback)) {
                mHandler.post(() -> callback.onFail(null, HttpCode.NETWORK_ERROR, "请检查网络是否连接"));
            }
            return false;
        }

        if (canCallback(activity, callback)) {
            mHandler.post(callback::onStart);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mHandler.post(() -> callback.onFail(null, HttpCode.FILE_NO_PRE, "文件创建失败！"));
        }
        Request request = new Request.Builder().url(req).build();
        FileOutputStream outputStream = fileOutputStream;
        new Thread(new Runnable() {
            long total = 0;//文件总大小；
            long down = 0;//已下载大小

            @Override
            public void run() {
                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                        if (canCallback(activity, callback)) {
                            mHandler.post(() -> callback.onFail(null, HttpCode.NOT_FOUND, "访问服务器失败！"));
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream inputStream = null;
                        try {
                            total = response.body().contentLength();//文件总大小；
                            down = 0;
                            inputStream = response.body().byteStream();
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, len);
                                down += len;
                                if (canCallback(activity, callback)) {
                                    mHandler.post(() -> callback.onProgress(down, total));
                                }
                            }

                            if (canCallback(activity, callback)) {
                                mHandler.post(() -> callback.onResult(req, outFile));
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            if (canCallback(activity, callback)) {
                                mHandler.post(() -> callback.onFail(req, HttpCode.FILE_SAVE_ERROR, "下载失败！"));
                            }
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        }

                        //方式二
//                        Sink sink;
//                        BufferedSink bufferedSink = null;
//                        try {
//                            sink = Okio.sink(outFile);
//                            bufferedSink = Okio.buffer(sink);
//                            bufferedSink.writeAll(response.body().source());
//                            bufferedSink.close();
//                            Log.i("DOWNLOAD", "download success");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Log.i("DOWNLOAD", "download failed");
//                        } finally {
//                            if (bufferedSink != null) {
//                                bufferedSink.close();
//                            }
//
//
//                        }

                        //运行结束
                        if (canCallback(activity, callback)) {
                            mHandler.post(callback::onFinish);
                        }
                    }
                });
            }
        }).start();
        return true;
    }

    // 是否允许回调
    private boolean canCallback(Activity activity, HttpCallback callback) {
        return callback != null && (activity == null || !activity.isFinishing());
    }
}
