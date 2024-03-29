package com.company.iendo.mineui.activity.casemanage.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.company.iendo.R;
import com.company.iendo.action.StatusAction;
import com.company.iendo.app.TitleBarFragment;
import com.company.iendo.bean.AddCaseBean;
import com.company.iendo.bean.CaseDetailBean;
import com.company.iendo.bean.DeleteBean;
import com.company.iendo.bean.DetailPictureBean;
import com.company.iendo.bean.DetailVideoBean;
import com.company.iendo.bean.DialogItemBean;
import com.company.iendo.bean.ListDialogDateBean;
import com.company.iendo.bean.event.RefreshCaseMsgEvent;
import com.company.iendo.bean.event.SocketRefreshEvent;
import com.company.iendo.bean.model.LocalDialogCaseModelBean;
import com.company.iendo.bean.socket.HandBean;
import com.company.iendo.bean.socket.UpdateCaseBean;
import com.company.iendo.green.db.CaseDBUtils;
import com.company.iendo.green.db.UserDBBean;
import com.company.iendo.green.db.UserDBUtils;
import com.company.iendo.green.db.downcase.CaseDBBean;
import com.company.iendo.green.db.downcase.CaseImageListBean;
import com.company.iendo.green.db.downcase.DownloadedNameListBean;
import com.company.iendo.manager.ActivityManager;
import com.company.iendo.mineui.activity.MainActivity;
import com.company.iendo.mineui.activity.casemanage.AddCaseActivity;
import com.company.iendo.mineui.activity.casemanage.DetailCaseActivity;
import com.company.iendo.mineui.activity.casemanage.dowvideo.DownVideoSelectedActivity;
import com.company.iendo.other.Constants;
import com.company.iendo.other.HttpConstant;
import com.company.iendo.service.HandService;
import com.company.iendo.ui.dialog.MenuDialog;
import com.company.iendo.ui.dialog.MessageDialog;
import com.company.iendo.utils.CalculateUtils;
import com.company.iendo.utils.FileUtil;
import com.company.iendo.utils.LogUtils;
import com.company.iendo.utils.SharePreferenceUtil;
import com.company.iendo.utils.SocketUtils;
import com.company.iendo.widget.LinesEditView;
import com.company.iendo.widget.StatusLayout;
import com.hjq.base.BaseDialog;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.widget.view.ClearEditText;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.company.iendo.mineui.activity.MainActivity.getCurrentItemID;

/**
 * company：江西神州医疗设备有限公司
 * <p>
 * <p>
 * author： LoveLin
 * time：2021/10/29 13:55
 * desc：第2个tab-fragment
 */
public class DetailFragment extends TitleBarFragment<MainActivity> implements StatusAction, DetailCaseActivity.OnEditStatusListener {

    private AppCompatTextView mTV;
    private StatusLayout mStatusLayout;
    private Boolean mFirstIn = true;    //第一次进入界面---->解决  首次进来 tosat 提示
    private Boolean mEditStatus = false;    //编辑状态为true,不可编辑状态为flase
    private Boolean isFatherExit = false;   //父类Activity 是否主动退出的标识,主动退出需要请求保存fragment的更新数据
    private DetailCaseActivity mActivity;
    private CaseDetailBean mBean;
    private String mBaseUrl;
    private TextView tv_01_age_type;
    private boolean mFragClickable = false;  //dialog数据请求错误,相对于dialog不允许弹窗,不然会闪退
    private HashMap<String, String> mParamsMap;
    private HashMap mDialogItemMap;
    private ClearEditText et_01_check_num, et_01_name, et_01_sex_type, et_01_age, et_01_jop, et_01_fee, et_01_get_check_doctor;
    //            et_01_i_tell_you, et_01_bad_tell;
    private LinesEditView lines_01_i_tell_you, lines_01_bad_tell;
    private LinesEditView lines_03_case_history, lines_03_family_case_history;
    private LinesEditView etlines_02_mirror_see, etlines_02_mirror_result, etlines_02_live_check, etlines_02_cytology, etlines_02_test,
            etlines_02_pathology, etlines_02_advice;
    private ClearEditText et_02_mirror_see, et_02_mirror_result, et_02_live_check, et_02_cytology, et_02_test, et_02_pathology,
            et_02_advice, et_02_check_doctor;
    private ClearEditText et_03_door_num, et_03_protection_num, et_03_section, et_03_device, et_03_case_num, et_03_in_hospital_num,
            et_03_case_area_num, et_03_case_bed_num, et_03_native_place, et_03_ming_zu, et_03_is_married, et_03_tel, et_03_address,
            et_03_my_id_num, edit_03_case_history, edit_03_family_case_history;

