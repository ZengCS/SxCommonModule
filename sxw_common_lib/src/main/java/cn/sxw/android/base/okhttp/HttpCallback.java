package cn.sxw.android.base.okhttp;

import java.util.List;

/**
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/08/25 14:42
 */
public interface HttpCallback<T, V> {
    void onStart(T req);

    /**
     * @param req
     * @param result 为了兼容单个对象和列表数据，这里统一返回列表，如果只有单个对象，可以通过result.get(0)得到
     */
    void onResult(T req, List<V> result);

    void onFail(T req, String code, String msg);

    void onFinish(T req);
}
