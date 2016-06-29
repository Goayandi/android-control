package com.yongyida.robot.bean;

/**
 * Created by Administrator on 2016/6/13 0013.
 */

/**
 * 辅助类  封装用来使用视频会议的request
 */
public class MeetingMemberObj {
    private String account;
    private String ip;
    private int port;
    public MeetingMemberObj(String account, String ip, int port) {
        this.setAccount(account);
        this.setIp(ip);
        this.setPort(port);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
