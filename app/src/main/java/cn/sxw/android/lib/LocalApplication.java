package cn.sxw.android.lib;

import java.util.UUID;

import cn.sxw.android.base.okhttp.HttpManager;
import cn.sxw.android.base.ui.BaseApplication;

/**
 * Created by ZengCS on 2019/1/8.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */

public class LocalApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化HttpManager
        // For test http://www.mocky.io/v2/5c35b8e63000009f0021b4a3
        String host = "api2.test.sxw.cn";
        host = "www.mocky.io";

        HttpManager.getInstance().setTokenHeader(UUID.randomUUID().toString())
                .setScheme("http")// 默认是http，如果使用https时，必须设置
                .setHost(host);// 这里不要写 http://

    }
}
