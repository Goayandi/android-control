package com.yongyida.robot.videohelper;

import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by Administrator on 2016/8/30 0030.
 */
public class MessageHandler extends IRtcEngineEventHandler {

    private MeetingListener mMeetingListener;

    //显示房间内其他用户的视频
    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

        if (mMeetingListener != null) {
            mMeetingListener.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        }
    }

    //用户进入
    @Override
    public void onUserJoined(int uid, int elapsed){

        if (mMeetingListener != null) {
            mMeetingListener.onUserJoined(uid, elapsed);
        }
    }

    //用户退出
    @Override
    public void onUserOffline(int uid, int reason) {

        if (mMeetingListener != null) {
            mMeetingListener.onUserOffline(uid, reason);
        }
    }

    //监听其他用户是否关闭视频
    @Override
    public void onUserMuteVideo(int uid,boolean muted){

        if (mMeetingListener != null) {
            mMeetingListener.onUserMuteVideo(uid, muted);
        }
    }

    //更新聊天数据
    @Override
    public void onRtcStats(RtcStats stats){

        if (mMeetingListener != null) {
            mMeetingListener.onUpdateSessionStats(stats);
        }
    }


    @Override
    public void onLeaveChannel(RtcStats stats) {

        if (mMeetingListener != null) {
            mMeetingListener.onLeaveChannel(stats);
        }
    }


    @Override
    public void onError(int err) {
        if (mMeetingListener != null) {
            mMeetingListener.onError(err);
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        if (mMeetingListener != null) {
            mMeetingListener.onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    public void setMeetingListener(MeetingListener listener) {

        this.mMeetingListener = listener;
    }
}
