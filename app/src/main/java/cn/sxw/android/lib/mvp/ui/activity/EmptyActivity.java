package cn.sxw.android.lib.mvp.ui.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.Random;

import cn.sxw.android.base.adapter.CommonRecyclerAdapter;
import cn.sxw.android.base.bean.BlankBean;
import cn.sxw.android.base.di.component.AppComponent;
import cn.sxw.android.base.imageloader.ImageLoader;
import cn.sxw.android.base.okhttp.HttpCallback;
import cn.sxw.android.base.provider.PicProvider;
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

    @Click({R.id.id_iv_refresh})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_iv_refresh:
                showToast("刷新数据!");
                onRefresh();
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

//    private void initData() {
//        // 初始化列表
//        initAdapter();
//        // 初始化数据
//        onRefresh();
//    }

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
                    helper.setGone(R.id.id_view_new, !item.isNewest());
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

        getDataByOkhttp();
    }

    private void getDataByOkhttp() {
//        OkGo.<String>get("http://swx.cn")
//                .tag(this)
//                .headers("", "")
//                .params("key", "val")
//                .execute(new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        //注意这里已经是在主线程了
//                        String data = response.body();//这个就是返回来的结果
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });

        // 这里注意，只需要输入API名称，无需输入HOST
        TestRequest request = new TestRequest(this, "v2/api/test/login");
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

        request.setHttpCallback(new HttpCallback<TestRequest, String>() {
            @Override
            public void onStart(TestRequest req) {
                showLoading();
            }

            @Override
            public void onResult(TestRequest req, String result) {

            }

            @Override
            public void onFail(TestRequest req, String code, String msg) {
                showToast("[errorCode = " + code + "]" + msg);
            }

            @Override
            public void onFinish(TestRequest req) {
                hideLoading();
            }
        }).post();

//        TestReq req = new TestReq();
//        req.param1 = "v1";
//        req.param2 = "v2";
//        HttpManager.getInstance().testRequest(this, req, new HttpCallback<TestReq, String>() {
//            @Override
//            public void onStart(TestReq req) {
//                showLoading();
//            }
//
//            @Override
//            public void onResult(TestReq req, String result) {
//                showToast("数据加载成功");
//            }
//
//            @Override
//            public void onFail(TestReq req, String code, String msg) {
//                showToast("[" + code + "]" + msg);
//            }
//
//            @Override
//            public void onFinish(TestReq req) {
//                hideLoading();
//            }
//        });
    }

    /**
     * 杀死自己
     */
    @Override
    public void killMyself() {
        mApplication.getAppComponent().appManager().killActivity(EmptyActivity_.class);
    }

    @Override
    public void onRequestSuccess(String data) {
        hideLoading();
        mAdapter.setEnableLoadMore(true);
        if (TextUtils.isEmpty(data)) {
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
        Random random = new Random();
        for (int i = 0; i < PAGE_SIZE; i++) {
            int id = i + (currPage - 1) * PAGE_SIZE + 1;
            BlankBean bean = new BlankBean(id);
            bean.setPic(PicProvider.getPicture());
            bean.setName("Name " + id);
            bean.setDesc("Description " + id);
            bean.setNewest(random.nextBoolean());
            mItems.add(bean);
        }
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
