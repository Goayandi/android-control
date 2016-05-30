/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co., Ltd. All rights reserved.
 * 
 * @author: hujianfeng@yongyida.com
 * @version 0.1
 * @date 2015-10-08
 * 
 */
package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.video.av.AVMeeting;
import com.yongyida.robot.video.av.CameraView;
import com.yongyida.robot.video.av.ISwitchView;
import com.yongyida.robot.video.av.TransferDataType;
import com.yongyida.robot.video.av.UserView;
import com.yongyida.robot.video.av.ViewStyle;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.DataPacket;
import com.yongyida.robot.video.command.RoomUser;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.net.HtoN;
import com.yongyida.robot.video.sdk.ChannelConfig;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.widget.FlowLayout;
import com.yongyida.robot.widget.WidgetPhotoView;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频页面
 */
public class ActivityMeeting extends BaseVideoActivity{
    public static final String TAG = "ActivityMeeting";

    private AudioManager mAudioManager;
    private int mThumbWidth;
    private int mThumbHeight;
    private FrameLayout mFrameLayout;
    private List<View> mViews;
    private AVMeeting mAVMeeting;
    private ChannelConfig mConfig;
    private DrawerLayout drawerLayout;
    private int mId;
    private String mRole;
    private int mRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.e(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting2);

        mId = getIntent().getIntExtra("id", -1);
        mRole = getIntent().getStringExtra("role");
        mRoomId = getIntent().getIntExtra("room_id", -1);
        initView();
        mAVMeeting = createAVMeeting();
        mThumbWidth = getThumbWidth();
        mThumbHeight = getThumbHeight();

        mFrameLayout = (FrameLayout) findViewById(R.id.view_container);
        mViews = new ArrayList<View>();
        addCameraView();
        addUserViews();

        drawerLayout = (DrawerLayout) super.findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        /* 有人进入房间 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_JOIN_ROOM}, mMediaJoinRoomBR);

        /* 退出房间返回 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.LOGIN_VIDEO_ROOM_LOGOUT_RESPONSE}, mMediaRoomLogoutBR);
    //    fillCallList();
    }

    private BroadcastReceiver mMediaRoomLogoutBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            close();
        }
    };

    private BroadcastReceiver mMediaJoinRoomBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO 有人加入房间
        }
    };

    @Override
    protected void initView() {
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        findViewById(R.id.btn_addcall).setOnClickListener(mToolbarClickListener);
        findViewById(R.id.btn_mute).setOnClickListener(mToolbarClickListener);
        findViewById(R.id.btn_hangup).setOnClickListener(mToolbarClickListener);
        findViewById(R.id.btn_addcall_dial).setOnClickListener(mAddCallClickListener);
        findViewById(R.id.btn_addcall_cancel).setOnClickListener(mAddCallClickListener);

    }

    private AVMeeting createAVMeeting() {
        log.d(TAG, "createAVMeeting()");

        mConfig = new ChannelConfig();
        mConfig.transferTyp = Config.getTransferType();
        mConfig.encoderType = Config.getEncoderType();
        int transferDataType = Config.getTransferDataType();
        mConfig.enableAudio = (transferDataType == TransferDataType.AUDIO
                || transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableVideo = (transferDataType == TransferDataType.VIDEO
                || transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableSend = getIntent().getBooleanExtra("EnableSend", true);
        mConfig.enableRecv = getIntent().getBooleanExtra("EnableRecv", true);
        log.d(TAG, mConfig.toString());

        return new AVMeeting(mConfig);
    }

    /**
     * 加入摄像头视图
     *
     */
    private void addCameraView() {
        log.d(TAG, "addCameraView");

        CameraView cv = new CameraView(this, mAVMeeting);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getViewInitWidth(), getViewInitHeight());
        layoutParams.setMargins(getViewInitLeft(), getViewInitTop(), 0, 0);

