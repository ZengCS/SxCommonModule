package cn.sxw.android.lib.mvp.presenter;

import android.app.Application;

import com.alibaba.fastjson.JSONObject;

import javax.inject.Inject;

import cn.sxw.android.base.di.scope.PerActivity;
import cn.sxw.android.base.integration.AppManager;
import cn.sxw.android.base.mvp.BasePresenter;
import cn.sxw.android.base.mvp.IModel;
import cn.sxw.android.lib.R;
import cn.sxw.android.lib.mvp.model.empty.IEmptyModel;
import cn.sxw.android.base.bean.response.BlankResponse;
import cn.sxw.android.lib.mvp.view.IEmptyView;

@PerActivity
public class EmptyPresenter extends BasePresenter<IEmptyModel, IEmptyView> {
    private AppManager mAppManager;
    private Application mApplication;

    @Inject
    public EmptyPresenter(IEmptyModel model, IEmptyView view, AppManager appManager, Application application) {
        super(model, view);
        this.mApplication = application;
        this.mAppManager = appManager;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    public void getBlankData(int currPage) {
        if (mRootView != null) mRootView.showLoading();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", "val1");
        jsonObject.put("key2", "val2");
        jsonObject.put("pageNo", currPage);
        jsonObject.put("pageSize", PAGE_SIZE);

        mModel.getBlankData(mApplication, jsonObject, new IModel.DataCallbackToUi<BlankResponse>() {
            @Override
            public void onSuccess(BlankResponse response) {
                if (response.isRequestSuccess()) {
                    if (mRootView == null) return;
                    mRootView.onRequestSuccess(response.getData());
                } else {
                    onFail(mApplication.getString(R.string.txt_load_data_error));
                }
            }

            @Override
            public void onFail(String msg) {
                if (mRootView == null) return;
                mRootView.onFailed(msg);
            }
        });
    }
}
