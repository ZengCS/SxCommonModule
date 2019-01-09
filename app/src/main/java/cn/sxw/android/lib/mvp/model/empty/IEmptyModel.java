package cn.sxw.android.lib.mvp.model.empty;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import cn.sxw.android.base.mvp.IModel;
import cn.sxw.android.base.bean.response.BlankResponse;

public interface IEmptyModel extends IModel {
    void getBlankData(Context context, JSONObject jsonObject, DataCallbackToUi<BlankResponse> callback);
}
