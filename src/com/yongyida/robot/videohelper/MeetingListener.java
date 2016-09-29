package com.yongyida.robot.videohelper;

import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by Administrator on 2016/8/30 0030.
 */
public interface MeetingListener {
    void onUserJoined(int uid, int elapsed);  //用户进入
    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);   //显示房间内其他用户的视频
    void onUserOffline(int uid, int reason);  //用户退出
    void onUserMuteVideo(int uid,boolean muted);  //监听其他用户是否关闭视频
    void onUpdateSessionStats(IRtcEngineEventHandler.RtcStats stats);  //更新聊天数据
    void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats);
    void onError(int err);
    void onJoinChannelSuccess(String channel, int uid, int elapsed); //加入频道回调
}
