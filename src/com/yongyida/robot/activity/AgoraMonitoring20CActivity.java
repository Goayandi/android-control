package com.yongyida.robot.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.videohelper.MeetingListener;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Administrator on 2016/9/8 0008.
 */
public class AgoraMonitoring20CActivity extends BaseEngineEventHandlerActivity implements MeetingListener, View.OnTouchListener {
    private static final String TAG = "AgoraMonitoring20C";
    private FrameLayout mSurfaceViewContainer;
    private Button mPlayBT;
    //  private ImageView mSpeakIV;
    private Button mBackBT;

    private ProgressDialog mProgressDialog;
    private Button mSpeakToggle;
    private static Timer mTimer = null;
    private String mPhoneNumber;
    private long mLastClickTime = 0;
    private String mBindRobotID;
    private String mAccount;
    private String mRobotName;
    /**
     * 设置通话状态监听
     */
    private boolean isconnected = false;
    private Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            if (msg.what == 1) {
                toggle();
            }

        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_monitoring_20c);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        // keep screen on - turned off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.unRegisterReceiver(neterror, this);
        Utils.unRegisterReceiver(mRNameBR, this);
    }

    @Override
    public void onBackPressed() {
        if (isconnected) {
            ToastUtil.showtomain(this, getString(R.string.hang_up_video_first));
            return;
        }
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mTimer != null)
                mTimer.cancel();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void init() {
        initData();
        initLayout();
        initBroadcastReceiver();
        initVideo();
    }

    private void initData() {
        mBindRobotID = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "robotid", null);
        mRobotName = getSharedPreferences("Receipt", MODE_PRIVATE).getString("username", null);
    }

    private void initVideo() {
        mPhoneNumber = Utils.getPhoneNumber(this);
        mAccount = Utils.getUserID(this);
        if ("error".equals(mAccount)){
            ToastUtil.showtomain(this, "账户出错");
            finish();
            return;
        }

        mVideoEngine.setEngineEventHandlerMeetingListener(this);

    }

    private void initBroadcastReceiver() {
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BATTERY}, mRNameBR);
        BroadcastReceiverRegister.reg(this,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION},
                neterror);
    }

    private void initLayout() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mPlayBT = (Button) findViewById(R.id.play);
        mPlayBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - mLastClickTime < 1000) {
                    mLastClickTime = currentTimeMillis;
                    ToastUtil.showtomain(AgoraMonitoring20CActivity.this, getString(R.string.dont_so_fast2));
                    return;
                }
                mLastClickTime = currentTimeMillis;
                if(!isconnected) {
                    playVideo();
                } else {
                    finishVideo();
                }
            }
        });
