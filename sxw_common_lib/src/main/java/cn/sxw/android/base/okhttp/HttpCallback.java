package cn.sxw.android.base.okhttp;

import java.util.List;

/**
 * @author zcs@sxw.cn
 * @version v1.0
 * @date 2018/08/25 14:42
 */
public interface HttpCallback<T, V> {
    void onStart(T req);

    void onResultWithObj(T req, V result);

    void onResultWithList(T req, List<V> result);

    void onFail(T req, String code, String msg);

    void onFinish(T req);
}
