package com.company.iendo.mineui.activity.casemanage.fragment;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.iendo.R;
import com.company.iendo.action.StatusAction;
import com.company.iendo.app.TitleBarFragment;
import com.company.iendo.bean.DetailPictureBean;
import com.company.iendo.mineui.activity.MainActivity;
import com.company.iendo.mineui.activity.casemanage.fragment.adapter.PictureAdapter;
import com.company.iendo.other.HttpConstant;
import com.company.iendo.utils.LogUtils;
import com.company.iendo.utils.SharePreferenceUtil;
import com.company.iendo.widget.MyItemDecoration;
import com.company.iendo.widget.StatusLayout;
import com.hjq.base.BaseAdapter;
import com.hjq.widget.layout.WrapRecyclerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2021/10/29 13:55
 * desc：第2个tab-fragment
 */
public class PictureFragment extends TitleBarFragment<MainActivity> implements StatusAction, BaseAdapter.OnItemClickListener, OnRefreshLoadMoreListener {
    private SmartRefreshLayout mRefreshLayout;
    private WrapRecyclerView mRecyclerView;
    private StatusLayout mStatusLayout;
    private List<DetailPictureBean.DataDTO> mDataLest = new ArrayList<>();
    private PictureAdapter mAdapter;
    private String mBaseUrl;

    public static PictureFragment newInstance() {
        return new PictureFragment();
    }

    @Override

    protected int getLayoutId() {
        return R.layout.fragment_detail_picture;
    }

    @Override
    protected void initView() {
        mRefreshLayout = findViewById(R.id.rl_pic_refresh);
        mRecyclerView = findViewById(R.id.rv_pic_list);
        mStatusLayout = findViewById(R.id.pic_hint);
        mBaseUrl = (String) SharePreferenceUtil.get(getActivity(), SharePreferenceUtil.Current_BaseUrl, "111");
        mAdapter = new PictureAdapter(getActivity(), MainActivity.getCurrentItemID());

        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new MyItemDecoration(getActivity(), 1, R.drawable.shape_divideritem_decoration));
        mAdapter.setData(mDataLest);
        sendRequest(MainActivity.getCurrentItemID());


    }

    /**
     * 获取当前用户的图片
     *
     * @param currentItemID
     */
    private void sendRequest(String currentItemID) {
        LogUtils.e("currentItemID" + currentItemID);

        OkHttpUtils.get()
                .url(mBaseUrl+HttpConstant.CaseManager_CasePictures)
                .addParams("ID", currentItemID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showError(listener -> {
                            sendRequest(currentItemID);
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if ("" != response) {
                            DetailPictureBean mBean = mGson.fromJson(response, DetailPictureBean.class);
                            List<DetailPictureBean.DataDTO> data = mBean.getData();
                            toast(mBean.getMsg());
                            LogUtils.e("图片"+"response==="+response);////原图路径

                            if (0 == mBean.getCode()) {  //成功
                                showComplete();


                                if (mBean.getData().size()!=0){
                                    mDataLest.clear();
                                    mDataLest.addAll(mBean.getData());
                                    LogUtils.e("图片"+"");////原图路径
                                    mAdapter.setData(mDataLest);
                                }else{
                                    showEmpty();
                                }

                            } else {
                                showError(listener -> {
                                    sendRequest(currentItemID);
                                });
                            }
                        } else {
                            showError(listener -> {
                                sendRequest(currentItemID);
                            });
                        }
                    }
                });

    }

    @Override
    protected void initData() {

    }


    @Override
    public void onItemClick(RecyclerView recyclerView, View itemView, int position) {
        DetailPictureBean.DataDTO item = mAdapter.getItem(position);
        toast(item.getID());
    }

    /**
     * {@link OnRefreshLoadMoreListener}
     */

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        postDelayed(() -> {
            mAdapter.clearData();
            mAdapter.setData(mDataLest);
            mRefreshLayout.finishRefresh();
        }, 1000);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        postDelayed(() -> {
            mRefreshLayout.finishLoadMore();
            mAdapter.setLastPage(true);
            mRefreshLayout.setNoMoreData(mAdapter.isLastPage());
        }, 1000);
    }


    @Override
    public boolean isStatusBarEnabled() {
        // 使用沉浸式状态栏
        return !super.isStatusBarEnabled();
    }

    @Override
    public StatusLayout getStatusLayout() {
        return mStatusLayout;
    }


}
