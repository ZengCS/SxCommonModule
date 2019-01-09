package cn.sxw.android.base.okhttp;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import okhttp3.HttpUrl;

/**
 * Created by ZengCS on 2019/1/9.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */

public abstract class BaseRequest {
    @JSONField(serialize = false)
    private String api = "";
    @JSONField(serialize = false)
    private HashMap<String, String> paramMap;
    @JSONField(serialize = false)
    private HashMap<String, String> headMap;
    @JSONField(serialize = false)
    private Activity activity;
    @JSONField(serialize = false)
    private TypeToken typeToken;
    @JSONField(serialize = false)
    private Class clz;

    protected abstract <T, V> HttpCallback<T, V> getHttpCallback();

    public BaseRequest(Activity activity, String api) {
        this.activity = activity;
        this.api = api;
    }

    public String getApi() {
        try {
            if (paramMap != null) {// 组装url完整地址
                HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                        .scheme(HttpManager.getInstance().getScheme())
                        .host(HttpManager.getInstance().getHost())
                        .addPathSegments(api);
                for (String key : paramMap.keySet()) {
                    urlBuilder.addQueryParameter(key, paramMap.get(key));
                }
                return urlBuilder.build().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public HashMap<String, String> getParamMap() {
        if (paramMap == null)
            paramMap = new HashMap<>();
        return paramMap;
    }

    public void setParamMap(HashMap<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public HashMap<String, String> getHeadMap() {
        if (headMap == null)
            headMap = new HashMap<>();
        return headMap;
    }

    public void setHeadMap(HashMap<String, String> headMap) {
        this.headMap = headMap;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public TypeToken getTypeToken() {
        return typeToken;
    }

    public void setTypeToken(TypeToken typeToken) {
        this.typeToken = typeToken;
    }

    public Class getClz() {
        return clz;
    }

    public void setClz(Class clz) {
        this.clz = clz;
    }

    public void postData() {
        HttpManager.getInstance().sendPost(this);
    }

    public void getData() {
        HttpManager.getInstance().sendGet(this);
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}
