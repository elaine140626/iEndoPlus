package com.company.iendo.app;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Message;

import com.company.iendo.bean.RefreshEvent;
import com.company.iendo.bean.event.SocketRefreshEvent;
import com.company.iendo.bean.socket.RecodeBean;
import com.company.iendo.bean.socket.getpicture.ColdPictureBean;
import com.company.iendo.bean.socket.getpicture.LookReportBean;
import com.company.iendo.bean.socket.getpicture.PrintReportBean;
import com.company.iendo.bean.socket.getpicture.UserIDBean;
import com.company.iendo.mineui.socket.ThreadManager;
import com.company.iendo.other.Constants;
import com.company.iendo.utils.CalculateUtils;
import com.company.iendo.utils.LogUtils;
import com.company.iendo.utils.SharePreferenceUtil;
import com.google.gson.Gson;
import com.hjq.gson.factory.GsonFactory;
import com.lzh.easythread.EasyThread;
import com.xdandroid.hellodaemon.AbsWorkService;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * 保活的Service通讯服务
 * <p>
 * 一直开启这监听线程,监听Socket
 */

public class ReceiveSocketService extends AbsWorkService {


    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static boolean isFirstIn = false;
    public static Disposable sDisposable;
    private Gson mGson;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null) sDisposable.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     *
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    /**
     * ***************************************************************************通讯模块**************************************************************************
     */
    private static DatagramSocket mReceiveSocket = null;
    private volatile static boolean isRuning = true;
    private String currentIP;


    /**
     * 开启消息接收线程
     */
    private void initReceiveThread() {
        mGson = GsonFactory.getSingletonGson();
        isRuning = true;
        //获取接收线程,设置了最高优先级10
        EasyThread mReceiveThread = ThreadManager.getIO();
        Runnable mReceiveRunnable = new Runnable() {
            @Override
            public void run() {
                LogUtils.e("正在执行Runnable任务：%s" + Thread.currentThread().getName());
                byte[] receiveData = new byte[1024];
                DatagramPacket mReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    if (mReceiveSocket == null) {
//                        mReceiveSocket = new DatagramSocket(Constants.RECEIVE_PORT);  //端口绑定异常
                        mReceiveSocket = new DatagramSocket(null);
                        mReceiveSocket.setReuseAddress(true);
                        mReceiveSocket.bind(new InetSocketAddress(Constants.RECEIVE_PORT));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    if (isRuning) {
                        try {
                            LogUtils.e("======LiveServiceImpl=====000==");
                            LogUtils.e("======LiveServiceImpl=====mReceivePacket.getAddress()==" + mReceivePacket.getAddress());
                            LogUtils.e("======LiveServiceImpl=====mReceivePacket.getData()==" + mReceivePacket.getAddress());
                            LogUtils.e("======LiveServiceImpl=====currentIP==" + currentIP);
                            if (!currentIP.equals(mReceivePacket.getAddress())) {   //不是自己的IP不接受
                                mReceiveSocket.receive(mReceivePacket);
                                String rec = CalculateUtils.byteArrayToHexString(mReceivePacket.getData()).trim();
//                                String rec = CalculateUtils.byteArrayToHexString(mReceivePacket.getData()).trim();
                                //过滤不是发送给我的消息全部不接受
                                int length = mReceivePacket.getLength() * 2;
                                String resultData = rec.substring(0, length);
                                LogUtils.e("======LiveServiceImpl=====获取长度==length==" + length);
                                LogUtils.e("======LiveServiceImpl=====获取长度数据==substring==" + resultData);
                                LogUtils.e("======LiveServiceImpl=====接受到数据==原始数据====mReceivePacket.getData()=" + mReceivePacket.getData());
                                LogUtils.e("======LiveServiceImpl=====3333==" + mReceivePacket.getData());
                                if (mReceivePacket != null) {
                                    LogUtils.e("======LiveServiceImpl=====mReceivePacket!= null==");
                                    boolean flag = false;//是否可用的ip---此ip是服务器ip
                                    String finalOkIp = "";
                                    if (CalculateUtils.getDataIfForMe(resultData, getApplicationContext())) {
                                        finalOkIp = CalculateUtils.getOkIp(mReceivePacket.getAddress().toString());
                                        flag = true;//正确的服务器ip地址
                                    }
                                    LogUtils.e("======LiveServiceImpl=====mReceivePacket!= flag==" + flag);

                                    //正确的服务器ip地址,才开始计算获取自己需要的数据
                                    if (flag) {
                                        String mRun2End4 = CalculateUtils.getReceiveRun2End4String(resultData);//随机数之后到data结尾的String
                                        String deviceType = CalculateUtils.getSendDeviceType(resultData);
                                        String deviceOnlyCode = CalculateUtils.getSendDeviceOnlyCode(resultData);
                                        String currentCMD = CalculateUtils.getCMD(resultData);
                                        LogUtils.e("======LiveServiceImpl==回调===随机数之后到data结尾的String=mRun2End4==" + mRun2End4);
                                        LogUtils.e("======LiveServiceImpl==回调===设备类型deviceType==" + deviceType);
                                        LogUtils.e("======LiveServiceImpl==回调===设备ID=deviceOnlyCode==" + deviceOnlyCode);
                                        LogUtils.e("======LiveServiceImpl==回调===CMD=currentCMD==" + currentCMD);
                                        SocketRefreshEvent event = new SocketRefreshEvent();
                                        Boolean dataIfForMe = CalculateUtils.getDataIfForMe(resultData, getApplicationContext());
                                        String dataString = CalculateUtils.getReceiveDataString(resultData);
                                        //16进制直接转换成为字符串
                                        String str = CalculateUtils.hexStr2Str(dataString);
                                        if (dataIfForMe) {
                                            switch (currentCMD) {
                                                case Constants.UDP_HAND://握手
                                                    LogUtils.e("======LiveServiceImpl==回调===握手==");
                                                    //判断数据是否是发个自己的
                                                    LogUtils.e("======LiveServiceImpl=====dataIfForMe==" + dataIfForMe);
                                                    //设备在线握手成功
                                                    event.setTga(true);
                                                    event.setData(resultData);
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_HAND);
                                                    EventBus.getDefault().post(event);
                                                    break;

                                                case Constants.UDP_FD: //广播
                                                    LogUtils.e("======LiveServiceImpl==回调===广播==");
                                                    event.setTga(true);
                                                    event.setData(resultData);
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_FD);
                                                    EventBus.getDefault().post(event);

                                                    break;
                                                case Constants.UDP_FC://授权接入
                                                    LogUtils.e("======LiveServiceImpl==回调===授权接入==");
                                                    //获取到病例的ID是十六进制的,需要转成十进制
                                                    event.setTga(true);
                                                    event.setData(resultData);
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_FC);
                                                    EventBus.getDefault().post(event);

                                                    break;
                                                case Constants.UDP_F0://获取当前病例
                                                    LogUtils.e("======LiveServiceImpl==回调===获取当前病例==");
                                                    //获取到病例的ID是十六进制的,需要转成十进制

                                                    LogUtils.e("======GetPictureActivity==回调===CMD=getReceiveDataString==" + dataString);
//                                                    String jsonID = CalculateUtils.hex16To10(dataString) + "";
                                                    LogUtils.e("======GetPictureActivity==回调===CMD=CalculateUtils.hexStr2Str(dataString)==" + CalculateUtils.hexStr2Str(dataString));
                                                    UserIDBean mUserIDBean = mGson.fromJson(str, UserIDBean.class);
//                                                    LogUtils.e("======GetPictureActivity==回调===CMD=jsonID==" + jsonID);
                                                    String jsonID = CalculateUtils.hex16To10(mUserIDBean.getRecordid()) + "";
                                                    //必须从新取数据不然会错乱
                                                    String spCaseID = (String) SharePreferenceUtil.get(getApplicationContext(), SharePreferenceUtil.Current_Chose_CaseID, "");
                                                    LogUtils.e("======GetPictureActivity==回调===itemID=spCaseID=" + spCaseID);
                                                    LogUtils.e("======GetPictureActivity==回调===jsonID=jsonID=" + jsonID);
                                                    if (spCaseID.equals(jsonID)) {
                                                        //id相等才能操作截图等功能
                                                        event.setData("true");
                                                    } else {
                                                        event.setData("false");
                                                    }
                                                    event.setTga(true);
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_F0);
                                                    EventBus.getDefault().post(event);
                                                    break;
                                                case Constants.UDP_F3://冻结与解冻:00冻结，01解冻,未调试
                                                    LogUtils.e("======LiveServiceImpl==回调===冻结与解冻==");
                                                    ColdPictureBean mColdBean = mGson.fromJson(str, ColdPictureBean.class);
                                                    String jsonString = CalculateUtils.hex16To10(mColdBean.getFreeze()) + "";
                                                    event.setTga(true);
                                                    event.setData(jsonString);
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_F3);
                                                    EventBus.getDefault().post(event);
                                                    break;
                                                case Constants.UDP_F1://预览报告
                                                    LogUtils.e("======LiveServiceImpl==回调===预览报告==" + str);
                                                    LookReportBean lookBean = mGson.fromJson(str, LookReportBean.class);
                                                    LogUtils.e("======LiveServiceImpl==回调===预览报告==" + lookBean.toString());

                                                    event.setTga(true);
                                                    event.setData(lookBean.getReporturl());
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_F1);
                                                    EventBus.getDefault().post(event);
                                                    break;
                                                case Constants.UDP_F2://打印报告
                                                    LogUtils.e("======LiveServiceImpl==回调===打印报告==");
                                                    PrintReportBean portBean = mGson.fromJson(str, PrintReportBean.class);
                                                    event.setTga(true);
                                                    event.setData(portBean.getPrintcode());
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_F2);
                                                    EventBus.getDefault().post(event);
                                                    break;
                                                case Constants.UDP_18://录像
                                                    LogUtils.e("======LiveServiceImpl==回调===录像==");
                                                    RecodeBean recodeBean = mGson.fromJson(str, RecodeBean.class);
                                                    event.setTga(true);
                                                    event.setData(recodeBean.getQrycode());
                                                    event.setIp(finalOkIp);
                                                    event.setUdpCmd(Constants.UDP_18);
                                                    EventBus.getDefault().post(event);
                                                    break;


                                            }
                                        }
                                    }
                                }

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };

        mReceiveThread.execute(mReceiveRunnable);

    }

    /**
     * ***************************************************************************通讯模块**************************************************************************
     */

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        LogUtils.e("检查磁盘中是否有上次销毁时保存的数据");
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            currentIP = getIpString(wifiInfo.getIpAddress());
        }
        LogUtils.e("保活服务开启----AAA--" + Thread.currentThread().getName());
//        sDisposable = Observable
//                .interval(3, TimeUnit.SECONDS)//定时器操作符，这里三秒打印一个log
//                //取消任务时取消定时唤醒
//                .doOnDispose(() -> {
//                    LogUtils.e("保活服务开启------===doOnDispose");
//                    cancelJobAlarmSub();
//                })
//                .subscribe(count -> {
//                    LogUtils.e("保活服务开启------每 3 秒采集一次数据... count = " + count);
//                    if (count > 0 && count % 18 == 0)
//                        LogUtils.e("保活服务开启------保存数据到磁盘。 saveCount = " + (count / 18 - 1));
//                });

        if (!isFirstIn) {
            isFirstIn = true;
            LogUtils.e("保活服务开启----BBB--" + Thread.currentThread().getName());
            initReceiveThread();
        }


    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        LogUtils.e("保活服务开启------===关闭了");

        stopService();
    }

    /**
     * 任务是否正在运行?
     *
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        LogUtils.e("保存数据到磁盘===onServiceKilled。");
    }

    /**
     * 将获取到的int型ip转成string类型
     */
    private static String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRuning = true;
    }

    @Override
    public void onDestroy() {
        isFirstIn = false;
        super.onDestroy();
    }
}