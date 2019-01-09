package cn.sxw.android.lib.mvp.model.request;

import android.app.Activity;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.reflect.TypeToken;

import cn.sxw.android.base.okhttp.BaseRequest;
import cn.sxw.android.base.okhttp.HttpCallback;

/**
 * Created by ZengCS on 2019/1/9.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */

public class TestRequest extends BaseRequest {
    private String key1;
    private String key2;
    @JSONField(serialize = false)
    private HttpCallback<TestRequest, String> httpCallback;

    public TestRequest(Activity activity, String api) {
        super(activity, api);
    }

    @Override
    public HttpCallback<TestRequest, String> getHttpCallback() {
        return httpCallback;
    }

    public TestRequest setHttpCallback(HttpCallback<TestRequest, String> httpCallback) {
        this.httpCallback = httpCallback;
        setTypeToken(new TypeToken<String>() {
        });
        return this;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }
}
