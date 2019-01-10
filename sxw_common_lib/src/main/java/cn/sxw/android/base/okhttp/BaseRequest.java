package cn.sxw.android.base.okhttp;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;

/**
 * Created by ZengCS on 2019/1/9.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */

public abstract class BaseRequest {
    @JSONField(serialize = false)
    private String api = "";// api路径,不带host
    @JSONField(serialize = false)
    private HashMap<String, String> paramMap;// query 参数列表
    @JSONField(serialize = false)
    private HashMap<String, String> headMap;// Http Header 参数列表
    @JSONField(serialize = false)
    private Activity activity;// 1.提供Context 2.验证是否需要回调
    @JSONField(serialize = false)
    private Class clz;// 用于FastJson反序列化

    protected abstract <T, V> HttpCallback<T, V> getHttpCallback();

    public BaseRequest(Activity activity, String api) {
        this.activity = activity;
        this.api = api;
    }

    public String getApi() {
        try {
            if (!api.startsWith("http:") && !api.startsWith("https:")) {
                return HttpUrlEncode.getUrlEncode(
                        HttpManager.getInstance().getScheme(),
                        HttpManager.getInstance().getHost(),
                        api, paramMap);
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
