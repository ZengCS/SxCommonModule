package cn.sxw.android.lib.mvp.model.empty;

import java.util.List;

import javax.inject.Inject;

import cn.sxw.android.base.bean.BlankBean;
import cn.sxw.android.base.mvp.BaseModel;
import cn.sxw.android.base.net.ApiHelper;
import cn.sxw.android.base.prefer.PreferencesHelper;
import cn.sxw.android.lib.mvp.model.request.TestListRequest;

public class EmptyModelImp extends BaseModel implements IEmptyModel {

    private PreferencesHelper preferencesHelper;

    private ApiHelper apiHelper;
    private int requestTime = 0;

    @Inject
    public EmptyModelImp(PreferencesHelper preferencesHelper, ApiHelper apiHelper) {
        this.preferencesHelper = preferencesHelper;
        this.apiHelper = apiHelper;
    }

    @Override
    public void cacheListData(TestListRequest request, List<BlankBean> list) {
        // TODO 根据自己的业务处理
    }
}
