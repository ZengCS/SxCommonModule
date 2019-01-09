package cn.sxw.android.base.okhttp;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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

import cn.sxw.android.base.ui.BaseApplication;
import cn.sxw.android.base.utils.BaseLogUtil;
import cn.sxw.android.base.utils.NetworkUtil;
import okhttp3.Cache;
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
 * BaseHttpManager
 *
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/07/16 13:17
 */
public class BaseHttpManagerAdv {
    private static BaseHttpManagerAdv httpManager = new BaseHttpManagerAdv();
    private Handler handler = new Handler(Looper.getMainLooper());

    public static BaseHttpManagerAdv instance() {
        return httpManager;
    }

    private OnResultCallback onResultCallback;

    public BaseHttpManagerAdv setOnResultCallback(OnResultCallback callback) {
        onResultCallback = callback;
        return this;
    }

    public boolean postNew(BaseRequest request) {
        return post(
                request.getActivity(),
                request.getApi(),
                request.getHeadMap(),
                request,
                request.getTypeToken(),
                request.getHttpCallback());
    }

    public <V> boolean post(Activity activity, String url, Map<String, String> headMap, BaseRequest req, TypeToken<V> typeToken, HttpCallback<BaseRequest, V> callback) {
        if (!NetworkUtil.isConnected(activity)) {
            if (callback != null && (activity == null || !activity.isFinishing())) {
                handler.post(() -> callback.onFail(null, HttpCode.NO_NET, "请检查网络是否连接"));
            }
            return false;
        }

        if (callback != null && (activity == null || !activity.isFinishing())) {
            handler.post(() -> callback.onStart(req));
        }

        new Thread(() -> {
            try {
                String resJson = execute(url, headMap, req.toJson());

                if (resJson.startsWith("{") && resJson.endsWith("}") || resJson.startsWith("[") && resJson.endsWith("]")) {//是否为Json
                    if (onResultCallback != null) {
                        onResultCallback.onResult(resJson);
                    }

                    ErrorEntity errorEntity = null;
                    if (resJson.startsWith("{") && resJson.endsWith("}")) {
                        errorEntity = GsonUtil.fromJson(resJson, ErrorEntity.class);
                    }

                    if (errorEntity == null || (TextUtils.isEmpty(errorEntity.reason) &&
                            TextUtils.isEmpty(errorEntity.message) && errorEntity.errors == null)) {
                        V res = GsonUtil.fromJson(resJson, typeToken.getType());
                        if (onResultCallback != null) {
                            onResultCallback.onSuccess(res);
                        }
                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            handler.post(() -> callback.onResult(req, res));
                        }
                    } else {
                        if (onResultCallback != null) {
                            onResultCallback.onError(req, errorEntity);
                        }
                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            ErrorEntity finalErrorEntity = errorEntity;
                            handler.post(() -> {
                                String errorCode = HttpCode.OTHER_ERROR;
                                String errorMsg = finalErrorEntity.reason;
                                if (finalErrorEntity.errors != null && finalErrorEntity.errors.length > 0) {
                                    errorCode = finalErrorEntity.errors[0].errorCode;
                                    errorMsg = finalErrorEntity.errors[0].reason;
                                }
                                callback.onFail(req, errorCode, errorMsg);
                            });
                        }
                    }
                } else {
                    if (callback != null && (activity == null || !activity.isFinishing())) {
                        handler.post(() -> callback.onFail(null, HttpCode.NO_JSON, "数据格式不正确!"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null && (activity == null || !activity.isFinishing())) {
                    handler.post(() -> callback.onFail(null, HttpCode.NO_HOST, "访问服务器失败！"));
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                if (callback != null && (activity == null || !activity.isFinishing())) {
                    handler.post(() -> callback.onFail(req, HttpCode.NO_JSON, "数据解析格式异常！"));
                }
            }

            //运行结束
            if (callback != null && (activity == null || !activity.isFinishing())) {
                handler.post(() -> callback.onFinish(req));
            }
        }).start();

        return true;
    }

    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15000, TimeUnit.MILLISECONDS).readTimeout(15000, TimeUnit.MILLISECONDS)
            // .writeTimeout(15000, TimeUnit.MILLISECONDS)
            .cache(new Cache(BaseApplication.getContext().getCacheDir(), 10 * 1024 * 1024)).build();

    private String execute(String url, Map<String, String> headMap, String jsonString) throws IOException {
        BaseLogUtil.methodStart("发送Http请求");
        BaseLogUtil.methodStep("API = " + url);
        BaseLogUtil.methodStep("bodyParam = " + jsonString);

        RequestBody builder = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);

        StringBuilder stringBuilder = new StringBuilder();
        // stringBuilder.append(url).append("?").append(jsonString);
        Request.Builder request = new Request.Builder().url(url).post(builder);
        if (headMap != null && headMap.size() > 0) {
            for (String key : headMap.keySet()) {
                request.addHeader(key, headMap.get(key));
                stringBuilder.append("\n* --> ").append(key).append(" = ").append(headMap.get(key));
            }
            BaseLogUtil.methodStep("HEADERS" + stringBuilder.toString());
        }

        Response response = httpClient.newCall(request.build()).execute();
        ResponseBody body = response.body();
        String bodyString = "";
        if (body != null)
            bodyString = body.string().trim();
        BaseLogUtil.i("HttpManager", bodyString);
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
        BaseLogUtil.i("HttpManager", stringBuilder.toString());

        Response response = httpClient.newCall(request.build()).execute();
        String bodyString = response.body().string().trim();
        BaseLogUtil.i("HttpManager", bodyString);
        return bodyString;
    }

