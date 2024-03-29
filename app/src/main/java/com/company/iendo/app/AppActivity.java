package com.company.iendo.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.company.iendo.bean.RefreshEvent;
import com.company.iendo.bean.event.SocketRefreshEvent;
import com.company.iendo.bean.model.CaseModelBean;
import com.company.iendo.bean.model.ModelBean;
import com.company.iendo.other.Constants;
import com.company.iendo.other.HttpConstant;
import com.company.iendo.service.HandService;
import com.company.iendo.utils.SharePreferenceUtil;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.bar.TitleBar;
import com.hjq.base.BaseActivity;
import com.hjq.base.BaseDialog;
import com.company.iendo.R;
import com.company.iendo.action.TitleBarAction;
import com.company.iendo.action.ToastAction;
import com.company.iendo.http.model.HttpData;
import com.company.iendo.ui.dialog.WaitDialog;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.http.listener.OnHttpListener;
import com.tencent.mmkv.MMKV;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : Activity 业务基类
 */
public abstract class AppActivity extends BaseActivity
        implements ToastAction, TitleBarAction, OnHttpListener<Object> {

    /**
     * 标题栏对象
     */
    private TitleBar mTitleBar;
    /**
     * 状态栏沉浸
     */
    private ImmersionBar mImmersionBar;

    /**
     * 加载对话框
     */
    private BaseDialog mDialog;
    /**
     * 对话框数量
     */
    private int mDialogCount;
    public Gson mGson;
    public String mBaseUrl;  //当前用户的头部url
    public String endoType;
    public static String mAppIP;
    public String mCurrentTypeDes;    //当前选择设备的==比如:一代一体机==07,此处mCurrentTypeDes==一代一体机
    public int mCurrentTypeNum;    //当前选择设备的==比如:一代一体机==07,此处mCurrentTypeNum==07
    public String mCurrentTypeMsg;     //当前选择设备的==的描述比如:1号内镜室
    public String mCurrentReceiveDeviceCode; //当前选择设备的==唯一设备码
    public String mSocketOrLiveIP;       //socket或者直播通讯的ip
    public String mSocketPort;           //socket通讯端口
    public String mUsername;            //直播账号
    public String mPassword;            //直播密码
    public String mLivePort;            //直播端口
    public String mUserID;              //用户ID
    public String mBaseUrlPort;
    public String mLoginUserName;
    public MMKV mMMKVInstace;
    //病例模板使用到的数据
    public static ArrayList<String> mTitleList;
    public static LinkedHashMap mBeanHashMap;
    public static  LinkedHashMap<String, ArrayList<String>> mStringHashMap;
    public static String[][] items;

    /**
     * 当前加载对话框是否在显示中
     */
    public boolean isShowDialog() {
        return mDialog != null && mDialog.isShowing();
    }

    /**
     * 显示加载对话框
     */
    public void showDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        mDialogCount++;
        postDelayed(() -> {
            if (mDialogCount <= 0 || isFinishing() || isDestroyed()) {
                return;
            }

            if (mDialog == null) {
                mDialog = new WaitDialog.Builder(this)
                        .setCancelable(false)
                        .create();
            }
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        }, 300);
    }

    /**
     * 隐藏加载对话框
     */
    public void hideDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (mDialogCount > 0) {
            mDialogCount--;
        }

        if (mDialogCount != 0 || mDialog == null || !mDialog.isShowing()) {
            return;
        }

        mDialog.dismiss();
    }

    /**
     * 解决:its super classes have no public methods with the @Subscribe annotation的BUG
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true )
    public void SocketRefreshEvent(SocketRefreshEvent event) {

    }





    //下载队列是否正在下载的标识
    private boolean isQueueDownOver = true;

    /**
     * eventbus 刷新数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        isQueueDownOver = true;
    }






    /**
     * 将获取到的int型ip转成string类型
     */

    @Override
    protected void initLayout() {
        super.initLayout();
        if (getTitleBar() != null) {
            getTitleBar().setOnTitleBarListener(this);
        }
        mMMKVInstace = MMKV.defaultMMKV();

        mBaseUrl = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_BaseUrl, "192.167.132.102");
        mBaseUrlPort = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_HttpPort, "7001");
        endoType = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_EndoType, "3");
        mUserID = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_Login_UserID, "3");
        mLoginUserName = mMMKVInstace.decodeString(Constants.KEY_CurrentLoginUserName);
        mCurrentTypeDes = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_Type, "妇科治疗台");
        mCurrentTypeMsg = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_Type_Msg, "1号内镜室");
        mCurrentTypeNum = (int) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_Type_Num, 0x07);
        mCurrentReceiveDeviceCode = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_DeviceCode, "00000000000000000000000000000000");
        mSocketOrLiveIP = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_IP, "192.168.132.102");
        mSocketPort = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_SocketPort, "7006");
        mUsername = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_DeviceUsername, "root");
        mPassword = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_DevicePassword, "root");
        mLivePort = (String) SharePreferenceUtil.get(AppActivity.this, SharePreferenceUtil.Current_LivePort, "7788");


        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init();

            // 设置标题栏沉浸
            if (getTitleBar() != null) {
                ImmersionBar.setTitleBar(this, getTitleBar());
            }
        }
        mGson = GsonFactory.getSingletonGson();
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            mAppIP = getIpString(wifiInfo.getIpAddress());
        }
    }

    /**
     * 获取病例模板数据
     */
    public void sendRequest2getModelDialogData() {
        OkHttpUtils.get()
                .addParams("EndoType", endoType)
                .url(mBaseUrl + HttpConstant.UserManager_caseTemplate)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("TAG", "病例模板==onError==" + e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (!"".equals(response)) {
                            CaseModelBean modelBean = mGson.fromJson(response, CaseModelBean.class);
                            Log.e("TAG", "病例模板==modelBean==" + modelBean.toString());
//                            getDialogBeanList(modelBean);
                            getDialogBeanList02(modelBean);


                        }

                    }
                });
    }

    private void getDialogBeanList02(CaseModelBean modelBean) {

        //LinkedHashMap 确保存入和遍历的顺序一致性
        //recycleView 展示的map
        mStringHashMap = new LinkedHashMap<>();
        //点击recycleView 获取到String,之后需要查询数据bean的map
        mBeanHashMap = new LinkedHashMap<>();
        //分类标题的list
        mTitleList = new ArrayList<String>();

        List<CaseModelBean.DataDTO> data = modelBean.getData();
        if (data.size() != 0) {
//                                for (int i = data.size() - 1; i >= 0; i--) {
            for (int i = 0; i < data.size(); i++) {
                CaseModelBean.DataDTO bean = data.get(i);
                //获取父节点
                if (bean.getIParentId().equals("0")) {
                    String fatherKeyName = bean.getSzName();
                    String titleId = bean.getID();
                    ModelBean sonBean = new ModelBean();
                    sonBean.setSzName(fatherKeyName);
                    ArrayList<ModelBean> dataList = new ArrayList<>();
                    ArrayList<String> dataStringList = new ArrayList<>();
                    dataStringList.add(fatherKeyName);
                    mTitleList.add(fatherKeyName+"");
                    for (int i1 = 0; i1 < data.size(); i1++) {
                        CaseModelBean.DataDTO dataDTO = data.get(i1);
                        if (titleId.equals(dataDTO.getIParentId())) {
                            ModelBean mBean = new ModelBean();
                            mBean.setiD(dataDTO.getID() + "");
                            mBean.setiParentId(dataDTO.getIParentId() + "");
                            mBean.setSzName(dataDTO.getSzName() + "");
                            mBean.setSzEndoDesc(dataDTO.getSzEndoDesc() + "");
                            mBean.setSzResult(dataDTO.getSzResult() + "");
                            mBean.setSzTherapy(dataDTO.getSzTherapy() + "");
                            mBean.setEndoType(dataDTO.getEndoType() + "");
                            dataStringList.add(dataDTO.getSzName() + "");
                            dataList.add(mBean);
                        }
                    }
                    mStringHashMap.put(fatherKeyName, dataStringList);
                    mBeanHashMap.put(fatherKeyName, dataList);
                }
            }
        }

        int size1 = mStringHashMap.keySet().size();

        items = new String[size1][];
        ArrayList<String[]> mList = new ArrayList<>();
        for (String key : mStringHashMap.keySet()) {
            ArrayList<String> StringList = mStringHashMap.get(key);
            int size = StringList.size();
            String[] strings = new String[size];

            for (int i = 0; i < StringList.size(); i++) {
                String s = StringList.get(i);
                strings[i] = s;

            }
            mList.add(strings);
        }
        for (int i = 0; i < size1; i++) {
            //给外围数组,赋值一整个 数组
            items[i] = mList.get(i);

        }


    }


    @Nullable
    @Override
    public CharSequence onCreateDescription() {
        return super.onCreateDescription();
    }

    /**
     * 将获取到的int型ip转成string类型
     */
    public static String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    /**
     * 是否使用沉浸式状态栏
     */
    protected boolean isStatusBarEnabled() {
        return true;
    }

    /**
     * 状态栏字体深色模式
     */
    protected boolean isStatusBarDarkFont() {
        return true;
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    @NonNull
    public ImmersionBar getStatusBarConfig() {
        if (mImmersionBar == null) {
            mImmersionBar = createStatusBarConfig();
        }
        return mImmersionBar;
    }

    /**
     * 初始化沉浸式状态栏
     */
    @NonNull
    protected ImmersionBar createStatusBarConfig() {
        return ImmersionBar.with(this)
                // 默认状态栏字体颜色为黑色
                .statusBarDarkFont(isStatusBarDarkFont())
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.white)
                // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
                .autoDarkModeEnable(true, 0.2f);
    }

    /**
     * 设置标题栏的标题
     */
    @Override
    public void setTitle(@StringRes int id) {
        setTitle(getString(id));
    }

    /**
     * 设置标题栏的标题
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (getTitleBar() != null) {
            getTitleBar().setTitle(title);
        }
    }

    @Override
    @Nullable
    public TitleBar getTitleBar() {
        if (mTitleBar == null) {
            mTitleBar = obtainTitleBar(getContentView());
        }
        return mTitleBar;
    }

    @Override
    public void onLeftClick(View view) {
        onBackPressed();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(R.anim.right_in_activity, R.anim.right_out_activity);
    }

    @Override
    public void finish() {
        super.finish();
        //转场动画
        overridePendingTransition(R.anim.left_in_activity, R.anim.left_out_activity);
    }

    /**
     * {@link OnHttpListener}
     */

    @Override
    public void onStart(Call call) {
        showDialog();
    }

    @Override
    public void onSucceed(Object result) {
        if (result instanceof HttpData) {
            toast(((HttpData<?>) result).getMessage());
        }
    }

    @Override
    public void onFail(Exception e) {
        toast(e.getMessage());
    }

    @Override
    public void onEnd(Call call) {
        hideDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isShowDialog()) {
            hideDialog();
        }
        mDialog = null;

    }

    //获取状态栏的高度
    public int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return activity.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }






    /**
     * 执行动画的方法
     * @param statue    开启还是关闭   true为打开
     * @param mAnimView 播放的View
     */

    public void startDialogIconAnim(Boolean statue, View mAnimView) {
        if (statue) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mAnimView, "rotation", 0f, 180f);
            animator.setDuration(150);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mAnimView instanceof ImageView) {
                        ImageView mView = (ImageView) mAnimView;
                        mView.setImageDrawable(getDrawable(R.drawable.ic_case_select_down_pre));
                    } else if (mAnimView instanceof TextView) {
                        mAnimView.setBackground(getDrawable(R.drawable.ic_case_select_down_nor));
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mAnimView, "rotation", 180f, 360f);
            animator.setDuration(150);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {


                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    if (mAnimView instanceof ImageView) {
                        ImageView mView = (ImageView) mAnimView;
                        mView.setImageDrawable(getDrawable(R.drawable.ic_case_select_down_nor));
                    } else if (mAnimView instanceof TextView) {
                        mAnimView.setBackground(getDrawable(R.drawable.ic_case_select_down_nor));
                    }

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }


    public void setSocketStatue(TextView mCurrentSocketStatue){
        //握手成功
        if (HandService.UDP_HAND_GLOBAL_TAG) {
            mCurrentSocketStatue.setTextColor(getResources().getColor(R.color.color_25A5FF));
            mCurrentSocketStatue.setText(Constants.SOCKET_STATUE_ONLINE);
        }else {
            mCurrentSocketStatue.setTextColor(getResources().getColor(R.color.red));
            mCurrentSocketStatue.setText(Constants.SOCKET_STATUE_OFFLINE);
        }
    }
}