//        mSpeakIV = (ImageView) findViewById(R.id.speak);
//        mSpeakIV.setOnClickListener(this);
        mBackBT = (Button) findViewById(R.id.back);
        mBackBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mSpeakToggle = (Button) findViewById(R.id.mictoggole);
        mSpeakToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mSurfaceViewContainer = (FrameLayout) findViewById(R.id.fl);
        setSpeakToggleVisiableState(false);
        mProgressDialog = new ProgressDialog(this);
    }

    private void setSpeakToggleVisiableState(boolean show) {
//        if (show) {
//            mSpeakToggle.setVisibility(View.VISIBLE);
//        } else {
//            mSpeakToggle.setVisibility(View.GONE);
//        }
    }

    private void resumeToInit() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSpeakToggleVisiableState(false);
                showPlayButton(false);
                mSurfaceViewContainer.removeAllViews();
            }
        });
        if (mPlayBT.getVisibility() == View.GONE) {
            toggle();
        }
    }

    /**
     *
     * @param play
     */
    private void showPlayButton(boolean play){
        if (play) {
            mPlayBT.setBackgroundResource(R.drawable.zanting);
        } else {
            mPlayBT.setBackgroundResource(R.drawable.bofang);
        }
        isconnected = play;
    }

    private void finishVideo() {
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(getString(R.string.hanguping));
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mVideoEngine.leaveChannel();
            }
        }).run();
    }

    private void playVideo() {
        boolean wificheck = getSharedPreferences("setting",
                MODE_PRIVATE).getBoolean("wificheck", true);
        if (wificheck && !checknetwork()) {
            ToastUtil.showtomain(AgoraMonitoring20CActivity.this, getString(R.string.not_wifi));
            return;
        }
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(getString(R.string.calling));
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mVideoEngine.setLogFile(getApplicationContext().getExternalFilesDir(null).toString() + "/agorasdk.log");
        mVideoEngine.enableVideo();
        mVideoEngine.muteLocalVideoStream(true);
        mVideoEngine.muteLocalAudioStream(true);
        mVideoEngine.setEnableSpeakerphone(true);
        mVideoEngine.muteAllRemoteVideoStreams(false);
        mVideoEngine.setVideoProfile(20);
        mVideoEngine.joinChannel(
                mVideoEngine.VendorKey,
                mPhoneNumber,
                "" /*optionalInfo*/,
                ACCOUNT_MASK + Integer.parseInt(mAccount)/*optionalUid*/);
        setSpeakToggleVisiableState(true);
    }

    private void invite() {
        Intent intent = new Intent(Constants.AGORA_VIDEO_MEETING_INVITE);
        intent.putExtra(Constants.AGORA_ID, Long.parseLong(mAccount)); //LONG
        intent.putExtra(Constants.AGORA_NUMBER, Long.parseLong(mBindRobotID)); //LONG
        intent.putExtra(Constants.AGORA_ROLE, "User"); //String
        intent.putExtra(Constants.AGORA_NICKNAME, Utils.getAccount(this)); //String
        intent.putExtra(Constants.AGORA_CHANNEL_ID, mPhoneNumber); //String
        intent.putExtra(Constants.AGORA_TYPE, "Robot"); //String
        intent.putExtra(Constants.AGORA_MODE, "monitor"); //String
        sendBroadcast(intent);
    }

    private BroadcastReceiver mRNameBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String rname = intent.getStringExtra("rname");
            getSharedPreferences("robotname", MODE_PRIVATE).edit()
                    .putString("name", rname).commit();
        }
    };

    BroadcastReceiver neterror = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (manager != null) {

                // 获取网络连接管理的对象

                NetworkInfo info = manager.getActiveNetworkInfo();

                if (info != null && info.isConnected()) {

                    // 判断当前网络是否已经连接

                    if (info.getState() == NetworkInfo.State.CONNECTED
                            && !info.isAvailable()) {
                        //TODO
                        StartUtil.startintent(AgoraMonitoring20CActivity.this,
                                ConnectActivity.class, "finish");
                    }

                }

            }
        }
    };

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
                // ensure remote video view setup
                if (isconnected) {
                    return;
                }
                showPlayButton(true);
                final SurfaceView remoteView = mVideoEngine.getSurfaceView(getApplicationContext());
                remoteView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isconnected) {
                            toggle();
                        }
                    }
                });
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                remoteView.setLayoutParams(params);
                mSurfaceViewContainer.addView(remoteView);
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
    public void onUserOffline(int uid, int reason) {
        finishVideo();
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {

    }

    @Override
    public void onUpdateSessionStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        resumeToInit();
    }

    @Override
    public void onError(int err) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        invite();
        mHandler.sendEmptyMessage(1);
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //    mSpeakIV.setEnabled(false);
            sendcmd("start", v);
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    sendcmd("start", v);
                }
            }, 1000, 1000);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //   mSpeakIV.setEnabled(true);
            sendcmd("stop", v);
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL){
            //   mSpeakIV.setEnabled(true);
            sendcmd("stop", v);
        }
        return true;
    }

    private void sendcmd(String flag, final View v) {
        if (flag.equals("start")) {
            switch (v.getId()) {
                case R.id.up:
                    execute("forward");
                    HandlerUtil.sendmsg(mHandler, "up", 0);
                    break;
                case R.id.left:
                    execute("turn_left");
                    HandlerUtil.sendmsg(mHandler, "left", 0);
                    break;
                case R.id.down:
                    execute("back");
                    HandlerUtil.sendmsg(mHandler, "down", 0);
                    break;
                case R.id.right:
                    execute("turn_right");
                    HandlerUtil.sendmsg(mHandler, "right", 0);
                    break;
                default:
                    break;
            }

        } else {
            switch (v.getId()) {
                case R.id.up:
                    execute("stop");
                    break;
                case R.id.left:
                    execute("stop");
                    break;
                case R.id.down:
                    execute("stop");
                    break;
                case R.id.right:
                    execute("stop");
                    break;
                default:
                    break;
            }
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = null;
        }

    }

    private void toggle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayBT.getVisibility() == View.VISIBLE) {
                    //    mSpeakIV.setVisibility(View.GONE);
                    mPlayBT.setVisibility(View.GONE);
                } else {
                    //    mSpeakIV.setVisibility(View.VISIBLE);
                    mPlayBT.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void execute(String execode) {
        Constants.execode = execode;
        Intent intent = new Intent();
        intent.setAction(Constants.Move_aciton);
        sendBroadcast(intent);
    }

}
