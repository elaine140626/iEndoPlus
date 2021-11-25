package com.company.iendo.green.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2021/11/16 15:13
 * desc：数据库设备表
 */

@Entity
public class DeviceDBBean {
    //主键
    @Id(autoincrement = true)
    private Long id;
    //设备唯一标识
    @Unique
    private String deviceID;
    //设备ip
    private String ip;
    //设备http端口
    private String httpPort;
    //设备socket端口
    private String socketPort;
    //设备直播端口
    private String livePort;
    //设备语音端口
    private String micPort;
    //设备账号(直播)
    private String username;
    //设备密码(直播)
    private String password;
    //设备标题
    private String title;
    //设备描述
    private String msg;
    //设备类型
    private String type;
    //工作站类型
    private String endoType;
    //备用字段
    private String usemsg01;
    //备用字段2
    private String usemsg02;
    @Generated(hash = 1975722533)
    public DeviceDBBean(Long id, String deviceID, String ip, String httpPort,
            String socketPort, String livePort, String micPort, String username,
            String password, String title, String msg, String type, String endoType,
            String usemsg01, String usemsg02) {
        this.id = id;
        this.deviceID = deviceID;
        this.ip = ip;
        this.httpPort = httpPort;
        this.socketPort = socketPort;
        this.livePort = livePort;
        this.micPort = micPort;
        this.username = username;
        this.password = password;
        this.title = title;
        this.msg = msg;
        this.type = type;
        this.endoType = endoType;
        this.usemsg01 = usemsg01;
        this.usemsg02 = usemsg02;
    }
    @Generated(hash = 1828217020)
    public DeviceDBBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceID() {
        return this.deviceID;
    }
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
    public String getIp() {
        return this.ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getHttpPort() {
        return this.httpPort;
    }
    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }
    public String getSocketPort() {
        return this.socketPort;
    }
    public void setSocketPort(String socketPort) {
        this.socketPort = socketPort;
    }
    public String getLivePort() {
        return this.livePort;
    }
    public void setLivePort(String livePort) {
        this.livePort = livePort;
    }
    public String getMicPort() {
        return this.micPort;
    }
    public void setMicPort(String micPort) {
        this.micPort = micPort;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getMsg() {
        return this.msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getEndoType() {
        return this.endoType;
    }
    public void setEndoType(String endoType) {
        this.endoType = endoType;
    }
    public String getUsemsg01() {
        return this.usemsg01;
    }
    public void setUsemsg01(String usemsg01) {
        this.usemsg01 = usemsg01;
    }
    public String getUsemsg02() {
        return this.usemsg02;
    }
    public void setUsemsg02(String usemsg02) {
        this.usemsg02 = usemsg02;
    }
}