package cn.sxw.android.lib.mvp.model.empty;

import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

import javax.inject.Inject;

import cn.sxw.android.base.bean.response.BlankResponse;
import cn.sxw.android.base.mvp.BaseModel;
import cn.sxw.android.base.net.ApiHelper;
import cn.sxw.android.base.prefer.PreferencesHelper;
import cn.sxw.android.base.utils.JDateKit;

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
    public void getBlankData(Context context, JSONObject jsonObject, final DataCallbackToUi<BlankResponse> callback) {
        // TODO 加载数据
        // apiHelper.postData(xxx, xxx, xxx);

        // 这里模拟访问
        new Handler().postDelayed(() -> {
            BlankResponse response = new BlankResponse();
            if (requestTime == 0) {
                response.setCode(500);
                response.setData("");
                response.setSuccess(false);
                callback.onFail("数据加载失败");
            } else if (requestTime == 1 || requestTime == 3) {
                response.setCode(200);
                response.setData("");
                response.setSuccess(true);
                callback.onSuccess(response);
            } else {
                response.setCode(200);
                response.setData("数据加载成功，现在是北京时间：" + JDateKit.dateToStr("yyyy-MM-dd HH:mm:ss", new Date()));
                response.setSuccess(true);
                callback.onSuccess(response);
            }
            requestTime++;
        }, 1000);
    }
}
