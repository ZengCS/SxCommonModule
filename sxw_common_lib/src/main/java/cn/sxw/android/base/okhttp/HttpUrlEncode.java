package cn.sxw.android.base.okhttp;

import android.text.TextUtils;

import java.util.HashMap;

import okhttp3.HttpUrl;

/**
 * Created by ZengCS on 2019/1/10.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */
public class HttpUrlEncode {
    /**
     * @param scheme   must be http or https
     * @param host     sxw.cn
     * @param api      a/b/c/d/e
     * @param paramMap 请求参数列表
     * @return
     */
    public static String getUrlEncode(String scheme, String host, String api, HashMap<String, String> paramMap) {
        if (!TextUtils.isEmpty(api) && api.startsWith("/"))
            api = api.substring(1, api.length());

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .addPathSegments(api);
        if (paramMap != null && paramMap.size() > 0) {// 组装url完整地址
            for (String key : paramMap.keySet()) {
                urlBuilder.addQueryParameter(key, paramMap.get(key));
            }
        }
        return urlBuilder.build().toString();
    }
}