    private ArrayList<ClearEditText> mEditList;
    private ArrayList<LinesEditView> mLineEditList;
    private String mDeviceCode;  //当前设备id-code
    private String mUserID;    //当前用户id
    private String currentItemCaseID;
    private ArrayList<String> ageList;
    private ArrayList<String> mNameList;
    private HashMap<String, String> mPathMap;     //例如imageName=001.jpg  url=http://192.168.64.56:7001/1_3/001.jpg
    private ArrayList<String> mVideoPathList = new ArrayList<>();     //视频的标题
    private ClearEditText edit_01_i_tell_you;
    private ClearEditText edit_01_i_bad_tell;
    private CaseDetailBean.DataDTO mDataBean;
    private String mUserName;
    private static final int IMAGE_DOWN = 126;   //下载图片
    private static final int IMAGE_DOWN_UPDATE = 127;   //更新图片
    private static final int DOWN_STATUE_DOWNING = 128;   //下载中,解决没有图片的时候,handler不发消息,从而UI界面不显示状态的bug
    private static final int DOWN_STATUE_OK = 129;   //下载完毕,更新图片
    private static boolean Details_Reault_Ok = false;
    private String itemID;
    private ArrayList<ImageView> mImageViewList;
    private String mCurrentDonwTime;
    private ImageView iv_01_age_type, iv_01_sex_type, iv_01_jop, tv_01_get_check_doctor, iv_01_i_tell_you, iv_01_bad_tell;
    private ImageView iv_02_mirror_see, iv_02_mirror_result, iv_02_live_check, iv_02_cytology, iv_02_test, iv_02_pathology, iv_02_advice, iv_02_check_doctor;
    private ImageView iv_03_section, iv_03_device, iv_03_ming_zu, iv_03_is_married;
    private String videosCounts;
    private String imageCounts;
    private int mCurrentDownImageCount = 0;   //第一次下载的时候,当前下载第一张的标识
    private int mImageCountNum = 0;           //上位机总图片数目
    private int mCurrentUpdateDownImageCount = 0;   //更新下载的时候,当前下载第一张的标识

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            SocketRefreshEvent event = new SocketRefreshEvent();
            event.setUdpCmd(Constants.UDP_CUSTOM_DOWN_OVER);
            switch (msg.what) {
                case DOWN_STATUE_DOWNING: //下载中
                    event.setData("false");
                    EventBus.getDefault().post(event);
                    break;
                case DOWN_STATUE_OK: //下载完毕
                    event.setData("true");
                    EventBus.getDefault().post(event);
                    toast("下载完毕");
                    break;
                case IMAGE_DOWN: //下载图片
                    if (mImageCountNum == mCurrentDownImageCount) {
                        toast("下载完毕");
                        //下载中的标识,true标识下载中,false标识没有下载
                        mMMKVInstace.encode(Constants.KEY_Picture_Downing, false);
                        mCurrentDownImageCount = 0;
                        mImageCountNum = 0;
                        event.setData("true");
                    } else {
                        //下载中的标识,true标识下载中,false标识没有下载
                        mMMKVInstace.encode(Constants.KEY_Picture_Downing, true);
                        event.setData("false");
                    }
                    EventBus.getDefault().post(event);
                    break;
                case IMAGE_DOWN_UPDATE://更新图片
                    if (mImageUpdateCountNum == mCurrentUpdateDownImageCount) {
                        toast("下载完毕");
                        //下载中的标识,true标识下载中,false标识没有下载
                        mMMKVInstace.encode(Constants.KEY_Picture_Downing, true);
                        mCurrentUpdateDownImageCount = 0;
                        mImageUpdateCountNum = 0;
                        event.setData("true");
                    } else {
                        //下载中的标识,true标识下载中,false标识没有下载
                        mMMKVInstace.encode(Constants.KEY_Picture_Downing, false);
                        event.setData("false");
                    }
                    EventBus.getDefault().post(event);
                    break;
            }
        }
    };
    private int mImageUpdateCountNum;  //需要更新上位机总数

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_detail_message;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        mStatusLayout = findViewById(R.id.detail_hint);
        mBaseUrl = (String) SharePreferenceUtil.get(getActivity(), SharePreferenceUtil.Current_BaseUrl, "192.168.132.102");
        mDeviceCode = (String) SharePreferenceUtil.get(getActivity(), SharePreferenceUtil.Current_DeviceCode, "");
        mUserID = (String) SharePreferenceUtil.get(getActivity(), SharePreferenceUtil.Current_Login_UserID, "");
        mUserName = mLoginUserName;
        currentItemCaseID = getCurrentItemID();
        initLayoutViewDate();
        setEditStatus(DetailCaseActivity.mModelView);
        responseListener();
        //请求界面数据
        sendRequest(getCurrentItemID());
        //判断该病例信息是否下载过
        checkCaseIsDown();

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //activity和fragment 通信回调
        mActivity = (DetailCaseActivity) getActivity();
        mActivity.setOnEditStatusListener(this);
    }


    /**
     * 下载的时候从这里开始
     *
     * @param currentItemID 获取到mPathMap  存入图片信息
     *                      例如imageName=001.jpg  url=http://192.168.64.56:7001/3/001.jpg
     */

    private void sendRequest(String currentItemID) {
        //获取图片并且把用户名添加到集合当中,以备下载需要
        OkHttpUtils.get()
                .url(mBaseUrl + HttpConstant.CaseManager_CasePictures)
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
                        mPathMap = new HashMap<>();
                        if ("" != response) {
                            DetailPictureBean mBean = mGson.fromJson(response, DetailPictureBean.class);
                            List<DetailPictureBean.DataDTO> data = mBean.getData();
                            if (0 == mBean.getCode()) {  //成功
                                showComplete();
                                if (mBean.getData().size() != 0) {
                                    for (int i = 0; i < mBean.getData().size(); i++) {
                                        String imageName = mBean.getData().get(i).getImagePath();
                                        String url = mBaseUrl + "/" + getCurrentItemID() + "/" + imageName;
                                        //例如imageName=001.jpg  url=http://192.168.64.56:7001/3/001.jpg
                                        mPathMap.put(imageName, url);

                                    }

                                } else {
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


        showLoading();
        sendCaseInfoRequest(currentItemID, false, null);

    }

    /**
     * 获取病例详情
     *
     * @param currentItemID
     * @param isUpdateDown  在下载过病例的前提下,编辑了病例,再更新DB
     */
    private void sendCaseInfoRequest(String currentItemID, Boolean isUpdateDown, File toLocalFile) {
        OkHttpUtils.get()
                .url(mBaseUrl + HttpConstant.CaseManager_CaseInfo)
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
                            mBean = mGson.fromJson(response, CaseDetailBean.class);
                            if (0 == mBean.getCode()) {  //成功
                                showComplete();
                                Details_Reault_Ok = true;
                                setLayoutData(mBean);
                                if (isUpdateDown) {
                                    //下载病例和图片信息---到本地sd卡里面
                                    downLocalCaseUserData(toLocalFile);
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

    /**
     * 设置数据
     *
     * @param mBean
     */
    private void setLayoutData(CaseDetailBean mBean) {
        mDataBean = mBean.getData();
        //送检医生/SubmitDoctor   检查医生/ExaminingPhysician
        et_01_check_num.setText(mDataBean.getCaseNo());     //检查号也叫病例编号
        et_01_name.setText(mDataBean.getName());
        edit_01_i_tell_you.setText(mDataBean.getChiefComplaint() + "");
        et_03_is_married.setText("" + mDataBean.getMarried());
        et_01_sex_type.setText("" + mDataBean.getSex());
        et_03_tel.setText("" + mDataBean.getTel());
        et_03_address.setText("" + mDataBean.getAddress());
        et_02_live_check.setText(mDataBean.getBiopsy()); //活检

        //        String PatientNo = et_01_check_num.getText().toString().trim();       //病人编号---检查号???
        et_03_my_id_num.setText("" + mDataBean.getCardID());
        lines_03_case_history.setContentText("" + mDataBean.getMedHistory());
        lines_03_family_case_history.setContentText("" + mDataBean.getFamilyHistory());
        et_03_ming_zu.setText("" + mDataBean.getRace());
        et_01_jop.setText("" + mDataBean.getOccupatior());
        et_03_protection_num.setText("" + mDataBean.getInsuranceID());
        et_03_native_place.setText("" + mDataBean.getNativePlace());
        //        String IsInHospital = et_03_in_hospital_num.getText().toString().trim();       //是否还在医院住院  ???
//        String DOB = et_.getText().toString().trim();       //生日                                  ???
        et_01_age.setText("" + mDataBean.getPatientAge());
        tv_01_age_type.setText("" + mDataBean.getAgeUnit());
        et_03_case_bed_num.setText("" + mDataBean.getBedID());
        et_03_case_area_num.setText("" + mDataBean.getWardID());
        et_03_case_num.setText("" + mDataBean.getCaseID());
        String SubmitDoctor = et_03_tel.getText().toString().trim();       //送检医生/SubmitDoctor   检查医生/ExaminingPhysician       ???
        et_03_section.setText("" + mDataBean.getDepartment());
        et_03_device.setText("" + mDataBean.getDevice());
        et_01_fee.setText("" + mDataBean.getFee());
        etlines_02_test.setContentText("" + mDataBean.getTest());
        etlines_02_advice.setContentText("" + mDataBean.getAdvice());
        et_03_in_hospital_num.setText("" + mDataBean.getInpatientID());
        et_03_door_num.setText("" + mDataBean.getOutpatientID());
        etlines_02_live_check.setContentText("" + mDataBean.getBiopsy());
        etlines_02_cytology.setContentText("" + mDataBean.getCtology());
        etlines_02_pathology.setContentText("" + mDataBean.getPathology());
        lines_01_bad_tell.setContentText("" + mDataBean.getClinicalDiagnosis());
        etlines_02_mirror_see.setContentText("" + mDataBean.getCheckContent());
        etlines_02_mirror_result.setContentText("" + mDataBean.getCheckDiagnosis());


        et_02_check_doctor.setText(mDataBean.getExaminingPhysician());//检查医生/ExaminingPhysician
        et_01_get_check_doctor.setText(mDataBean.getSubmitDoctor());//检查医生/ExaminingPhysician

    }

    @Override
    protected void initData() {

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

    private void setEditStatus(AppCompatTextView mModelView) {
        if (null != mEditList && !mEditList.isEmpty()) {
            for (int i = 0; i < mEditList.size(); i++) {
                if (mEditStatus) {
                    //设置可编辑状态
                    mEditList.get(i).setFocusableInTouchMode(true);
                    mEditList.get(i).setFocusable(true);
                    mEditList.get(i).requestFocus();

                    //android:focusable="false"
                    //谈对话框的不能获取焦点
                } else {
                    //设置不可编辑状态
                    mEditList.get(i).setFocusable(false);
                    mEditList.get(i).setFocusableInTouchMode(false);

                }

            }
            for (int i1 = 0; i1 < mLineEditList.size(); i1++) {
                //设置可编辑状态
                if (mEditStatus) {
                    mLineEditList.get(i1).setFocusableInTouchMode(true);
                    mLineEditList.get(i1).setFocusable(true);
                    mLineEditList.get(i1).requestFocus();
                } else {
                    //设置不可编辑状态
                    mLineEditList.get(i1).setFocusable(false);
                    mLineEditList.get(i1).setFocusableInTouchMode(false);
                }

            }

        }

        //可编辑状态下,需要显示iamgeview
        if (mEditStatus) {
            for (int i = 0; i < mImageViewList.size(); i++) {
                ImageView imageView = mImageViewList.get(i);
                imageView.setVisibility(View.VISIBLE);
                mModelView.setVisibility(View.VISIBLE);
            }
        } else {
            for (int i = 0; i < mImageViewList.size(); i++) {
                ImageView imageView = mImageViewList.get(i);
                imageView.setVisibility(View.GONE);
                mModelView.setVisibility(View.GONE);

            }
        }

//        private Boolean mEditStatus = false;    //编辑状态为true,不可编辑状态为flase
//        private Boolean isFatherExit = false;   //父类Activity 是否主动退出的标识,主动退出需要请求保存fragment的更新数据

//        //编辑状态为true,不可编辑状态为flase,默认false不可编辑
        if (!mEditStatus) {//切换到了不可编辑模式,发送请求
            if (mFirstIn) {  //解决  首次进来 tosat 提示
                mFirstIn = false;
            } else {
                checkDataAndRequest();
            }
        }
        if (isFatherExit) {//父类界面主动退出,保存当前数据
            CharSequence rightTitle = DetailCaseActivity.mTitlebar.getRightTitle();
            if ("编辑".equals(rightTitle)) {
                SocketRefreshEvent event = new SocketRefreshEvent();
                event.setUdpCmd(Constants.UDP_CUSTOM_FINISH);
                EventBus.getDefault().post(event);
            } else {
                new MessageDialog.Builder(getActivity())
                        .setTitle("是否返回")
                        .setMessage("当前正在编辑病历信息,是否返回")
                        .setConfirm("继续编辑")
                        .setCancel("立即返回")
                        .setListener(new MessageDialog.OnListener() {

                            @Override
                            public void onConfirm(BaseDialog dialog) {

                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                                SocketRefreshEvent event = new SocketRefreshEvent();
                                event.setUdpCmd(Constants.UDP_CUSTOM_FINISH);
                                EventBus.getDefault().post(event);

                            }
                        })
                        .show();
            }


        }

    }

    /**
     * EditText 和  弹窗是否可以用的标识
     * 以及导入病例模板  mModelConfirm 导入模板的View
     *
     * @param status
     */
    @Override
    public void onEditStatus(boolean status, boolean isFatherExit,AppCompatTextView mModelView) {
        this.mEditStatus = status;
        this.isFatherExit = isFatherExit;
        setEditStatus(mModelView);
    }

    /**
     * 病例模板选中之后,回调的数据
     *
     * @param mBean
     */
    @Override
    public void onClickModel(LocalDialogCaseModelBean mBean) {
        //把把三个参数置空,在设置新的数据,其他不变
        et_02_mirror_see.setText("");
        et_02_mirror_result.setText("");
        et_02_advice.setText("");
        et_02_mirror_see.setText(""+mBean.getMirrorSee());
        et_02_mirror_result.setText(""+mBean.getMirrorDiagnostics());
        et_02_advice.setText(""+mBean.getAdvice());
        et_02_mirror_result.setFocusableInTouchMode(true);
        et_02_mirror_result.setFocusable(true);
        et_02_mirror_result.requestFocus();
    }

    @Override
    public void onDown(boolean userInfo, boolean userPicture) {
        //下载的时候就去请求,获取Video视频数目和标题
        sendGetVideoPathListRequest(getCurrentItemID());
        String mCaseDownStr = DetailCaseActivity.mCaseDown.getText().toString();
        if (("已下载").equals(mCaseDownStr)) {
            //下载图片
            requestPermission();
        } else {
            new MessageDialog.Builder(getActivity())
                    .setTitle("提示")
                    // 内容必须要填写
                    .setMessage("确定下载吗?")
                    .setConfirm(getString(R.string.common_confirm))
                    .setCancel(getString(R.string.common_cancel))
                    .setListener(new MessageDialog.OnListener() {
                        @Override
                        public void onConfirm(BaseDialog dialog) {
                            //下载图片
                            requestPermission();
                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {
                        }
                    })
                    .show();
        }
    }

    /**
     * 下载视频  DetailCaseActivity 点击下载视频的时候下载视频
     * 1,如果没有下载病例和图片,弹出对话框告知,必须先现在病例和图片
     * 2,下载过了病例和图片,才弹出视频选择界面
     */
    @Override
    public void onDownVideo() {
        checkCaseIsDown();
        String mCaseDownStr = DetailCaseActivity.mCaseDown.getText().toString();
        if (("已下载").equals(mCaseDownStr)) {
            //跳转下载界面
            Intent intent = new Intent(getAttachActivity(), DownVideoSelectedActivity.class);
//            Intent intent = new Intent(getAttachActivity(), DownSelectedVideoActivity.class);
            intent.putExtra("currentItemCaseID", getCurrentItemID());
            intent.putExtra("mDeviceCode", mDeviceCode);
            startActivity(intent);
        } else {
            toast("请先下载病历和图片");
        }
    }

    /**
     * 判断当前病例是否下载过
     */
    private void checkCaseIsDown() {
        /**
         * 本地文件夹命名规则:文件夹（设备ID_病例ID）
         */
        //创建本地的/MyDownImages/mID文件夹  再把图片下载到这个文件夹下  文件夹（设备ID-病例ID）
        String dirName = Environment.getExternalStorageDirectory() + "/MyDownImages/" + mDeviceCode + "_" + getCurrentItemID();
        File toLocalFile = new File(dirName);
        //此处做校验,本文件夹创建过,并且里面的图片数量和请求结果数量一直,表示下载过
        boolean FileExists = toLocalFile.exists();
        if (FileExists) {//本地存在,并且上位机请求成功
            SocketRefreshEvent event = new SocketRefreshEvent();
            event.setUdpCmd(Constants.UDP_CUSTOM_DOWN_OVER);
            event.setData("true");
            EventBus.getDefault().post(event);

        }

    }


    private void startDownPictureCase() {

        /**
         * 本地文件夹命名规则:文件夹（设备ID_病例ID）
         */
        //创建本地的/MyDownImages/mID文件夹  再把图片下载到这个文件夹下  文件夹（设备ID-病例ID）
        String dirName = Environment.getExternalStorageDirectory() + "/MyDownImages/" + mDeviceCode + "_" + getCurrentItemID();
        File toLocalFile = new File(dirName);
        //此处做校验,本文件夹创建过,并且里面的图片数量和请求结果数量一直,表示下载过
        boolean FileExists = toLocalFile.exists();
        //存在,更新
        if (FileExists) {
            int length = toLocalFile.listFiles().length;  //本地下载的图片数量
            if (null != mPathMap) {
                int size = mPathMap.keySet().size();  //当前上位机该病例图片数量
                //下载的图片数量和请求的图片数量不一样
                if (length == size || length <= size) {
                    new MessageDialog.Builder(getActivity())
                            .setTitle("提示")
                            .setMessage("该病历已经下载,是否需要更新?")
                            .setConfirm(getString(R.string.common_confirm))
                            .setCancel(getString(R.string.common_cancel))
                            //.setAutoDismiss(false)
                            .setListener(new MessageDialog.OnListener() {

                                @Override
                                public void onConfirm(BaseDialog dialog) {
                                    mHandler.sendEmptyMessage(DOWN_STATUE_DOWNING);
                                    updatePictureCaseData(toLocalFile);
                                }

                                @Override
                                public void onCancel(BaseDialog dialog) {
                                }
                            })
                            .show();
                }
            }
        } else if (!toLocalFile.exists()) {   //没有下载过,下载
            toLocalFile.mkdir();
            mHandler.sendEmptyMessage(DOWN_STATUE_DOWNING);
            downPictureCaseData(toLocalFile);

        }

    }

    /**
     * 更新图片
     * 下载过,更新图片,比较上位机图片和本地图片名字,不相等的就下载
     *
     * @param toLocalFile
     */
    private void updatePictureCaseData(File toLocalFile) {
        File[] files = toLocalFile.listFiles();
        ArrayList<String> mFileList = new ArrayList<>();
        HashMap<String, String> mFilesHashMap = new HashMap<String, String>(); //例如imageName=001.jpg  url=http://192.168.64.56:7001/1_3/001.jpg

        //1,存入当前本地所有图片的名字
        for (int i = 0; i < files.length; i++) {
            mFileList.add(files[i].getName());
        }

        //2,获取和上位机不相同的图片名字和url
        if (null != mPathMap && !mPathMap.isEmpty()) {   //说明有图片
            for (String key : mPathMap.keySet()) {
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                mImageCountNum = mPathMap.keySet().size();//5
                //此处循环遍历上位机map,和本地文件夹图片作比较,没有包含,直接更新下载使用

                boolean contains = mFileList.contains(key);//包含返回true
                if (!contains) {//没有包含,存入当前上位机多出来的图片名字和url,提供更新使用
                    mFilesHashMap.put(key, mPathMap.get(key));
                }
            }
        }

        //3,开始更新下载
        if (null != mFilesHashMap && !mFilesHashMap.isEmpty()) {   //说明有图片
            for (String key : mFilesHashMap.keySet()) {
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                //5
                mImageUpdateCountNum = mFilesHashMap.keySet().size();
                sendUpdatePictureRequest(toLocalFile, mPathMap.get(key), key);
            }

        } else {
            //图片数量和名字都相等的时候,直接提示,下载完毕
            toast("下载完毕");
        }

        //4,开始下载最新的用户和病例信息
        /**
         * 更新病例和用户存入数据库
         * true表示 当前下载病例的时候,编辑了病例,需要获取最新的bean
         * 再次请求数据获取最新更改的UserBean和CaseBean,再存入数据库,避免bug
         */

        sendCaseInfoRequest(getCurrentItemID(), true, toLocalFile);


    }

    private void sendGetVideoPathListRequest(String currentItemID) {

        //这里请求是为了获取视频总数,在需要下载的时候存入视频标题
        OkHttpUtils.get()
                .url(mBaseUrl + HttpConstant.CaseManager_CaseVideos)
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
                            DetailVideoBean mBean = mGson.fromJson(response, DetailVideoBean.class);
                            List<DetailVideoBean.DataDTO> data = mBean.getData();
                            if (0 == mBean.getCode()) {  //成功
                                showComplete();
                                if (mBean.getData().size() != 0) {
                                    mVideoPathList.clear();
                                    for (int i = 0; i < mBean.getData().size(); i++) {
                                        DetailVideoBean.DataDTO dataDTO = mBean.getData().get(i);
                                        //        mBaseUrl=http://192.168.132.102:7001
                                        String mUrl = mBaseUrl + "/" + dataDTO.getRecordID() + "/" + dataDTO.getFilePath();
                                        //存完整的url
                                        mVideoPathList.add(mUrl);
                                    }
                                }
                            }
                        }
                    }
                });
    }


    /**
     * 图片下载
     * 开始下载病例信息和图片到本地
     *
     * @param toLocalFile
     */
    private void downPictureCaseData(File toLocalFile) {
        //mPathMap===例如imageName=001.jpg  url=http://192.168.64.56:7001/1_3/001.jpg
        if (null != mPathMap && !mPathMap.isEmpty()) {   //说明有图片
            for (String key : mPathMap.keySet()) {
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                mImageCountNum = mPathMap.keySet().size();//5
                sendPictureRequest(toLocalFile, mPathMap.get(key), key);
            }

        }
        /**
         * 下载病例和用户存入数据库
         * true表示 当前下载病例的时候,编辑了病例,需要获取最新的bean
         * 再次请求数据获取最新更改的UserBean和CaseBean,再存入数据库,避免bug
         */
        sendCaseInfoRequest(getCurrentItemID(), true, toLocalFile);


    }

    /**
     * 更新图片
     *
     * @param toLocalFile
     * @param path
     * @param pictureName
     */
    private void sendUpdatePictureRequest(File toLocalFile, String path, String pictureName) {
//        String url = "http://images.csdn.net/20150817/1.jpg";
        if (!toLocalFile.exists()) {
            toLocalFile.mkdir();
        }
        OkHttpUtils.get()
                .url(path)
                .build()
                .execute(new FileCallBack(toLocalFile.getAbsolutePath(), pictureName) {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        //下载失败
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        mCurrentUpdateDownImageCount++;
                        mHandler.sendEmptyMessage(IMAGE_DOWN_UPDATE);
                        //=====/storage/emulated/0/MyDownImages/2_3/004.jpg
                        //刷新相册 必须下载完了才能退出不然容易出现bug ,所以我们放在每次进入该界面的时候刷新
                        try {

                            if (null != getActivity()) {
                                MediaStore.Images.Media.insertImage(getActivity().getApplication().getContentResolver(), toLocalFile.getAbsolutePath() + "/" + pictureName, pictureName, "");
                                // 最后通知图库更新
                                getActivity().getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(new File(toLocalFile.getPath()))));
                            } else {
                                LogUtils.e("下载图片的时候(sendUpdatePictureRequest),getActivity()==00==null");
                                mMMKVInstace.encode(Constants.KEY_Picture_Downing, false);

                            }

//                             视频下载,刷新图库
//                            //说明第一个参数上下文，第二个参数是文件路径例如
//                            ///storage/emulated/0/1621832516463_1181875151.mp4 第三个参数是文件类型，传空代表自行根据文件后缀判断刷新到相册
//                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fileNamePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                                @Override
//                                public void onScanCompleted(String path, Uri uri) {
//                                    //刷新成功的回调方法
//                                }
//                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 下载图片
     *
     * @param toLocalFile
     * @param path
     * @param pictureName
     */
    private void sendPictureRequest(File toLocalFile, String path, String pictureName) {
//        String url = "http://images.csdn.net/20150817/1.jpg";
        if (!toLocalFile.exists()) {
            toLocalFile.mkdir();
        }
        OkHttpUtils.get()
                .url(path)
                .build()
                .execute(new FileCallBack(toLocalFile.getAbsolutePath(), pictureName) {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        //下载失败
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        mCurrentDownImageCount++;
                        mHandler.sendEmptyMessage(IMAGE_DOWN);
                        //=====/storage/emulated/0/MyDownImages/2_3/004.jpg
                        //刷新相册 必须下载完了才能退出不然容易出现bug ,所以我们放在每次进入该界面的时候刷新
                        try {
                            if (null != getActivity()) {
                                MediaStore.Images.Media.insertImage(getActivity().getApplication().getContentResolver(), toLocalFile.getAbsolutePath() + "/" + pictureName, pictureName, "");
//                            MediaStore.Images.Media.insertImage(getActivity().getApplication().getContentResolver(), toLocalFile.getAbsolutePath() + "/" + pictureName, pictureName, "");
                                // 最后通知图库更新
                                getActivity().getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(new File(toLocalFile.getPath()))));
                            } else {
                                LogUtils.e("下载图片的时候(sendPictureRequest),getActivity()==null");
                                mMMKVInstace.encode(Constants.KEY_Picture_Downing, false);
                            }

//                             视频下载,刷新图库
//                            //说明第一个参数上下文，第二个参数是文件路径例如
//                            ///storage/emulated/0/1621832516463_1181875151.mp4 第三个参数是文件类型，传空代表自行根据文件后缀判断刷新到相册
//                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{fileNamePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                                @Override
//                                public void onScanCompleted(String path, Uri uri) {
//                                    //刷新成功的回调方法
//                                }
//                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 下载病例和图片信息---到本地sd卡里面
     * 创建一个CaseDBBean,直接存信息和图片信息即可
     */
    private void downLocalCaseUserData(File toLocalFile) {
        mLoginUserName = mMMKVInstace.decodeString(Constants.KEY_CurrentLoginUserName);
        if (getActivity() == null) {
            LogUtils.e("下载图片的时候(downLocalCaseUserData),getActivity()====null");
            mMMKVInstace.encode(Constants.KEY_Picture_Downing, false);
            return;
        }

        /**
         * 创建需要下载的本地的--->病例表
         */
        CaseDBBean caseDBBean = new CaseDBBean();
        //避免多次创建,需要区别赋值

        //查询当前设备码下,当前病例详情ID(mDataBean.getID()),之下的病例,有就是下载过,没有就是没下载过,数字长度0或者1
        List<CaseDBBean> myList = CaseDBUtils.getQueryBeanByTow02(getApplication(), mDeviceCode, mDataBean.getID() + "");

        /**
         *   病例不存在,就存入
         *   存在就不处理
         */
        if (myList.size() == 0) {
            /**
             * 不存在这个设备
             */
            //设置是否下载过的标识 ==上位机返回的ID
            caseDBBean.setOthers(mDataBean.getID() + "");
            caseDBBean.setDeviceCaseID(mDeviceCode + "");  //用户表和设备表进行绑定, //用户表和设备表进行绑定, //用户表和设备表进行绑定
            caseDBBean.setOccupatior(mDataBean.getOccupatior() + "");// 职业
            caseDBBean.setNativePlace(mDataBean.getAddress() + "");    //籍贯
            caseDBBean.setFee(mDataBean.getFee() + "");    //收费
            caseDBBean.setChiefComplaint(mDataBean.getChiefComplaint() + "");  //主诉
            //图片路径集合--文件夹（设备ID_病例ID）
//      /storage/emulated/0/MyDownImages/2_3/004.jpg
//      LogUtils.e("下载图片==更新相册图片==" + toLocalFile.getAbsolutePath() + "/" + pictureName);       本地存的路径
//     toLocalFile.getAbsolutePath()==/storage/emulated/0/MyDownImages/0000000000000000546017FE6BC28949_1154
//     pictureName==001.jpg
            ArrayList<CaseImageListBean> caseImageList = new ArrayList<CaseImageListBean>();
            if (null != mPathMap && !mPathMap.isEmpty()) {
                for (String key : mPathMap.keySet()) {
                    //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                    int size = mPathMap.keySet().size();
//                sendPictureRequest(toLocalFile, mPathMap.get(key), key, false);
                    CaseImageListBean imageBean = new CaseImageListBean();
                    // /storage/emulated/0/MyDownImages/0000000000000000546017FE6BC28949_1154/001.jpg
                    imageBean.setImagePath(toLocalFile.getAbsolutePath() + "/" + key);  //存入本地存储路径
                    caseImageList.add(imageBean);
                }
                caseDBBean.setImageList(caseImageList);    //图片路径集合--文件夹（设备ID-病例ID）
            }

            //存入视频 标题
//            ArrayList<CaseVideoListBean> VideoList = new ArrayList<CaseVideoListBean>();
//            if (null != mVideoPathList && mVideoPathList.size() > 0) {
//                for (int i = 0; i < mVideoPathList.size(); i++) {
//                    CaseVideoListBean caseVideoListBean = new CaseVideoListBean();
//                    String str = mVideoPathList.get(i);
//                    //http://192.168.67.219:7001/1196/88820220424081936912.mp4
//                    caseVideoListBean.setVideoPath(str);
//                    int index = str.lastIndexOf("/");
//                    String substring = str.substring(index + 1, str.length());
////                    caseVideoListBean.setFileName();
//                    caseVideoListBean.setDown(false);
//                    VideoList.add(caseVideoListBean);
//                }
//                caseDBBean.setVideoList(VideoList);
//            }

            //不管存在不存在,都需要校验和存储被下载着名单列表,当前情况是病例不存在,所以创建list集合
            ArrayList<DownloadedNameListBean> mNameList = new ArrayList<DownloadedNameListBean>();
            DownloadedNameListBean nameBean = new DownloadedNameListBean();
            nameBean.setDownloadedByName(mLoginUserName);
            mNameList.add(nameBean);
            caseDBBean.setDownloadedNameList(mNameList);


            caseDBBean.setBiopsy(mDataBean.getBiopsy() + "");    //活检
            caseDBBean.setPathology(mDataBean.getPathology() + "");    //病理学
            caseDBBean.setFeeType(mDataBean.getFeeType() + "");    //收费类型
            caseDBBean.setMedHistory(mDataBean.getMedHistory() + "");    // 医疗病史
            caseDBBean.setLastCheckUserID(mDataBean.getLastCheckUserID() + "");    // 最后一个来查房的医生
            caseDBBean.setAgeUnit(mDataBean.getAgeUnit() + "");    // 年龄单位
            caseDBBean.setAdvice(mDataBean.getAdvice() + "");    // 建议
            caseDBBean.setUserName(mDataBean.getUserName() + "");    // 操作员用户名
            caseDBBean.setRecord_date(mDataBean.getRecord_date() + "");    // 创建时间
            caseDBBean.setImagesCount(mDataBean.getImageCount() + "");    // 图片数量
//          caseDBBean.setVideosCount(mDataBean.getVid() + "");    // 视频数量
            caseDBBean.setSubmitDoctor(mDataBean.getSubmitDoctor() + "");    //送检医生
            caseDBBean.setRace(mDataBean.getRace() + "");    // 民族种族
            caseDBBean.setRecordType(mDataBean.getRecordType() + "");    // 病例类型
            caseDBBean.setUpdate_time(mDataBean.getUpdate_time() + "");    // 更新时间
            caseDBBean.setPatientAge(mDataBean.getPatientAge() + "");    // 患者年龄
            caseDBBean.setCardID(mDataBean.getCardID() + "");    // 身份证号
            caseDBBean.setTel(mDataBean.getTel() + "");    // 电话

            //2022-03-23 08:13:16
            String check_date = mBean.getData().getCheck_date();
            int i = check_date.indexOf(" ");
            //获取到需要在下载病例的时候,需要存入时间的正确值 2022-03-23
            mCurrentDonwTime = check_date.substring(0, i);

            caseDBBean.setCheck_date(mCurrentDonwTime + "");    // 检查时间,也是下载的时候当前时间的标识!!!
            caseDBBean.setPatientNo(mDataBean.getPatientNo() + "");    // 病人编号
            caseDBBean.setInpatientID(mDataBean.getInpatientID() + "");    // 住院号
            caseDBBean.setBedID(mDataBean.getBedID() + "");    // 病床号
            caseDBBean.setCheckContent(mDataBean.getCheckContent() + "");    // 检查内容（镜检所见）
            caseDBBean.setReturnVisit(mDataBean.isReturnVisit() + "");    // 初复诊 (0-初诊 1-复诊)
            caseDBBean.setCaseNo(mDataBean.getCaseNo() + "");    // 病例编号
            caseDBBean.setCtology(mDataBean.getCtology() + "");    // 细胞学
            caseDBBean.setDOB(mDataBean.getDOB() + "");    // 生日
            caseDBBean.setExaminingPhysician(mDataBean.getExaminingPhysician() + "");    // 检查医生
            caseDBBean.setCheckDiagnosis(mDataBean.getCheckDiagnosis() + "");    // 镜检诊断
            caseDBBean.setSex(mDataBean.getSex() + "");    // 性别
            caseDBBean.setEndoType(mDataBean.getEndoType() + "");    // 工作站类型
            caseDBBean.setDevice(mDataBean.getDevice() + "");    // 设备
            caseDBBean.setIsInHospital(mDataBean.isIsInHospital() + "");    // 是否还在医院住院
            caseDBBean.setMarried(mDataBean.getMarried() + "");    // 婚否
            caseDBBean.setFamilyHistory(mDataBean.getFamilyHistory() + "");    // 家族病史
            caseDBBean.setTest(mDataBean.getTest() + "");    // 试验
            caseDBBean.setClinicalDiagnosis(mDataBean.getClinicalDiagnosis() + "");    // 临床诊断
            caseDBBean.setDepartment(mDataBean.getDepartment() + "");    // 科室
            caseDBBean.setWardID(mDataBean.getWardID() + "");    // 病区号
            caseDBBean.setCaseID(mDataBean.getCaseID() + "");    // 病例号
            caseDBBean.setName(mDataBean.getName() + "");    // 姓名
            caseDBBean.setAddress(mDataBean.getAddress() + "");    // 住址
            caseDBBean.setInsuranceID(mDataBean.getInsuranceID() + "");    // 社保卡号
            CaseDBUtils.insertOrReplaceInTx(getActivity(), caseDBBean);

        } else {
            /**
             * 存在就更新
             */
            CaseDBBean currentDBBean = myList.get(0);
            //设置id,就是执行update的操作
            caseDBBean.setId(currentDBBean.getId());
            //设置是否下载过的标识 ==上位机返回的ID
            caseDBBean.setOthers(mDataBean.getID() + "");
            caseDBBean.setDeviceCaseID(mDeviceCode + "");  //用户表和设备表进行绑定, //用户表和设备表进行绑定, //用户表和设备表进行绑定
            caseDBBean.setOccupatior(mDataBean.getOccupatior() + "");// 职业
            caseDBBean.setNativePlace(mDataBean.getAddress() + "");    //籍贯
            caseDBBean.setFee(mDataBean.getFee() + "");    //收费
            caseDBBean.setChiefComplaint(mDataBean.getChiefComplaint() + "");  //主诉
            //        图片路径集合--文件夹（设备ID_病例ID）
            //      /storage/emulated/0/MyDownImages/2_3/004.jpg
            //      LogUtils.e("下载图片==更新相册图片==" + toLocalFile.getAbsolutePath() + "/" + pictureName);       本地存的路径
            //     toLocalFile.getAbsolutePath()==/storage/emulated/0/MyDownImages/0000000000000000546017FE6BC28949_1154
            //     pictureName==001.jpg
            ArrayList<CaseImageListBean> caseImageList = new ArrayList<CaseImageListBean>();
            if (null != mPathMap && !mPathMap.isEmpty()) {
                for (String key : mPathMap.keySet()) {
                    //001.jpg     http://192.168.131.43:7001/1154/001.jpg
                    int size = mPathMap.keySet().size();
//                sendPictureRequest(toLocalFile, mPathMap.get(key), key, false);
                    CaseImageListBean imageBean = new CaseImageListBean();
                    // /storage/emulated/0/MyDownImages/0000000000000000546017FE6BC28949_1154/001.jpg
                    imageBean.setImagePath(toLocalFile.getAbsolutePath() + "/" + key);  //存入本地存储路径
                    caseImageList.add(imageBean);
                }
                caseDBBean.setImageList(caseImageList);    //图片路径集合--文件夹（设备ID-病例ID）
            }


            //不管存在不存在,都需要校验和存储被下载着名单列表
            //当前情况是病例被下载过,所以需要校验被下载着名单,是否有当前登入用户,有就不操作,没有就存入,下载者名单列表中
            List<DownloadedNameListBean> mDownloadNameList = currentDBBean.getDownloadedNameList();
            DownloadedNameListBean tagBean = new DownloadedNameListBean();
            tagBean.setDownloadedByName(mLoginUserName);
            if (mDownloadNameList.size() != 0) {
                //下载者名单中没有包含了,此用户
                boolean contains = mDownloadNameList.contains(tagBean);
                if (!contains) {
                    DownloadedNameListBean nameBean = new DownloadedNameListBean();
                    nameBean.setDownloadedByName(mLoginUserName);
                    mDownloadNameList.add(nameBean);
                    caseDBBean.setDownloadedNameList(mDownloadNameList);
                } else {
                    caseDBBean.setDownloadedNameList(mDownloadNameList);
                }
            }


            caseDBBean.setBiopsy(mDataBean.getBiopsy() + "");    //活检
            caseDBBean.setPathology(mDataBean.getPathology() + "");    //病理学
            caseDBBean.setFeeType(mDataBean.getFeeType() + "");    //收费类型
            caseDBBean.setMedHistory(mDataBean.getMedHistory() + "");    // 医疗病史
            caseDBBean.setLastCheckUserID(mDataBean.getLastCheckUserID() + "");    // 最后一个来查房的医生
            caseDBBean.setAgeUnit(mDataBean.getAgeUnit() + "");    // 年龄单位
            caseDBBean.setAdvice(mDataBean.getAdvice() + "");    // 建议
            caseDBBean.setUserName(mDataBean.getUserName() + "");    // 操作员用户名
            caseDBBean.setRecord_date(mDataBean.getRecord_date() + "");    // 创建时间
            caseDBBean.setImagesCount(mDataBean.getImageCount() + "");    // 图片数量
//        caseDBBean.setVideosCount(mDataBean.getVid() + "");    // 视频数量
            caseDBBean.setSubmitDoctor(mDataBean.getSubmitDoctor() + "");    //送检医生
            caseDBBean.setRace(mDataBean.getRace() + "");    // 民族种族
            caseDBBean.setRecordType(mDataBean.getRecordType() + "");    // 病例类型
            caseDBBean.setUpdate_time(mDataBean.getUpdate_time() + "");    // 更新时间
            caseDBBean.setPatientAge(mDataBean.getPatientAge() + "");    // 患者年龄
            caseDBBean.setCardID(mDataBean.getCardID() + "");    // 身份证号
            caseDBBean.setTel(mDataBean.getTel() + "");    // 电话

            //2022-03-23 08:13:16
            String check_date = mBean.getData().getCheck_date();
            int i = check_date.indexOf(" ");
            //获取到需要在下载病例的时候,需要存入时间的正确值 2022-03-23
            mCurrentDonwTime = check_date.substring(0, i);

            caseDBBean.setCheck_date(mCurrentDonwTime + "");    // 检查时间,也是下载的时候当前时间的标识!!!
            caseDBBean.setPatientNo(mDataBean.getPatientNo() + "");    // 病人编号
            caseDBBean.setInpatientID(mDataBean.getInpatientID() + "");    // 住院号
            caseDBBean.setBedID(mDataBean.getBedID() + "");    // 病床号
            caseDBBean.setCheckContent(mDataBean.getCheckContent() + "");    // 检查内容（镜检所见）
            caseDBBean.setReturnVisit(mDataBean.isReturnVisit() + "");    // 初复诊 (0-初诊 1-复诊)
            caseDBBean.setCaseNo(mDataBean.getCaseNo() + "");    // 病例编号
            caseDBBean.setCtology(mDataBean.getCtology() + "");    // 细胞学
            caseDBBean.setDOB(mDataBean.getDOB() + "");    // 生日
            caseDBBean.setExaminingPhysician(mDataBean.getExaminingPhysician() + "");    // 检查医生
            caseDBBean.setCheckDiagnosis(mDataBean.getCheckDiagnosis() + "");    // 镜检诊断
            caseDBBean.setSex(mDataBean.getSex() + "");    // 性别
            caseDBBean.setEndoType(mDataBean.getEndoType() + "");    // 工作站类型
            caseDBBean.setDevice(mDataBean.getDevice() + "");    // 设备
            caseDBBean.setIsInHospital(mDataBean.isIsInHospital() + "");    // 是否还在医院住院
            caseDBBean.setMarried(mDataBean.getMarried() + "");    // 婚否
            caseDBBean.setFamilyHistory(mDataBean.getFamilyHistory() + "");    // 家族病史
            caseDBBean.setTest(mDataBean.getTest() + "");    // 试验
            caseDBBean.setClinicalDiagnosis(mDataBean.getClinicalDiagnosis() + "");    // 临床诊断
            caseDBBean.setDepartment(mDataBean.getDepartment() + "");    // 科室
            caseDBBean.setWardID(mDataBean.getWardID() + "");    // 病区号
            caseDBBean.setCaseID(mDataBean.getCaseID() + "");    // 病例号
            caseDBBean.setName(mDataBean.getName() + "");    // 姓名
            caseDBBean.setAddress(mDataBean.getAddress() + "");    // 住址
            caseDBBean.setInsuranceID(mDataBean.getInsuranceID() + "");    // 社保卡号
            CaseDBUtils.insertOrReplaceInTx(getActivity(), caseDBBean);
        }
        String userLoginUserName = mLoginUserName;
        String userLoginPassword = (String) SharePreferenceUtil.get(getAttachActivity(), SharePreferenceUtil.Current_Login_Password, "");
        String mLoginReol = (String) SharePreferenceUtil.get(getAttachActivity(), SharePreferenceUtil.Current_Login_Role, "");
        Boolean isRemember = (Boolean) SharePreferenceUtil.get(getAttachActivity(), SharePreferenceUtil.Current_Login_Remember_Password, true);
        String mLoginUserID = (String) SharePreferenceUtil.get(getAttachActivity(), SharePreferenceUtil.Current_Login_UserID, "1");

        /**
         * 创建需要下载的本地病例--->用户表
         */
        //设备码和当前操作用户绑定
        //查询当前设备码下 绑定的所有用户
        //查询当前设备码下绑定的用户,并且是操作用户是当前登入的用户
        List<UserDBBean> userListt = UserDBUtils.getQueryBeanByThree(getAttachActivity(), mDeviceCode, mLoginUserID, "true");
//        List<UserDBBean> userListt = UserDBUtils.getQueryBeanByTowCodeUserID(getAttachActivity(), mDeviceCode, mLoginUserID,"true");

        if (null != userListt && userListt.size() > 0) {
            UserDBBean userDBBean = userListt.get(0);
            //有数据,更新
            userDBBean.setId(userDBBean.getId());
            userDBBean.setDeviceID(mDeviceCode + "");
            userDBBean.setDeviceUserID(mLoginUserID + "");  //设置用户ID
            userDBBean.setUserName(userDBBean.getUserName() + "");
            userDBBean.setPassword(userDBBean.getPassword() + "");
            userDBBean.setRelo(userDBBean.getRelo() + "");
            userDBBean.setMake01(userDBBean.getMake01() + "");
            userDBBean.setIsRememberPassword(userDBBean.getIsRememberPassword());
            UserDBUtils.insertOrReplaceInTx(getAttachActivity(), userDBBean);

        } else {
            //无数据,新增
            UserDBBean bean = new UserDBBean();
            bean.setDeviceID(mDeviceCode + "");
            bean.setDeviceUserID(mLoginUserID + "");//设置用户ID
            bean.setUserName(userLoginUserName);
            bean.setPassword(userLoginPassword);
            bean.setRelo(mLoginReol + "");
            bean.setMake01("true");  //设置为被下载状态
            bean.setIsRememberPassword(isRemember);
            UserDBUtils.insertOrReplaceInTx(getAttachActivity(), bean);
        }

        //发送消息刷新界面
        mHandler.sendEmptyMessage(DOWN_STATUE_OK);


    }


    private void requestPermission() {
        XXPermissions.with(this)
                // 不适配 Android 11 可以这样写
//                .permission(Permission.Group.STORAGE)
                // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
//                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            startDownPictureCase();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            toast("被永久拒绝授权，请手动授予存储权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getActivity(), permissions);
                        } else {
                            toast("获取存储权限失败");
                        }
                    }
                });

    }

    @Override
    public void onDelete() {
        if (mMMKVInstace.decodeBool(Constants.KEY_CanDelete)) {
            new MessageDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage("确认删除该病历吗?")
                    .setConfirm("确定")
                    .setCancel("取消")
                    .setListener(new MessageDialog.OnListener() {
                        @Override
                        public void onConfirm(BaseDialog dialog) {
                            sendDeleteRequest();
                        }
                    }).show();
        } else {
            toast(Constants.HAVE_NO_PERMISSION);
        }
    }


    @Override
    public void onGetReport() {


    }

    @Override
    public void onGetPicture() {

    }

    /**
     * 病例被删除,删除当前下载过的记录
     */
    private void deleteCurrentDownMsg() {
        // /storage/emulated/0/MyDownVideos/0000000000000000546017FE6BC28949_1195/720220424151314404.mp4
        //查询当前设备码下,当前病例详情ID(mDataBean.getID()),之下的病例,有就是下载过,没有就是没下载过,数字长度0或者1
        List<CaseDBBean> myList = CaseDBUtils.getQueryBeanByTow02(getApplication(), mDeviceCode, mDataBean.getID() + "");
        if (myList.size() != 0) {
            //删除数据库中的这条病例
            CaseDBUtils.delete(getAttachActivity(), myList.get(0));
        }
        /**
         * 再删除本地图片
         * 本地文件夹命名规则:文件夹（设备ID_病例ID）
         */
        //创建本地的/MyDownImages/mID文件夹  再把图片下载到这个文件夹下  文件夹（设备ID-病例ID）
        String dirNameImages = Environment.getExternalStorageDirectory() + "/MyDownImages/" + mDeviceCode + "_" + getCurrentItemID();
        String dirNameVideos = Environment.getExternalStorageDirectory() + "/MyDownVideos/" + mDeviceCode + "_" + getCurrentItemID();
        //删除本地图片文件夹以及图片
        File mImagesFile = new File(dirNameImages);
        if (mImagesFile.exists()) {
            FileUtil.deleteSDFile(dirNameImages, true);
        }
        //删除本地视频文件夹以及视频
        File mVideoFile = new File(dirNameImages);
        if (mVideoFile.exists()) {
            FileUtil.deleteSDFile(dirNameVideos, true);
        }

    }

    /**
     * 删除用户请求
     * 成功回调之后,需要删除,数据库表中病例表,和已缓存的图片和视频(删除-MyDownImages和MyDownVideos两个文件夹下的code_caseID)
     * /storage/emulated/0/MyDownImages/0000000000000000546017FE6BC28949_1195/ 视频文件夹
     * /storage/emulated/0/MyDownVideos/0000000000000000546017FE6BC28949_1195/ 图片文件夹
     */
    private void sendDeleteRequest() {
        mLoginUserName = mMMKVInstace.decodeString(Constants.KEY_CurrentLoginUserName);
        if (Details_Reault_Ok) {
            showLoading();
            OkHttpUtils.post()
                    .url(mBaseUrl + HttpConstant.CaseManager_DeleteCase)
                    .addParams("ID", mBean.getData().getID() + "")
                    .addParams("UserName", mLoginUserName)
                    .addParams("EndoType", endoType)
                    .addParams("UserID", mUserID)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            showError(listener -> {
                                sendDeleteRequest();
                            });
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if ("" != response) {
                                DeleteBean mBean = mGson.fromJson(response, DeleteBean.class);
                                if (0 == mBean.getCode()) {  //成功
                                    showComplete();
                                    sendSocketPointMessage(Constants.UDP_14);
                                    deleteCurrentDownMsg();
                                    mActivity.finish();

                                } else {
                                    showError(listener -> {
                                        sendDeleteRequest();
                                    });
                                }
                            } else {
                                showError(listener -> {
                                    sendDeleteRequest();
                                });
                            }
                        }
                    });
        } else {
            toast("稍后在尝试删除");
        }

    }


    private void responseListener() {


        //年纪类别的List数据本地写:岁,月,天,
        setOnClickListener(R.id.iv_01_sex_type, R.id.iv_01_age_type, R.id.iv_01_jop, R.id.tv_01_get_check_doctor,
                R.id.iv_02_mirror_see, R.id.iv_02_mirror_result, R.id.iv_02_live_check, R.id.iv_02_cytology, R.id.iv_02_test, R.id.iv_02_pathology,
                R.id.iv_02_advice, R.id.iv_02_check_doctor, R.id.iv_03_section, R.id.iv_03_device, R.id.iv_03_ming_zu, R.id.iv_03_is_married);

//        setOnClickListener(R.id.et_01_sex_type, R.id.tv_01_age_type, R.id.et_01_jop, R.id.et_01_get_check_doctor,
//                R.id.et_02_mirror_see, R.id.et_02_mirror_result, R.id.et_02_live_check, R.id.et_02_cytology, R.id.et_02_test, R.id.et_02_pathology,
//                R.id.et_02_advice, R.id.et_02_check_doctor, R.id.et_03_section, R.id.et_03_device, R.id.et_03_ming_zu, R.id.et_03_is_married);

//        iv_01_i_tell_you.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(lines_01_i_tell_you, "11",iv_01_i_tell_you);
//
//            }
//        });
//        iv_01_bad_tell.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(lines_01_bad_tell, "12", iv_01_bad_tell);
//
//            }
//        });
//
//        //02-layout
//
//        iv_02_mirror_see.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_mirror_see, "13", iv_02_mirror_see);
//
//            }
//        });
//        iv_02_mirror_result.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_mirror_result, "14", iv_02_mirror_result);
//
//            }
//        });
//        iv_02_live_check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_live_check, "15", iv_02_live_check);
//
//            }
//        });
//        iv_02_cytology.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_cytology, "16", iv_02_cytology);
//
//            }
//        });
//        iv_02_test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_test, "17", iv_02_test);
//
//            }
//        });
//        iv_02_pathology.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_pathology, "18", iv_02_pathology);
//
//            }
//        });
//        iv_02_advice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showITellyouMenuDialog(etlines_02_advice, "19", iv_02_advice);
//
//            }
//        });


        //01--layout
        iv_01_i_tell_you.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_01_i_tell_you);
                showITellyouMenuDialog(lines_01_i_tell_you, "11", iv_01_i_tell_you);

            }
        });
        iv_01_bad_tell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_01_bad_tell);

                showITellyouMenuDialog(lines_01_bad_tell, "12", iv_01_bad_tell);

            }
        });
        //02-layout

        iv_02_mirror_see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_mirror_see);

                showITellyouMenuDialog(etlines_02_mirror_see, "13", iv_02_mirror_see);

            }
        });
        iv_02_mirror_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_mirror_result);

                showITellyouMenuDialog(etlines_02_mirror_result, "14", iv_02_mirror_result);

            }
        });
        iv_02_live_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_live_check);
                showITellyouMenuDialog(etlines_02_live_check, "15", iv_02_live_check);

            }
        });
        iv_02_cytology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_cytology);
                showITellyouMenuDialog(etlines_02_cytology, "16", iv_02_cytology);

            }
        });
        iv_02_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_test);
                showITellyouMenuDialog(etlines_02_test, "17", iv_02_test);

            }
        });
        iv_02_pathology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_pathology);
                showITellyouMenuDialog(etlines_02_pathology, "18", iv_02_pathology);

            }
        });
        iv_02_advice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogIconAnim(true, iv_02_advice);
                showITellyouMenuDialog(etlines_02_advice, "19", iv_02_advice);

            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_01_sex_type:  //性别
                startDialogIconAnim(true, iv_01_sex_type);

                showMenuDialog(et_01_sex_type, "100", iv_01_sex_type);
                break;
            case R.id.iv_01_age_type:  //年龄类别  --本地写数据List
                startDialogIconAnim(true, iv_01_age_type);
                showMenuElseDialog();
                break;
            case R.id.iv_01_jop:        //职业
                startDialogIconAnim(true, iv_01_jop);
                showMenuDialog(et_01_jop, "5", iv_01_jop);
                break;
            case R.id.tv_01_get_check_doctor://送检医生
                startDialogIconAnim(true, tv_01_get_check_doctor);
                showMenuDialog(et_01_get_check_doctor, "8", tv_01_get_check_doctor);
                break;
            case R.id.et_01_i_tell_you:    //主诉--带字数限制的
