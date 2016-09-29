package com.yongyida.robot.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.videohelper.MeetingListener;
import com.yongyida.robot.videohelper.VideoEngine;
import com.yongyida.robot.widget.VideoMeetingUI;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Administrator on 2016/8/30 0030.
 * 发送给服务器的id是手机的id
 * 加入用的uid需要加掩码
 */
public class VideoMeetingActivity extends BaseEngineEventHandlerActivity implements MeetingListener {


    private static final String TAG = "VideoMeetingActivity";
    private long mStartTime;
    private long mStopTime;
    private static final int TIME_COUNT = 100;
    private VideoMeetingUI mVideoMeetingUI;
    private TextView mInviteTV;
    private TextView mMuteTV;
    private TextView mSwitchCameraTV;
    private TextView mHangupTV;
    private String mAccount;
    private String mPhoneNumber;
    private String mChannelID;
    private long mCallId;  //-1 房主
    private boolean mute = false;
    private ProgressDialog mProgressDialog;
    private boolean mInviteState = true;
    private String mBindRobotID;
    private TextView mTimeTV;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private int mTimeCount;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_COUNT:
                    mTimeTV.setText(Utils.showTimeCount(mTimeCount * 1000));
                    break;
            }
        }
    };
    private BroadcastReceiver mInviteResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.AGORA_RET, -2);
            if (ret == 0) {
                ToastUtil.showtomain(VideoMeetingActivity.this, "邀请发送成功");
            } else if (ret == -1) {
                ToastUtil.showtomain(VideoMeetingActivity.this, "对方不在线");
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            } else {
                ToastUtil.showtomain(VideoMeetingActivity.this, "邀请发送失败");
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        }
    };

    private BroadcastReceiver mInviteReplyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int reply = intent.getIntExtra(Constants.AGORA_REPLY, -1);
            long id = intent.getLongExtra(Constants.AGORA_ID, -1);
            if (reply == 0) {
                ToastUtil.showtomain(VideoMeetingActivity.this, id + "拒绝邀请");
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            } else if (reply == 1) {
                ToastUtil.showtomain(VideoMeetingActivity.this, id + "接受邀请");
            }
        }
    };

    private BroadcastReceiver mTimeBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.AGORA_RET, -1);
            if (ret == 0) {
                //计时上传成功
            } else {
                //计时上传失败
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_meeting);
        init();
    }

    private void init(){
        initData();
        initLayout();
        initBR();
        initMeetingInfo();
    }

    private void initData() {
        mCallId = getIntent().getLongExtra(Constants.AGORA_ID, -1);
        mChannelID = getIntent().getStringExtra(Constants.AGORA_CHANNEL_ID);
        mBindRobotID = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "robotid", null);
        mPhoneNumber = Utils.getPhoneNumber(this);
        Log.e(TAG, "num:" + mPhoneNumber);
    }

    private void initLayout(){
        mVideoMeetingUI = (VideoMeetingUI) findViewById(R.id.video_meeting_ui);
        mTimeTV = (TextView) findViewById(R.id.tv_time);
        mInviteTV = (TextView) findViewById(R.id.tv_invite);
        mInviteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallId != -1) {
                    ToastUtil.showtomain(VideoMeetingActivity.this, "只有房主能邀请");
                    return;
                } else {
                    if (!mInviteState) {
                        ToastUtil.showtomain(VideoMeetingActivity.this, "用户已进入房间");
                        return;
                    }
                }

                if (!TextUtils.isEmpty(mBindRobotID)) {
                    if (mProgressDialog != null) {
                        mProgressDialog.setMessage(getString(R.string.inviting_wait));
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                    }
                    invite(Long.parseLong(mBindRobotID));
                } else {
                    ToastUtil.showtomain(VideoMeetingActivity.this, "error");
                    finish();
                }
            }
        });
        mMuteTV = (TextView) findViewById(R.id.tv_mute);
        mMuteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mute = !mute;
                mVideoEngine.muteLocalAudioStream(mute);
                setMuteIcon(mute);
            }
        });
        mSwitchCameraTV = (TextView) findViewById(R.id.tv_switch_camera);
        mSwitchCameraTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoEngine.switchCamera();
            }
        });
        mHangupTV = (TextView) findViewById(R.id.tv_hang_up);
        mHangupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void invite(long account){
        Log.e(TAG, "invite account:" + Long.parseLong(mAccount));
        Log.e(TAG, "invite num:" + account);
        Intent intent = new Intent(Constants.AGORA_VIDEO_MEETING_INVITE);
        intent.putExtra(Constants.AGORA_ID, Long.parseLong(mAccount)); //LONG
        intent.putExtra(Constants.AGORA_NUMBER, account); //LONG
        intent.putExtra(Constants.AGORA_ROLE, "User"); //String
        intent.putExtra(Constants.AGORA_NICKNAME, Utils.getAccount(this)); //String
        intent.putExtra(Constants.AGORA_CHANNEL_ID, mPhoneNumber); //String
        intent.putExtra(Constants.AGORA_TYPE, "Robot"); //String
        intent.putExtra(Constants.AGORA_MODE, "meeting"); //String
        sendBroadcast(intent);
    }

    private void inviteCancel(){
        Intent intent = new Intent(Constants.AGORA_VIDEO_MEETING_INVITE_CANCEL);
        if (!TextUtils.isEmpty(mAccount)) {
            intent.putExtra(Constants.AGORA_ID, Long.parseLong(mAccount)); //LONG
        }
        if (!TextUtils.isEmpty(mBindRobotID)) {
            intent.putExtra(Constants.AGORA_NUMBER, Long.parseLong(mBindRobotID)); //LONG
        }
        intent.putExtra(Constants.AGORA_ROLE, "User"); //String
        intent.putExtra(Constants.AGORA_TYPE, "Robot"); //String
        sendBroadcast(intent);
    }

    private void initBR(){
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_INVITE_REPONSE}, mInviteResponseBR);
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_INVITE_REPLY}, mInviteReplyBR);
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_MEETING_TIME_REPONSE}, mTimeBR);
    }

    private void initMeetingInfo(){
        mProgressDialog = new ProgressDialog(this);
        boolean wificheck = getSharedPreferences("setting",
                MODE_PRIVATE).getBoolean("wificheck", true);
        if (wificheck && !checknetwork()) {
            ToastUtil.showtomain(this, getString(R.string.not_wifi));
            return;
        }
        mAccount = Utils.getUserID(this);
        Log.i(TAG, "account:" + mAccount);
        if ("error".equals(mAccount)){
            ToastUtil.showtomain(this, "账户出错");
            finish();
            return;
        }
        mVideoEngine = VideoEngine.create(getApplicationContext());
        mVideoEngine.setEngineEventHandlerMeetingListener(this);
        mVideoEngine.setLogFile(getApplicationContext().getExternalFilesDir(null).toString() + "/agorasdk.log");
        mVideoEngine.enableVideo();
        SurfaceView localSurfaceView = mVideoEngine.getSurfaceView(getApplicationContext());
        mVideoMeetingUI.initRoom(localSurfaceView, mAccount);
        mVideoEngine.setupLocalVideo(new VideoCanvas(localSurfaceView));
        mVideoEngine.muteLocalVideoStream(false);
        mVideoEngine.muteLocalAudioStream(false);
        mVideoEngine.setEnableSpeakerphone(true);
        mVideoEngine.muteAllRemoteVideoStreams(false);
        mVideoEngine.setVideoProfile(20);
        if (mCallId == -1) {
            mVideoEngine.joinChannel(
                    mVideoEngine.VendorKey,
                    mPhoneNumber,
                    "" /*optionalInfo*/,
                    ACCOUNT_MASK + Integer.parseInt(mAccount)/*optionalUid*/);
        } else {
            setInviteIcon(false);
            mVideoEngine.joinChannel(
                    mVideoEngine.VendorKey,
                    mChannelID,
                    "" /*optionalInfo*/,
                    ACCOUNT_MASK + Integer.parseInt(mAccount)/*optionalUid*/);
        }
    }

    private void startTime(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartTime = System.currentTimeMillis();
                mTimeTV.setVisibility(View.VISIBLE);
                resetTimeCount();
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        mTimeCount++;
                        mHandler.sendEmptyMessage(TIME_COUNT);
                    }
                };
                mTimer = new Timer();
                mTimer.schedule(mTimerTask, 1000, 1000);
            }
        });
    }

    private void resetTimeCount(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStopTime = System.currentTimeMillis();
                mTimeTV.setText(Utils.showTimeCount(0));
                mTimeCount = 0;
                Intent intent = new Intent(Constants.AGORA_VIDEO_MEETING_TIME);
                intent.putExtra(Constants.AGORA_ID, Long.parseLong(mAccount));
                intent.putExtra(Constants.AGORA_ROLE, "User");
                intent.putExtra(Constants.AGORA_BEGINTIME, mStartTime + "");
                intent.putExtra(Constants.AGORA_ENDTIME, mStopTime + "");
                sendBroadcast(intent);
            }
        });
    }


    private void setMuteIcon(boolean isMute) {
        if (isMute) {
            Drawable drawableT = getResources().getDrawable(R.drawable.tv_mute);
            drawableT.setBounds(0, 0, drawableT.getMinimumWidth(), drawableT.getMinimumHeight());
            mMuteTV.setCompoundDrawables(null, drawableT, null, null);
        } else {
            Drawable drawableF = getResources().getDrawable(R.drawable.tv_not_mute);
            drawableF.setBounds(0, 0, drawableF.getMinimumWidth(), drawableF.getMinimumHeight());
            mMuteTV.setCompoundDrawables(null, drawableF, null, null);
        }
    }

    private void setInviteIcon(boolean invitable){
        if (invitable) {
            Drawable drawableT = getResources().getDrawable(R.drawable.tv_invite);
            drawableT.setBounds(0, 0, drawableT.getMinimumWidth(), drawableT.getMinimumHeight());
            mInviteTV.setCompoundDrawables(null, drawableT, null, null);
            mInviteState = true;
        } else {
            Drawable drawableF = getResources().getDrawable(R.drawable.tv_not_invite);
            drawableF.setBounds(0, 0, drawableF.getMinimumWidth(), drawableF.getMinimumHeight());
            mInviteTV.setCompoundDrawables(null, drawableF, null, null);
            mInviteState = false;
        }
    }

    @Override
    public void onBackPressed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mVideoEngine.leaveChannel();
            }
        }).run();

        finish();
    }

    @Override
    protected void onDestroy() {
        if (mCallId == -1) {
            inviteCancel();
        }
        mVideoEngine.setEngineEventHandlerMeetingListener(null);
        super.onDestroy();
        Utils.unRegisterReceiver(mInviteResponseBR, this);
        Utils.unRegisterReceiver(mInviteReplyBR, this);
        Utils.unRegisterReceiver(mTimeBR, this);
        // keep screen on - turned off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                if (mCallId == -1) {
                    if (uid == Integer.parseInt(mBindRobotID)) {
                        setInviteIcon(false);
                    }
                }
                // ensure remote video view setup
                if (mVideoMeetingUI.getAccoutExistState(uid + "")) {
                    return;
                }
                final SurfaceView remoteView = mVideoEngine.getSurfaceView(getApplicationContext());
                mVideoMeetingUI.addRoomUser(remoteView, uid + "");
                mVideoEngine.enableVideo();
                int successCode = mVideoEngine.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid));
                Log.e(TAG, "successCode:" + successCode);
                if (successCode < 0) {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mVideoEngine.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid));
                            remoteView.invalidate();
                        }
                    }, 500);
                }
            }
        });
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCallId != -1) {
                    if (uid == Integer.parseInt(mBindRobotID)) {
                        finish();
                    } else {
                        mVideoMeetingUI.removeRoomUser(uid + "");
                    }
                } else {
                    if (uid == Integer.parseInt(mBindRobotID)) {
                        setInviteIcon(true);
                    }
                    mVideoMeetingUI.removeRoomUser(uid + "");
                }

            }
        });
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {

    }

    @Override
    public void onUpdateSessionStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        resetTimeCount();
        finish();
    }

    @Override
    public void onError(int err) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        startTime();
    }
}
