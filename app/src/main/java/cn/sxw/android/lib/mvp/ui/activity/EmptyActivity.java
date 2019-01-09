package cn.sxw.android.lib.mvp.ui.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import cn.sxw.android.base.adapter.CommonRecyclerAdapter;
import cn.sxw.android.base.bean.BlankBean;
import cn.sxw.android.base.di.component.AppComponent;
import cn.sxw.android.base.imageloader.ImageLoader;
import cn.sxw.android.base.okhttp.HttpCallback;
import cn.sxw.android.base.utils.JListKit;
import cn.sxw.android.lib.R;
import cn.sxw.android.lib.di.component.DaggerEmptyComponent;
import cn.sxw.android.lib.di.module.EmptyModule;
import cn.sxw.android.lib.mvp.base.BaseActivityAdv;
import cn.sxw.android.lib.mvp.model.request.TestRequest;
import cn.sxw.android.lib.mvp.presenter.EmptyPresenter;
import cn.sxw.android.lib.mvp.view.IEmptyView;

@EActivity(R.layout.activity_empty)
public class EmptyActivity extends BaseActivityAdv<EmptyPresenter> implements IEmptyView {
    private ImageLoader mImageLoader;// 用于加载图片的管理类,默认使用glide
    private List<BlankBean> mItems = JListKit.newArrayList();
    private CommonRecyclerAdapter<BlankBean> mAdapter;

    // Views
    @ViewById(R.id.id_rv_empty)
    RecyclerView mRecyclerView;
    @ViewById(R.id.id_tv_response_info)
    TextView tvResponse;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {
        DaggerEmptyComponent
                .builder()
                .appComponent(appComponent)
                .emptyModule(new EmptyModule(this))
                .build()
                .inject(this);
        mImageLoader = mApplication.getAppComponent().imageLoader();
    }

