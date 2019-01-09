package cn.sxw.android.base.okhttp;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpManager
 *
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/12/1 21:02
 */
public class HttpManager {
    private static HttpManager sHttpManager;
    private BaseHttpManagerAdv mHttp;
    private Map<String, String> headMap = new HashMap<>();

    public static HttpManager getInstance() {
        if (sHttpManager == null) {
            synchronized (HttpManager.class) {
                if (sHttpManager == null) {
                    sHttpManager = new HttpManager();
                }
            }
        }
        return sHttpManager;
    }

    public void setHeader(String token, String tokenSsl) {
        headMap.clear();
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(tokenSsl)) {
            headMap.put("TOKEN", token);
            headMap.put("x-kc-ic-authtoken-ssl", tokenSsl);
        }
    }

    private HttpManager() {
        mHttp = BaseHttpManagerAdv.instance();
        mHttp.setOnResultCallback(new BaseHttpManagerAdv.OnResultCallback() {
            @Override
            public void onResult(String json) {
                //TODO 正确的JSON
            }

            @Override
            public void onError(Object req, ErrorEntity error) {
                if (error.errors != null && error.errors.length > 0) {
                    // TODO 处理错误逻辑
                }
            }

            @Override
            public void onSuccess(Object json) {
                //TODO 成功业务处理
            }
        });
    }

    public BaseHttpManagerAdv getHttpManager() {
        return mHttp;
    }

    public void getData(Context context, BaseRequest request) {

    }

    public void postData(BaseRequest request){
        mHttp.postNew(request);
    }

//    public void testRequest(Activity activity, TestReq req, HttpCallback<TestReq, String> callBack) {
//        mHttp.post(activity, TestReq._URL, headMap, req, new TypeToken<String>() {
//        }, callBack);
//    }

}
