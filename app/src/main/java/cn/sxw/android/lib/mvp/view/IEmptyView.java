package cn.sxw.android.lib.mvp.view;

import cn.sxw.android.base.mvp.IViewAdvance;

/**
 * Created by ZengCS on 2017/9/6.
 * E-mail:zcs@sxw.cn
 * Add:成都市天府软件园E3-3F
 */
public interface IEmptyView extends IViewAdvance {
    void onRequestSuccess(String data);

    void onFailed(String msg);
}
