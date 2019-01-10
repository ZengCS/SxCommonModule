package cn.sxw.android.lib.mvp.model.request;

import android.app.Activity;

import com.alibaba.fastjson.annotation.JSONField;

import cn.sxw.android.base.bean.BlankBean;
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
    private HttpCallback<TestRequest, BlankBean> httpCallback;

    /**
     * @param activity
     * @param api      只传Api名称，适配全局的Scheme和Host，或者通过HttpUrlEncode.getUrlEncode()组装完整路径
     */
    public TestRequest(Activity activity, String api) {
        super(activity, api);
    }

    @Override
    public HttpCallback<TestRequest, BlankBean> getHttpCallback() {
        return httpCallback;
    }

    public TestRequest setHttpCallback(HttpCallback<TestRequest, BlankBean> httpCallback) {
        this.httpCallback = httpCallback;
        // 不要忘记设置数据类型
        super.setClz(BlankBean.class);

//        setTypeToken(new TypeToken<String>() {
//        });
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