    public <V> boolean postFile(Activity activity, String url, Map<String, String> headMap, BaseRequest req, List<File> files, TypeToken<V> typeToken, HttpCallback<BaseRequest, V> callback) {
        if (!NetworkUtil.isConnected(activity)) {
            if (callback != null && (activity == null || !activity.isFinishing())) {
                handler.post(() -> callback.onFail(null, HttpCode.NO_NET, "请检查网络是否连接"));
            }
            return false;
        }

        if (callback != null && (activity == null || !activity.isFinishing())) {
            handler.post(() -> callback.onStart(req));
        }

        new Thread(() -> {
            try {
                String resJson = executeFile(url, headMap, req, files);

                if (resJson.startsWith("{") && resJson.endsWith("}") || resJson.startsWith("[") && resJson.endsWith("]")) {//是否为Json
                    if (onResultCallback != null) {
                        onResultCallback.onResult(resJson);
                    }

                    ErrorEntity errorEntity = null;
                    if (resJson.startsWith("{") && resJson.endsWith("}")) {
                        errorEntity = GsonUtil.fromJson(resJson, ErrorEntity.class);
                    }

                    if (errorEntity == null || (TextUtils.isEmpty(errorEntity.reason) &&
                            TextUtils.isEmpty(errorEntity.message) && errorEntity.errors == null)) {
                        V res = GsonUtil.fromJson(resJson, typeToken.getType());
                        if (onResultCallback != null) {
                            onResultCallback.onSuccess(res);
                        }
                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            handler.post(() -> callback.onResult(req, res));

                        }
                    } else {
                        if (onResultCallback != null) {
                            onResultCallback.onError(req, errorEntity);
                        }
                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            ErrorEntity finalErrorEntity = errorEntity;
                            handler.post(() -> {
                                String errorCode = HttpCode.OTHER_ERROR;
                                String errorMsg = finalErrorEntity.reason;
                                if (finalErrorEntity.errors != null && finalErrorEntity.errors.length > 0) {
                                    errorCode = finalErrorEntity.errors[0].errorCode;
                                    errorMsg = finalErrorEntity.errors[0].reason;
                                }
                                callback.onFail(req, errorCode, errorMsg);
                            });
                        }
                    }
                } else {
                    if (callback != null && (activity == null || !activity.isFinishing())) {
                        handler.post(() -> callback.onFail(null, HttpCode.NO_JSON, "数据格式不正确!"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null && (activity == null || !activity.isFinishing())) {
                    handler.post(() -> callback.onFail(null, HttpCode.NO_HOST, "访问服务器失败！"));
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                if (callback != null && (activity == null || !activity.isFinishing())) {
                    handler.post(() -> callback.onFail(req, HttpCode.NO_JSON, "数据解析格式异常！"));
                }
            }

            //运行结束
            if (callback != null && (activity == null || !activity.isFinishing())) {
                handler.post(() -> callback.onFinish(req));
            }
        }).start();

        return true;
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
        void onError(Object req, ErrorEntity json);

        /**
         * 执行成功回调
         */
        void onSuccess(Object json);
    }

    public boolean downloadFile(Activity activity, String req, File outFile, HttpFileCallBack
            callback) {
        if (!NetworkUtil.isConnected(activity)) {
            if (callback != null && (activity == null || !activity.isFinishing())) {
                handler.post(() -> callback.onFail(null, HttpCode.NO_NET, "请检查网络是否连接"));
            }
            return false;
        }

        if (callback != null && (activity == null || !activity.isFinishing())) {
            handler.post(() -> callback.onStart(req));
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            handler.post(() -> callback.onFail(null, HttpCode.FILE_NO_PRE, "文件创建失败！"));
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

                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            handler.post(() -> callback.onFail(null, HttpCode.NO_HOST, "访问服务器失败！"));
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
                                if (callback != null && (activity == null || !activity.isFinishing())) {
                                    handler.post(() -> callback.onProgress(down, total));
                                }
                            }

                            if (callback != null && (activity == null || !activity.isFinishing())) {
                                handler.post(() -> callback.onResult(req, outFile));
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            if (callback != null && (activity == null || !activity.isFinishing())) {
                                handler.post(() -> callback.onFail(req, HttpCode.FILE_SAVE_ERROR, "下载失败！"));
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
                        if (callback != null && (activity == null || !activity.isFinishing())) {
                            handler.post(() -> callback.onFinish(req));
                        }
                    }
                });
            }
        }).start();
        return true;
    }

    public interface HttpCode {
        String OK = "200";
        /**
         * 网络没有接通
         */
        String NO_NET = "-1";
        /**
         * JSON格式错误
         */
        String NO_JSON = "-2";
        /**
         * 找不到服务器或者接口
         */
        String NO_HOST = "404";
        /**
         * 文件创建无权限
         */
        String FILE_NO_PRE = "-3";
        /**
         * 文件保存失败
         */
        String FILE_SAVE_ERROR = "-4";
        /**
         * 其他错误
         */
        String OTHER_ERROR = "-5";
    }
}