//                showITellyouMenuDialog(et_01_i_tell_you, "11");
                break;
            case R.id.et_01_bad_tell:     //临床诊断--带字数限制的
//                showITellyouMenuDialog(et_01_bad_tell, "12");
                break;
//            case R.id.et_02_mirror_see:   //镜检所见
//                showMenuDialog(etlines_02_mirror_see, "13");
//                break;
//            case R.id.et_02_mirror_result://镜检诊断
//                showMenuDialog(etlines_02_mirror_result, "14");
//                break;
//            case R.id.et_02_live_check://活检
//                showMenuDialog(etlines_02_live_check, "15");
//                break;
//            case R.id.et_02_cytology://细胞学
//                showMenuDialog(etlines_02_cytology, "16");
//                break;
//            case R.id.et_02_test://试验
//                showMenuDialog(etlines_02_test, "17");
//                break;
//            case R.id.et_02_pathology://病理学
//                showMenuDialog(etlines_02_pathology, "18");
//                break;
//            case R.id.et_02_advice://建议
//                showMenuDialog(etlines_02_advice, "19");
//                break;
            case R.id.iv_02_check_doctor://检查医生
                startDialogIconAnim(true, iv_02_check_doctor);
                showMenuDialog(et_02_check_doctor, "20", iv_02_check_doctor);
                break;
            case R.id.iv_03_section: //科室
                startDialogIconAnim(true, iv_03_section);
                showMenuDialog(et_03_section, "9", iv_03_section);
                break;
            case R.id.iv_03_device://设备
                startDialogIconAnim(true, iv_03_device);
                showMenuDialog(et_03_device, "10", iv_03_device);
                break;
            case R.id.iv_03_ming_zu://民族
                startDialogIconAnim(true, iv_03_ming_zu);
                showMenuDialog(et_03_ming_zu, "23", iv_03_ming_zu);
                break;
            case R.id.iv_03_is_married://婚否
                startDialogIconAnim(true, iv_03_is_married);
                showMenuDialog(et_03_is_married, "101", iv_03_is_married);
                break;

        }
    }


    private void showMenuElseDialog() {
        // 底部选择框
        if (mFragClickable && mEditStatus) {//有dialog数据,集合不为空,可编辑状态
            new MenuDialog.Builder(getActivity())
                    // 设置 null 表示不显示取消按钮
                    //.setCancel(getString(R.string.common_cancel))
                    // 设置点击按钮后不关闭对话框
                    //.setAutoDismiss(false)
                    .setList(ageList)
                    .addOnDismissListener(new BaseDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(BaseDialog dialog) {
                            startDialogIconAnim(false, iv_01_age_type);

                        }
                    })
                    .setListener(new MenuDialog.OnListener<String>() {

                        @Override
                        public void onSelected(BaseDialog dialog, int position, String string) {
                            tv_01_age_type.setText("" + ageList.get(position));
                            startDialogIconAnim(false, iv_01_age_type);
                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {
                            startDialogIconAnim(false, iv_01_age_type);
                        }
                    })
                    .show();
        }

    }

    private void showITellyouMenuDialog(LinesEditView mEdit, String key, ImageView mView) {
        if (mFragClickable && null != mDialogItemMap && mEditStatus) {//有dialog数据,集合不为空,可编辑状态
            ArrayList<DialogItemBean> mDataList = (ArrayList<DialogItemBean>) mDialogItemMap.get(key);
            ArrayList<String> stringList = new ArrayList<>();
            for (int i = 0; i < mDataList.size(); i++) {
                stringList.add(mDataList.get(i).getDictItem());
            }
            // 底部选择框
            new MenuDialog.Builder(getActivity())
                    // 设置 null 表示不显示取消按钮
                    //.setCancel(getString(R.string.common_cancel))
                    // 设置点击按钮后不关闭对话框
                    //.setAutoDismiss(false)
                    .setList(stringList)
                    .addOnDismissListener(new BaseDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(BaseDialog dialog) {
                            startDialogIconAnim(false, mView);
                        }
                    })
                    .setListener(new MenuDialog.OnListener<String>() {
                        @Override
                        public void onSelected(BaseDialog dialog, int position, String string) {
                            String s = stringList.get(position);
                            String s1 = mEdit.getContentText() + "" + s;
                            startDialogIconAnim(false, mView);
                            mEdit.setContentText(mEdit.getContentText() + "" + s);


                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {
                            startDialogIconAnim(false, mView);
                        }
                    })
                    .show();
        }
    }

    private void showMenuDialog(ClearEditText mEdit, String key, ImageView mAnimView) {
        if (mFragClickable && null != mDialogItemMap && mEditStatus) {//有dialog数据,集合不为空,可编辑状态
            ArrayList<DialogItemBean> mDataList = (ArrayList<DialogItemBean>) mDialogItemMap.get(key);
            ArrayList<String> stringList = new ArrayList<>();
            for (int i = 0; i < mDataList.size(); i++) {
                stringList.add(mDataList.get(i).getDictItem());
            }
            // 底部选择框
            new MenuDialog.Builder(getActivity())
                    // 设置 null 表示不显示取消按钮
                    //.setCancel(getString(R.string.common_cancel))
                    // 设置点击按钮后不关闭对话框
                    //.setAutoDismiss(false)
                    .setList(stringList)
                    .addOnDismissListener(new BaseDialog.OnDismissListener() {
                        @Override
                        public void onDismiss(BaseDialog dialog) {
                            startDialogIconAnim(false, mAnimView);

                        }
                    })
                    .setListener(new MenuDialog.OnListener<String>() {

                        @Override
                        public void onSelected(BaseDialog dialog, int position, String data) {
                            String s = stringList.get(position);
                            startDialogIconAnim(false, mAnimView);
                            mEdit.setText("" + s);

                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {

                            startDialogIconAnim(false, mAnimView);

                        }
                    })
                    .show();
        }

    }


    /**
     * 获取需要Dialog选择数据的集合
     */
    private void sendListDictsRequest() {
        //获取Dialog item的数据
        OkHttpUtils.get()
                .url(mBaseUrl + HttpConstant.CaseManager_CaseDialogDate)
                .addParams("EndoType", endoType)
//                .addParams("EndoType", "4")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        mFragClickable = false;
                    }

                    @SuppressLint("NewApi")
                    @Override
                    public void onResponse(String response, int id) {
                        ListDialogDateBean mBean = mGson.fromJson(response, ListDialogDateBean.class);
                        //1,按'DictName'进行分组
                        //2,按照分组中找出 'ParentId=0' 的项,作为每个分组的类别
                        List<ListDialogDateBean.DataDTO.ListDictsDTO> listDicts = mBean.getData().getListDicts();
                        //创建一个map  key是 DictName,value是list
                        mDialogItemMap = new HashMap<String, ArrayList<DialogItemBean>>();
                        for (int i = 0; i < listDicts.size(); i++) {
                            //获取每条数据的dictname
                            String currentDictName = listDicts.get(i).getDictName();  //100
                            //再次遍历这个集合,和currentDictName  相同的bean全部存入集合中
                            ArrayList<DialogItemBean> itemBeanList = new ArrayList<>();
                            for (int j = 0; j < listDicts.size(); j++) {
                                ListDialogDateBean.DataDTO.ListDictsDTO listDictsDTO = listDicts.get(j);
                                String dictName = listDictsDTO.getDictName();
                                if (currentDictName.equals(dictName)) {
                                    DialogItemBean itemBean = new DialogItemBean();
                                    itemBean.setID(listDictsDTO.getID());
                                    itemBean.setParentId(listDictsDTO.getParentId());
                                    itemBean.setDictItem(listDictsDTO.getDictItem());
                                    itemBean.setEndoType(listDictsDTO.getEndoType());
                                    itemBeanList.add(itemBean);
                                }
                            }
                            if (!itemBeanList.isEmpty()) {
                                boolean currentDictName1 = mDialogItemMap.containsKey(currentDictName);
                                if (!currentDictName1) {
                                    mDialogItemMap.put(currentDictName, itemBeanList);
                                }
                            }
                            mFragClickable = true;

                        }
//
//                        Iterator<Map.Entry<String, ArrayList<DialogItemBean>>> entries = mDialogItemMap.entrySet().iterator();
//                        while (entries.hasNext()) {
//                            Map.Entry<String, ArrayList<DialogItemBean>> entry = entries.next();
//                            String key = entry.getKey();
//                            ArrayList<DialogItemBean> value = entry.getValue();
//                            LogUtils.e("对话框数据====key====" + key);
//
//                            for (int i = 0; i < value.size(); i++) {
//                                DialogItemBean bean = value.get(i);
//                                LogUtils.e("对话框数据====value====" + bean.getDictItem());
//                            }
//                        }
                    }
                });

    }


    private void initLayoutViewDate() {

        ageList = new ArrayList<>();
        ageList.add("岁");
        ageList.add("月");
        ageList.add("天");
        /**
         * 获取基本信息id
         */
        //检查号
        et_01_check_num = findViewById(R.id.et_01_check_num);
        //姓名
        et_01_name = findViewById(R.id.et_01_name);
        //性别
        et_01_sex_type = findViewById(R.id.et_01_sex_type);
        //年龄
        et_01_age = findViewById(R.id.et_01_age);
        //年龄类别-弹窗选择
        tv_01_age_type = findViewById(R.id.tv_01_age_type);
        //职业
        et_01_jop = findViewById(R.id.et_01_jop);
        //职业
        et_01_fee = findViewById(R.id.et_01_fee);
        //送检医生
        et_01_get_check_doctor = findViewById(R.id.et_01_get_check_doctor);
        //主诉
        lines_01_i_tell_you = findViewById(R.id.et_01_i_tell_you);
        //临床诊断
        lines_01_bad_tell = findViewById(R.id.et_01_bad_tell);

        //主诉---多行显示的edit
        edit_01_i_tell_you = lines_01_i_tell_you.getContentEdit();
        edit_01_i_tell_you.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        //临床诊断---多行显示的edit
        edit_01_i_bad_tell = lines_01_bad_tell.getContentEdit();
        edit_01_i_tell_you.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});


        //获取点击图标弹出dialog
        iv_01_sex_type = findViewById(R.id.iv_01_sex_type);
        iv_01_age_type = findViewById(R.id.iv_01_age_type);
        iv_01_jop = findViewById(R.id.iv_01_jop);
        tv_01_get_check_doctor = findViewById(R.id.tv_01_get_check_doctor);
        iv_01_i_tell_you = findViewById(R.id.iv_01_i_tell_you);
        iv_01_bad_tell = findViewById(R.id.iv_01_bad_tell);


        /**
         *获取镜信息id
         */
        //镜检所见
        etlines_02_mirror_see = findViewById(R.id.et_02_mirror_see);
        //镜检诊断
        etlines_02_mirror_result = findViewById(R.id.et_02_mirror_result);
        //活检
        etlines_02_live_check = findViewById(R.id.et_02_live_check);
        //细胞学
        etlines_02_cytology = findViewById(R.id.et_02_cytology);
        //试验
        etlines_02_test = findViewById(R.id.et_02_test);
        //病理学
        etlines_02_pathology = findViewById(R.id.et_02_pathology);
        //建议
        etlines_02_advice = findViewById(R.id.et_02_advice);
        //检查医生
        et_02_check_doctor = findViewById(R.id.et_02_check_doctor);

        et_02_mirror_see = etlines_02_mirror_see.getContentEdit();
        et_02_mirror_result = etlines_02_mirror_result.getContentEdit();
        et_02_live_check = etlines_02_live_check.getContentEdit();
        et_02_cytology = etlines_02_cytology.getContentEdit();
        et_02_test = etlines_02_test.getContentEdit();
        et_02_pathology = etlines_02_pathology.getContentEdit();
        et_02_advice = etlines_02_advice.getContentEdit();


        et_02_mirror_see.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        et_02_mirror_result.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        et_02_live_check.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        et_02_cytology.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        et_02_test.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        et_02_pathology.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        et_02_advice.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        //获取点击图标弹出dialog
        iv_02_mirror_see = findViewById(R.id.iv_02_mirror_see);
        iv_02_mirror_result = findViewById(R.id.iv_02_mirror_result);
        iv_02_live_check = findViewById(R.id.iv_02_live_check);
        iv_02_cytology = findViewById(R.id.iv_02_cytology);
        iv_02_test = findViewById(R.id.iv_02_test);
        iv_02_pathology = findViewById(R.id.iv_02_pathology);
        iv_02_advice = findViewById(R.id.iv_02_advice);
        iv_02_check_doctor = findViewById(R.id.iv_02_check_doctor);


        /**
         * 获取其他信息id
         */
        //门诊号
        et_03_door_num = findViewById(R.id.et_03_door_num);
        //医保号
        et_03_protection_num = findViewById(R.id.et_03_protection_num);
        //科室
        et_03_section = findViewById(R.id.et_03_section);
        //设备
        et_03_device = findViewById(R.id.et_03_device);
        //病例号
        et_03_case_num = findViewById(R.id.et_03_case_num);
        //住院号
        et_03_in_hospital_num = findViewById(R.id.et_03_in_hospital_num);
        //病区号
        et_03_case_area_num = findViewById(R.id.et_03_case_area_num);
        //病床号
        et_03_case_bed_num = findViewById(R.id.et_03_case_bed_num);
        //籍贯
        et_03_native_place = findViewById(R.id.et_03_native_place);
        //民族
        et_03_ming_zu = findViewById(R.id.et_03_ming_zu);
        //婚否
        et_03_is_married = findViewById(R.id.et_03_is_married);
        //电话
        et_03_tel = findViewById(R.id.et_03_tel);
        //住址
        et_03_address = findViewById(R.id.et_03_address);
        //身份证
        et_03_my_id_num = findViewById(R.id.et_03_my_id_num);
        //病史
        lines_03_case_history = findViewById(R.id.et_03_case_history);
        //家族病史
        lines_03_family_case_history = findViewById(R.id.et_03_family_case_history);

        edit_03_case_history = lines_03_case_history.getContentEdit();
        edit_03_family_case_history = lines_03_family_case_history.getContentEdit();
        edit_03_case_history.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        edit_03_family_case_history.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
        //获取点击图标弹出dialog
        iv_03_section = findViewById(R.id.iv_03_section);
        iv_03_device = findViewById(R.id.iv_03_device);
        iv_03_ming_zu = findViewById(R.id.iv_03_ming_zu);
        iv_03_is_married = findViewById(R.id.iv_03_is_married);


        mEditList = new ArrayList<>();
        mLineEditList = new ArrayList<>();   //不能获取焦点的edit
        mImageViewList = new ArrayList<>();         //点击弹出对话框的imageview
        //存入02
        mImageViewList.add(iv_02_mirror_see);
        mImageViewList.add(iv_02_mirror_result);
        mImageViewList.add(iv_02_live_check);
        mImageViewList.add(iv_02_cytology);
        mImageViewList.add(iv_02_test);
        mImageViewList.add(iv_02_pathology);
        mImageViewList.add(iv_02_advice);
        mImageViewList.add(iv_02_check_doctor);
        //存入03
        mImageViewList.add(iv_03_section);
        mImageViewList.add(iv_03_device);
        mImageViewList.add(iv_03_ming_zu);
        mImageViewList.add(iv_03_is_married);
        //存入01
        mImageViewList.add(iv_01_age_type);
        mImageViewList.add(iv_01_jop);
        mImageViewList.add(tv_01_get_check_doctor);
        mImageViewList.add(iv_01_i_tell_you);
        mImageViewList.add(iv_01_bad_tell);
        mImageViewList.add(iv_01_sex_type);

        mEditList.add(edit_01_i_tell_you);
        mEditList.add(edit_01_i_bad_tell);
        mEditList.add(et_02_mirror_see);
        mEditList.add(et_02_mirror_result);
        mEditList.add(et_02_live_check);
        mEditList.add(et_02_cytology);
        mEditList.add(et_02_test);
        mEditList.add(et_02_pathology);
        mEditList.add(et_02_advice);
        mEditList.add(et_02_check_doctor);
        mEditList.add(et_03_door_num);

        mEditList.add(et_03_protection_num);
        mEditList.add(edit_03_case_history);
        mEditList.add(edit_03_family_case_history);
        mEditList.add(et_03_section);
        mEditList.add(et_03_device);
        mEditList.add(et_03_case_num);
        mEditList.add(et_03_in_hospital_num);
        mEditList.add(et_03_case_area_num);
        mEditList.add(et_03_case_bed_num);
        mEditList.add(et_03_native_place);
        mEditList.add(et_03_ming_zu);