    @Click({R.id.id_btn_get_bean,
            R.id.id_btn_failed,
            R.id.id_btn_error_json,
            R.id.id_btn_not_found
    })
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_get_bean:
                getDataByOkhttp("v2/5c35b8e63000009f0021b4a3");
                break;
            case R.id.id_btn_failed:
                getDataByOkhttp("v2/5c35c3b9300000780021b4e9");
                break;
            case R.id.id_btn_error_json:
                getDataByOkhttp("v2/5c35c41a3000007f0021b4ec");
                break;
            case R.id.id_btn_not_found:
                getDataByOkhttp("api2.test.sxw.cn");
                break;
        }
    }

    @AfterViews
    void init() {
        // 添加下拉刷新能力
        super.addSwipeRefreshAbility();
        // 初始化列表
        initAdapter();
        // 初始化数据
        onRefresh();
    }

    private void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new CommonRecyclerAdapter<BlankBean>(R.layout.item_blank, mItems) {
                @Override
                protected void convert(BaseViewHolder helper, BlankBean item) {
                    // 绑定数据
                    helper.setText(R.id.id_tv_blank_name, item.getName());
                    helper.setText(R.id.id_tv_blank_desc, item.getDesc());
                    // 加载网络图片
                    mImageLoader.displayImage(item.getPic(), 0, helper.getView(R.id.id_iv_blank_pic));
                    // Glide.with(mContext).load(item.getPic()).crossFade().into((ImageView) helper.getView(R.id.id_iv_blank_pic));
                    // 绑定子View点击事件
                    helper.getView(R.id.id_btn_inner).setOnClickListener(v -> {
                        // TODO 编写具体代码
                        int position = helper.getAdapterPosition();
                        showToast("我是元素内的按钮，position = " + position);
                    });
                    // 设置可见性 true for VISIBLE, false for GONE.
                    helper.setGone(R.id.id_view_new, item.isNewest());
                }
            };
            // mAdapter.addHeaderView();
            // mAdapter.addFooterView();
            // 点击事件
            mAdapter.setOnItemClickListener((adapter, view, position) -> {
                // TODO 编写具体代码
                showToast("Click Root position = " + position);
            });

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAdapter);
            // 设置加载更多能力
            mAdapter.setEnableLoadMore(true);
            mAdapter.setOnLoadMoreListener(() -> {
                currPage++;
                getDataFromNet();
            }, mRecyclerView);
            // 默认第一次加载会进入回调，如果不需要可以配置：
            mAdapter.disableLoadMoreIfNotFullPage();
        }
    }

    @Override
    protected void getDataFromNet() {
        mPresenter.getBlankData(currPage);

        getDataByOkhttp("v2/5c35bf0d3000005f0021b4d8");// 返回列表
    }

    private void getDataByOkhttp(String api) {
        // 这里注意，只需要输入API名称，无需输入HOST
        // TestRequest request = new TestRequest(this, "v2/5c35b8e63000009f0021b4a3");
        TestRequest request = new TestRequest(this, api);
        // 这些参数会被转成JSON
        request.setKey1("val1");
        request.setKey2("val2");
        // 这些参数会拼接到url
        request.getParamMap().put("p1", "1");
        request.getParamMap().put("p2", "test");
        request.getParamMap().put("p3", "true");
        // 这些参数会放到Header里
        request.getHeadMap().put("header1", "1234567890");
        request.getHeadMap().put("header2", "9874563210");

        request.setHttpCallback(new HttpCallback<TestRequest, BlankBean>() {
            @Override
            public void onStart(TestRequest req) {
                showLoading();
            }

            @Override
            public void onResultWithObj(TestRequest req, BlankBean bean) {
                // TODO 如果返回值是对象，这这里处理
                showToast("数据加载成功，" + JSON.toJSONString(bean));
                tvResponse.setText("数据加载成功，JSON\n" + JSON.toJSONString(bean));
            }

            @Override
            public void onResultWithList(TestRequest req, List<BlankBean> list) {
                // TODO 如果返回值是列表，在这里处理
                showToast("加载数据列表成功，共:" + list.size() + "条数据");
                onRequestSuccess(list);
            }

            @Override
            public void onFail(TestRequest req, String code, String msg) {
                showToast("[ErrorCode = " + code + "]" + msg);
                tvResponse.setText("[ErrorCode = " + code + "]" + msg);
            }

            @Override
            public void onFinish(TestRequest req) {
                hideLoading();
            }
        }).getData();
    }

    /**
     * 杀死自己
     */
    @Override
    public void killMyself() {
        mApplication.getAppComponent().appManager().killActivity(EmptyActivity_.class);
    }

    @Override
    public void onRequestSuccess(List<BlankBean> list) {
        hideLoading();
        mAdapter.setEnableLoadMore(true);
        if (list == null || list.size() == 0) {
            if (mItems == null || mItems.size() == 0)
                mAdapter.setEmptyView(notDataView);
            else {
                if (currPage > 1)
                    currPage--;
                mAdapter.loadMoreFail();
            }
            return;
        }
        showToast("数据加载成功~");

        if (currPage == 1)
            mItems.clear();
        mItems.addAll(list);
        // 模拟只加载3页数据
        hasMoreData = currPage < 3;

        if (currPage == 1) {
            mAdapter.setNewData(mItems);
        } else {
            if (hasMoreData) {
                mAdapter.loadMoreComplete();
            } else {
                mAdapter.loadMoreEnd();
            }
        }
    }

    @Override
    public void onFailed(String msg) {
        hideLoading();
        mAdapter.setEnableLoadMore(true);
        if (TextUtils.isEmpty(msg)) {
            msg = getString(R.string.txt_load_data_error);
        }
        showToast(msg);
        mAdapter.setEmptyView(errorView);
    }

    @Override
    public void onRefresh() {
        mAdapter.setEnableLoadMore(false);//这里的作用是防止下拉刷新的时候还可以上拉加载
        if (mItems == null || mItems.size() == 0)
            mAdapter.setEmptyView(mLoadingView);
        currPage = 1;
        getDataFromNet();
    }
}
