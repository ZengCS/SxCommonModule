package cn.sxw.android.lib.mvp.model.empty;

import java.util.List;

import cn.sxw.android.base.bean.BlankBean;
import cn.sxw.android.base.mvp.IModel;
import cn.sxw.android.lib.mvp.model.request.TestListRequest;
import cn.sxw.android.base.okhttp.response.LoginResponse;

public interface IEmptyModel extends IModel {
    /**
     * 缓存列表数据
     */
    void cacheListData(TestListRequest request, List<BlankBean> list);

    void updateToken(LoginResponse loginResponse);
}