//        mEditList.add(et_03_is_married);
        mEditList.add(et_03_tel);
        mEditList.add(et_03_address);
        mEditList.add(et_03_my_id_num);
        mEditList.add(et_01_check_num);
        mEditList.add(et_01_name);


        mEditList.add(et_01_age);//et_01_age
        mEditList.add(et_01_jop);
        mEditList.add(et_01_fee);
        mEditList.add(et_01_get_check_doctor);
        mEditList.add(et_01_sex_type);


        //需要先获取到lines然后再去获取edit
        mLineEditList.add(lines_01_bad_tell);
        mLineEditList.add(etlines_02_mirror_see);
        mLineEditList.add(etlines_02_mirror_result);
        mLineEditList.add(etlines_02_live_check);
        mLineEditList.add(etlines_02_cytology);
        mLineEditList.add(etlines_02_test);
        mLineEditList.add(etlines_02_pathology);
        mLineEditList.add(etlines_02_advice);
        mLineEditList.add(lines_03_case_history);
        mLineEditList.add(lines_03_family_case_history);
        mLineEditList.add(lines_01_i_tell_you);


    }


    /**
     * 切换到不可编辑状态下-->就去修改数据
     */


    private void checkDataAndRequest() {
        String Name = et_01_name.getText().toString().trim();
        if (Name.isEmpty()) {
            toast("用户名不能为空");
        } else {
            getElseCanSelected();
        }
    }


    /**
     * 获取其他可选参数
     */
    private void getElseCanSelected() {
        mParamsMap = new HashMap<>();
        String Married = et_03_is_married.getText().toString().trim();       //婚否 （已婚，未婚）
        if (!"其他".equals(Married)) {
            mParamsMap.put("Married", Married);
        }
        String Sex = et_01_sex_type.getText().toString().trim();       //性别 （男，女）
        if (!"性别".equals(Married)) {
            mParamsMap.put("Sex", Sex);
        }

        String Tel = et_03_tel.getText().toString().trim();       //电话
        String Address = et_03_address.getText().toString().trim();       //住址
//        String PatientNo = et_01_check_num.getText().toString().trim();       //病人编号---检查号???
        String CardID = et_03_my_id_num.getText().toString().trim();       //身份证号
        String MedHistory = lines_03_case_history.getContentText().toString().trim();       //医疗病史
        String FamilyHistory = lines_03_family_case_history.getContentText().toString().trim();       //家族病史
        String Race = et_03_ming_zu.getText().toString().trim();       //民族种族
        if (!"民族".equals(Race)) {
            mParamsMap.put("Race", Race);
        }
        String Occupatior = et_01_jop.getText().toString().trim();       //职业
        if (!"职业".equals(Occupatior)) {
            mParamsMap.put("Occupatior", Occupatior);
        }
        String InsuranceID = et_03_protection_num.getText().toString().trim();       //社保卡ID
        String NativePlace = et_03_native_place.getText().toString().trim();       //籍贯
//        String IsInHospital = et_03_in_hospital_num.getText().toString().trim();       //是否还在医院住院  ???
//        String LastCheckUserID = et_03_tel.getText().toString().trim();       //最后一个来查房的医生  ???
//        String DOB = et_.getText().toString().trim();       //生日                                  ???
        String PatientAge = et_01_age.getText().toString().trim();       //患者年龄
        String AgeUnit = tv_01_age_type.getText().toString().trim();       //年龄单位 （岁，月，天）
//        String ReturnVisit = et_03_tel.getText().toString().trim();       //初复诊 （0-初诊 1-复诊）  ???
        String BedID = et_03_case_bed_num.getText().toString().trim();       //病床号
        String WardID = et_03_case_area_num.getText().toString().trim();       //病区号
        String CaseID = et_03_case_num.getText().toString().trim();       //病历号
        String Department = et_03_section.getText().toString().trim();       //科室

        if (!"科室".equals(Department)) {
            mParamsMap.put("Department", Department);
        }
        String Device = et_03_device.getText().toString().trim();       //设备

        if (!"设备".equals(Device)) {
            mParamsMap.put("Device", Device);
        }
        String Fee = et_01_fee.getText().toString().trim();       //收费
//        String FeeType = et_03_tel.getText().toString().trim();       //收费类型         ???
        String ChiefComplaint = lines_01_i_tell_you.getContentText().toString().trim();       //主诉
        String Test = etlines_02_test.getContentText().toString().trim();       //试验
        String Advice = etlines_02_advice.getContentText().toString().trim();       //建议
        String InpatientID = et_03_in_hospital_num.getText().toString().trim();       //住院号
        String OutpatientID = et_03_door_num.getText().toString().trim();       //门诊号
        String Biopsy = etlines_02_live_check.getContentText().toString().trim();       //活检
        String Ctology = etlines_02_cytology.getContentText().toString().trim();       //细胞学
        String Pathology = etlines_02_pathology.getContentText().toString().trim();       //病理学
        //送检医生/SubmitDoctor   检查医生/ExaminingPhysician
        String SubmitDoctor = et_01_get_check_doctor.getText().toString().trim();       //送检医生
        String ExaminingPhysician = et_02_check_doctor.getText().toString().trim();       //检查医生
        String ClinicalDiagnosis = lines_01_bad_tell.getContentText().toString().trim();       //临床诊断
        String CheckContent = etlines_02_mirror_see.getContentText().toString().trim();       //检查内容（镜检所见）
        String CheckDiagnosis = etlines_02_mirror_result.getContentText().toString().trim();       //镜检诊断


        //添加三个必须添加的参数
        String UserName = mLoginUserName;
        String EndoType = (String) SharePreferenceUtil.get(getActivity(), SharePreferenceUtil.Current_EndoType, "3");
        mParamsMap.put("ID", getCurrentItemID());
        mParamsMap.put("Name", et_01_name.getText().toString().trim());
        mParamsMap.put("CaseNo", et_01_check_num.getText().toString().trim());
        mParamsMap.put("UserName", UserName);
        mParamsMap.put("EndoType", EndoType);
        mParamsMap.put("UserID", mUserID);
        mParamsMap.put("Tel", Tel);
        mParamsMap.put("Address", Address);
        mParamsMap.put("CardID", CardID);
        mParamsMap.put("MedHistory", MedHistory);
        mParamsMap.put("FamilyHistory", FamilyHistory);
        mParamsMap.put("Race", Race);
        mParamsMap.put("InsuranceID", InsuranceID);
        mParamsMap.put("NativePlace", NativePlace);
        mParamsMap.put("PatientAge", PatientAge);
        mParamsMap.put("AgeUnit", AgeUnit);
        mParamsMap.put("BedID", BedID);
        mParamsMap.put("WardID", WardID);
        mParamsMap.put("CaseID", CaseID);
        mParamsMap.put("Department", Department);
        mParamsMap.put("Fee", Fee);
        mParamsMap.put("ChiefComplaint", ChiefComplaint);
        mParamsMap.put("Test", Test);
        mParamsMap.put("Advice", Advice);
        mParamsMap.put("InpatientID", InpatientID);
        mParamsMap.put("OutpatientID", OutpatientID);
        mParamsMap.put("Biopsy", Biopsy);
        mParamsMap.put("Ctology", Ctology);
        mParamsMap.put("Pathology", Pathology);
        mParamsMap.put("ExaminingPhysician", ExaminingPhysician);
        mParamsMap.put("ClinicalDiagnosis", ClinicalDiagnosis);
        mParamsMap.put("CheckContent", CheckContent);
        mParamsMap.put("SubmitDoctor", SubmitDoctor);
        mParamsMap.put("CheckDiagnosis", CheckDiagnosis);
        sendUpdateRequest();


    }

    /**
     * 发送更新病例请求
     *
     * @param
     */
    private void sendUpdateRequest() {
        OkHttpUtils.post()
                .url(mBaseUrl + HttpConstant.CaseManager_ChangeCase)
                .params(mParamsMap)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showError(listener -> {
//                            sendRequest(mParamsMap);
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if ("" != response) {
                            AddCaseBean mBean = mGson.fromJson(response, AddCaseBean.class);
                            if (0 == mBean.getCode()) {  //成功
                                showComplete();
                                //socket告知上位机更新病例
                                sendSocketPointUpdateMessage(Constants.UDP_13);
                                ActivityManager.getInstance().finishActivity(AddCaseActivity.class);

                            } else {
                                toast(mBean.getMsg() + "");
                            }
                        } else {
                            showError(listener -> {
                            });
                        }
                    }
                });


    }

    //获取病例图片数目
    private void sendImageRequest(String mCaseID) {
        OkHttpUtils.get()
                .url(mBaseUrl + HttpConstant.CaseManager_CaseInfo)
                .addParams("ID", mCaseID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showError(listener -> {
                            sendRequest(mCaseID);
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if ("" != response) {
                            CaseDetailBean mBean = mGson.fromJson(response, CaseDetailBean.class);
                            if (0 == mBean.getCode()) {  //成功
                                showComplete();
                                imageCounts = mBean.getData().getImagesCount() + "";
                                videosCounts = mBean.getData().getVideosCount() + "";
                                DetailCaseActivity.mTabAdapter.setItem(1, "图片(" + imageCounts + ")");
                                DetailCaseActivity.mTabAdapter.setItem(2, "视频(" + videosCounts + ")");

                            } else {
                                showError(listener -> {
                                    sendRequest(mCaseID);
                                });
                            }
                        } else {
                            showError(listener -> {
                                sendRequest(mCaseID);
                            });
                        }
                    }
                });
    }
    /**
     * ***************************************************************************通讯模块**************************************************************************
     */
    /**
     * 切换病例 刷新数据
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void RefreshCaseMsgEvent(RefreshCaseMsgEvent event) {
        String caseID = event.getCaseID();
        String c1aseID = event.getCaseID();
        sendRequest(getCurrentItemID());

    }

    /**
     * eventbus 刷新socket数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void SocketRefreshEvent(SocketRefreshEvent event) {
//        String mRun2End4 = CalculateUtils.getReceiveRun2End4String(event.getData());//随机数之后到data结尾的String
//        String deviceType = CalculateUtils.getSendDeviceType(event.getData());
//        String deviceOnlyCode = CalculateUtils.getSendDeviceOnlyCode(event.getData());
//        String currentCMD = CalculateUtils.getCMD(event.getData());
        switch (event.getUdpCmd()) {
            case Constants.UDP_15://截图
                if (mCaseID.equals(event.getData())) {
                    sendImageRequest(mCaseID);
                }
                break;
            case Constants.UDP_16://删除图片
                sendImageRequest(mCaseID);
                break;
            case Constants.UDP_20://删除视频
                sendImageRequest(mCaseID);
                break;
            case Constants.UDP_18://新增录像:---->录像--->0：查询录像状态 1：开始录像，2：停止录像，3：正在录像  4：未录像,在停止录像的视频
                //获取当前上位机操作的病例ID

                String mUpCaseID = event.getIp();
                if (mUpCaseID.equals(mCaseID)) {
                    String tag = (String) event.getData();
                    if ("2".equals(tag) || "4".equals(tag)) {
                        sendImageRequest(mCaseID);
                    }
                }
                break;
            case Constants.UDP_13://有病例,并且当前病例id==回调病例id则更新界面数据
                if (event.getTga()) {
                    if (getCurrentItemID().equals(event.getData())) {
                        //请求界面数据
                        sendRequest(getCurrentItemID());
                    }
                }
                break;
        }

    }


    /**
     * 发送点对点消息,必须握手成功
     *
     * @param CMDCode 命令cmd
     */
    public void sendSocketPointUpdateMessage(String CMDCode) {
        if (HandService.UDP_HAND_GLOBAL_TAG) {
//            mCaseID
            UpdateCaseBean bean = new UpdateCaseBean();
            if (!"".equals(mCaseID)) {
                String hexStringID = CalculateUtils.numToHex16(Integer.parseInt(mCaseID));
                bean.setRecordid(hexStringID);
            }

            byte[] sendByteData = CalculateUtils.getSendByteData(getAttachActivity(), mGson.toJson(bean), mCurrentTypeNum + "", mCurrentReceiveDeviceCode,
                    CMDCode);
            if (("".equals(mSocketPort))) {
                toast("通讯端口不能为空");
                return;
            }

            SocketUtils.startSendPointMessage(sendByteData, mSocketOrLiveIP, Integer.parseInt(mSocketPort), getAttachActivity());
        } else {
            LogUtils.e(Constants.HAVE_HAND_FAIL_OFFLINE);
        }

    }


    /**
     * 发送点对点消息,必须握手成功
     *
     * @param CMDCode 命令cmd
     */
    public void sendSocketPointMessage(String CMDCode) {
        if (HandService.UDP_HAND_GLOBAL_TAG) {
            HandBean handBean = new HandBean();
            handBean.setHelloPc("");
            handBean.setComeFrom("");
            byte[] sendByteData = CalculateUtils.getSendByteData(getAttachActivity(), mGson.toJson(handBean), mCurrentTypeNum + "", mCurrentReceiveDeviceCode,
                    CMDCode);
            if (("".equals(mSocketPort))) {
                toast("通讯端口不能为空");
                return;
            }

            SocketUtils.startSendPointMessage(sendByteData, mSocketOrLiveIP, Integer.parseInt(mSocketPort), getAttachActivity());
        } else {
            LogUtils.e(Constants.HAVE_HAND_FAIL_OFFLINE);
        }

    }

    /**
     * ***************************************************************************通讯模块**************************************************************************
     */
    /**
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


    @Override
    public void onResume() {
        super.onResume();
        isFatherExit = false;
        sendListDictsRequest();
    }

    @Override
    public void onPause() {
        mFirstIn = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