        cv.setLayoutParams(layoutParams);
        cv.setViewStyle(getInitViewStyle());
        cv.setOnClickListener(mSwitchClickListener);
        cv.setTitle("Camera");
        mViews.add(cv);
        mFrameLayout.addView(cv);
    }

    private void addUserViews() {
        log.d(TAG, "addUserViews");

        List<RoomUser> users = mAVMeeting.getRoomUsers();
        for (RoomUser user : users) {
            log.d(TAG, "user: " + user.toString());
            addUserView(user);
        }

        // 刷新视图, 把大视图置底，小视图置前。
        refreshView();
    }

    private void addUserView(RoomUser user) {
        log.d(TAG, "addUserView");

        UserView uv = new UserView(this, user, mAVMeeting);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getViewInitWidth(), getViewInitHeight());
        layoutParams.setMargins(getViewInitLeft(), getViewInitTop(), 0, 0);
        uv.setLayoutParams(layoutParams);
        uv.setViewStyle(getInitViewStyle());
        uv.setOnClickListener(mSwitchClickListener);
        uv.setTitle(user.getRole() + user.getId());
        mViews.add(uv);
        mFrameLayout.addView(uv);
    }

    private void refreshView() {
        log.d(TAG, "refreshView()");

        View bigView = getBigView();
        if (bigView != null) {
            moveToBack(bigView);
        }
        else {
            log.d(TAG, "Not found bigview");
        }
    }

    private void moveToBack(View currentView) {
        log.d(TAG, "moveToBack(), view: " + currentView);

        for (View view : mViews) {
            if (view instanceof ISwitchView
                    && ((ISwitchView) view).getViewStyle() == ViewStyle.ThumbView) {
                mFrameLayout.bringChildToFront(view);
            }
        }
    }

    private void toggleRightSliding() {
        log.d(TAG, "toggleRightSliding");

        if (drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
        }
        else {
            drawerLayout.openDrawer(Gravity.END);
        }
    }

    /**
     * 填充可添加通话列表
     * @param
     * @return
     *
     */
    private void fillCallList() {
        FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
        layout.clear();

        User myself = YYDSDKHelper.getInstance().getUser();
        List<User> friends = YYDLogicServer.getInstance().getFriendList();
        for (User user : friends) {
            if ((myself != null && user.equals(myself)) //排除自己
                    || mAVMeeting.contains(user))       //排除视频中用户
                continue;

            // 未在视频中的用户才加入添加通话列表
            WidgetPhotoView view = new WidgetPhotoView(this);
            view.setUser(user);
            view.setImageResource(R.drawable.ic_photo_default);
            view.setText(user.getUserName());
            view.setOnClickListener(mContactClickListener);
            layout.addView(view);
        }
    }

    /**
     * 移除用户
     * @param user
     */
    private void removeCallList(RoomUser user) {
        FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
        int count = layout.getChildCount();
        for (int i = 0; i < count; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof WidgetPhotoView) {
                WidgetPhotoView wpv = (WidgetPhotoView)view;
                if (wpv.getUser().equals(user)) {
                    layout.removeView(view);
                }
            }
        }
    }

    private OnClickListener mSwitchClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof ISwitchView
                    && ((ISwitchView)v).getViewStyle() == ViewStyle.ThumbView) {
                View bigView = getBigView();
                if (bigView != null) {
                    switchView(v, bigView);
                }
                else {
                    log.e(TAG, "Not found big view!");
                }
            }
        }
    };

    private void switchView(View smallView, View bigView) {
        log.d(TAG, "switchView(), smallView: " + smallView + ", bigView: " + bigView);

        // 大视图变为小视图
        FrameLayout.LayoutParams sp = (FrameLayout.LayoutParams)smallView.getLayoutParams();
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(smallView.getWidth(), smallView.getHeight());
        bp.setMargins(sp.leftMargin, sp.topMargin, sp.rightMargin, sp.bottomMargin);
        bigView.setLayoutParams(bp);
        if (bigView instanceof ISwitchView) {
            ISwitchView bv = (ISwitchView) bigView;
            bv.setViewStyle(ViewStyle.ThumbView);
        }

        // 小视图变为大视图
        FrameLayout.LayoutParams sp2 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        smallView.setLayoutParams(sp2);
        if (smallView instanceof ISwitchView) {
            ISwitchView sv = (ISwitchView) smallView;
            sv.setViewStyle(ViewStyle.FullView);
        }

        // 刷新，把小视图（原来的大视图）置前
        refreshView();
    }

    private View getBigView() {
        for (View v : mViews) {
            if (v instanceof ISwitchView
                    && ((ISwitchView) v).getViewStyle() == ViewStyle.FullView)
                return v;
        }
        return null;
    }

    private void removeUserView(String role, long id) {
        log.d(TAG, "removeUserView(), role: " + role + ", id: " + id);

        UserView uv = getUserView(role, id);
        if (uv != null) {
            uv.close();
            mViews.remove(uv);
            mFrameLayout.removeView(uv);
        }
        else {
            log.e(TAG, "Not found user view, role: " + role + ", id: " + id);
        }
    }

    private UserView getUserView(String role, long id) {
        log.d(TAG, "getUserView(), role: " + role + ", id: " + id);

        for (View v : mViews) {
            if (v instanceof UserView) {
                UserView uv = (UserView) v;
                log.d(TAG, "User role: " + uv.getUser().getRole() + ", id: " + uv.getUser().getId());
                if (role.equals(uv.getUser().getRole()) && id == uv.getUser().getId()) {
                    return uv;
                }
            }
        }
        return null;
    }

    private int getScreenHeight() {
        return (Utils.getScreenConfigurationOrientatioin(this) == Configuration.ORIENTATION_LANDSCAPE)
                ? Utils.getScreenHeight(this) : Utils.getScreenWidth(this);
    }

    private int getThumbWidth() {
        return (int) ((((float) mAVMeeting.getMeetingInfo().VideoWidth) / mAVMeeting.getMeetingInfo().VideoHeight) * getThumbHeight());
    }

    private int getThumbHeight() {
        return getScreenHeight() / 4;
    }

    private ViewStyle getInitViewStyle() {
        return (mViews.size() == 1) ? ViewStyle.FullView : ViewStyle.ThumbView;
    }

    private int getViewInitLeft() {
        if (mViews.size() > 1)
            return (mViews.size() - 1) * (mThumbWidth + 5);
        else
            return 0;
    }

    private int getViewInitTop() {
        return 0;
    }

    private int getViewInitWidth() {
        if (mViews.size() == 1)
            return LayoutParams.MATCH_PARENT;
        else
            return mThumbWidth;
    }

    private int getViewInitHeight() {
        if (mViews.size() == 1)
            return LayoutParams.MATCH_PARENT;
        else
            return mThumbHeight;
    }


    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            log.d(TAG, "ORIENTATION_PORTRAIT");
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            log.d(TAG, "ORIENTATION_LANDSCAPE");
        }
    }

    private OnClickListener mToolbarClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_addcall:
                    showAddCall();
                    break;
                case R.id.btn_mute:
                    switchMute();
                    break;
                case R.id.btn_hangup:
                    hangUp();
                    break;
                default:
                    break;
            }
        }
    };

    private OnClickListener mContactClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            WidgetPhotoView pv = (WidgetPhotoView) v;
            pv.setSelected(!pv.getSelected());
        }
    };

    private OnClickListener mAddCallClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_addcall_dial:
                    addCall_dial();
                    break;
                case R.id.btn_addcall_cancel:
                    addCall_close();
                    break;
                default:
                    break;
            }
        }
    };

    private void addCall_dial() {
        FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
        int count = layout.getChildCount();
        for (int i = 0; i < count; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof WidgetPhotoView) {
                WidgetPhotoView wpv = (WidgetPhotoView)view;
                if (wpv.getSelected()) {
                    User user = wpv.getUser();
                    meetingInvite(user.getRole(), user.getId());
                }
            }
        }

        //拨号完成后关闭
        addCall_close();
    }

    private void addCall_close() {
        toggleRightSliding();

        // 取消已选中的项
        FlowLayout layout = (FlowLayout) findViewById(R.id.layout_contacts);
        int count = layout.getChildCount();
        for (int i = 0; i < count; ++i) {
            View view = layout.getChildAt(i);
            if (view instanceof WidgetPhotoView) {
                WidgetPhotoView wpv = (WidgetPhotoView)view;
                wpv.setSelected(false);
            }
        }
    }

    public void meetingInvite(String role, long id) {
        log.d(TAG, "meetingInvite(), role: " + role + ", id: " + id);
        YYDLogicServer.getInstance().meetingInvite(
                "test", //会议名称
                role,
                id,
                true, // enableAudio
                true, // enableVideo
                new CmdCallBacker() {
                    public void onSuccess(Object arg) {
                        log.d(TAG, "meetingInvite success");
                    }

                    public void onFailed(int error) {
                        log.d(TAG, "meetingInvite failed, error: " + error);
                    }
                });
    }

    /**
     * 添加通话
     * @param
     * @return
     *
     */
    private void showAddCall() {
        toggleRightSliding();
    }

    /**
     * 静音切换
     * @param
     * @return
     *
     */
    private void switchMute() {
        setMute(!isMute());
        findViewById(R.id.btn_mute).setBackgroundResource(isMute() ? R.drawable.ic_toolbar_mute_checked : R.drawable.ic_toolbar_mute);
    }

    /**
     * 挂断
     * @param
     * @return
     *
     */
    private void hangUp() {
        Intent intent = new Intent(Constants.LOGOUT_VIDEO_ROOM);
        intent.putExtra("id", mId + "");
        intent.putExtra("role", mRole);
        intent.putExtra("room_id", mRoomId);
        sendBroadcast(intent);
    }

    public boolean isMute() {
        return mAudioManager.isMicrophoneMute();
    }

    public void setMute(boolean mute) {
        mAudioManager.setMicrophoneMute(mute);
    }

    public boolean isSpeakerOn() {
        return mAudioManager.isSpeakerphoneOn();
    }

    public void setSpeakerOn(boolean speakerOn) {
        mAudioManager.setSpeakerphoneOn(speakerOn);
    }

    /**
     * 转发数据
     * @param
     * @return
     *
     */
    public void forward() {
        byte[] bytes = HtoN.getBytes("0123456789");
        DataPacket data = DataPacket.createCopy(bytes, 0, bytes.length);
        YYDVideoServer.getInstance().forward(data, new CmdCallBacker() {
            public void onSuccess(Object arg) {
                log.d(TAG, "forward success");
            }

            public void onFailed(int error) {
                log.d(TAG, "forward failed, error: " + error);
            }
        });
    }

    private void close() {
        for (View v : mViews) {
            if (v instanceof CameraView) {
                ((CameraView) v).close();
            }
            if (v instanceof UserView) {
                ((UserView) v).close();
            }
        }

        if (mAVMeeting != null) {
            mAVMeeting.close();
            mAVMeeting = null;
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        try {
            if (mMediaJoinRoomBR != null) {
                unregisterReceiver(mMediaJoinRoomBR);
            }
            if (mMediaRoomLogoutBR != null) {
                unregisterReceiver(mMediaRoomLogoutBR);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

}
