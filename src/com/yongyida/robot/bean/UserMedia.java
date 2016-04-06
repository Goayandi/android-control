package com.yongyida.robot.bean;

/**
 * Created by Administrator on 2016/4/5 0005.
 */
public class UserMedia {
    private String role;

    private String id;

    private int room_id;

    private String nickname;

    private String pic;

    private String recv_host;

    private String recv_port;

    private String send_host;

    private String send_port;


    /**
     * 用户标识
     */
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 数字id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 房间id
     */
    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    /**
     * 昵称
     */
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 图片
     */
    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    /**
     * udp接收地址
     */
    public String getRecv_host() {
        return recv_host;
    }

    public void setRecv_host(String recv_host) {
        this.recv_host = recv_host;
    }

    /**
     * udp接收端口
     */
    public String getRecv_port() {
        return recv_port;
    }

    public void setRecv_port(String recv_port) {
        this.recv_port = recv_port;
    }

    /**
     * udp发送地址
     */
    public String getSend_host() {
        return send_host;
    }

    public void setSend_host(String send_host) {
        this.send_host = send_host;
    }

    /**
     * udp发送端口
     */
    public String getSend_port() {
        return send_port;
    }

    public void setSend_port(String send_port) {
        this.send_port = send_port;
    }
}
