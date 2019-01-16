package cn.sxw.android.lib.mvp.model.empty;

import com.alibaba.fastjson.JSON;

import java.util.List;

import javax.inject.Inject;

import cn.sxw.android.base.bean.BlankBean;
import cn.sxw.android.base.cache.SharedPreferencesUtil;
import cn.sxw.android.base.mvp.BaseModel;
import cn.sxw.android.base.net.ApiHelper;
import cn.sxw.android.base.okhttp.HttpManager;
import cn.sxw.android.lib.mvp.model.request.TestListRequest;
import cn.sxw.android.lib.mvp.model.response.LoginResponse;

public class EmptyModelImp extends BaseModel implements IEmptyModel {

    private ApiHelper apiHelper;

    @Inject
    public EmptyModelImp(ApiHelper apiHelper) {
        this.apiHelper = apiHelper;
    }

    @Override
    public void updateToken(LoginResponse loginResponse) {
        SharedPreferencesUtil.setParam("KEY_LOGIN_RESPONSE_V1", JSON.toJSONString(loginResponse));

        HttpManager.getInstance().setTokenHeader(loginResponse.getToken());
        HttpManager.getInstance().setRefreshToken(loginResponse.getRefreshToken());
    }

    @Override
    public void cacheListData(TestListRequest request, List<BlankBean> list) {
        // TODO 根据自己的业务处理
    }
}
