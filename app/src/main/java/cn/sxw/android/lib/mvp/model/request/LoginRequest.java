package cn.sxw.android.lib.mvp.model.request;

import android.app.Activity;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;

import cn.sxw.android.base.okhttp.BaseRequest;
import cn.sxw.android.base.okhttp.HttpCallback;
import cn.sxw.android.base.okhttp.response.LoginResponse;

/**
 * Created by ZengCS on 2019/1/9.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 * <p>
 * 返回结果是列表时使用
 */
public class LoginRequest extends BaseRequest {
    // 具体的参数，只适用于Body，当Method=GET时，无效
    private String account;// 登录帐号值，根据登录类型不同，取对应的值
    private int accountType = 1;// 登录帐号类型 0=手机 1=身份证 2=用户名 3=邮箱 4=qq 5=微信 6=微博,7=网阅临时账号，8=手机验证码
    private String app = "MDM";// App: SXT|XWCZ|XWKT|XWKW|MDM
    private String client = "STUDENT";// 客户端：STUDENT|TEACHER|PARENTS|MANAGER
    private String password;// 登录密码，请求时请将密码进行32位 MD5(password) 后传参;
    private String platform = "ANDROID";// 平台：IOS|ANDROID|PAD|PC|H5|WECHAT

    @JSONField(serialize = false)
    private HttpCallback<LoginRequest, LoginResponse> httpCallback;

    /**
     * @param activity
     * @param api      只传Api名称，适配全局的Scheme和Host，或者通过HttpUrlEncode.encode()组装完整路径
     */
    public LoginRequest(Activity activity, String api) {
        super(activity, api);
    }

    @Override
    public HttpCallback<LoginRequest, LoginResponse> getHttpCallback() {
        return httpCallback;
    }

    public LoginRequest setHttpCallback(HttpCallback<LoginRequest, LoginResponse> httpCallback) {
        this.httpCallback = httpCallback;
        // 不要忘记设置数据类型，用于JSON数据反序列化
        super.setTypeReference(new TypeReference<LoginResponse>() {
        });
        return this;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
