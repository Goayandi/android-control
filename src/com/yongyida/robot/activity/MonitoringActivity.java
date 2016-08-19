package com.yongyida.robot.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;
import com.yuntongxun.ecsdk.voip.video.OnCameraInitListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/6/24 0024.
 */
public class MonitoringActivity extends RlyVideoBaseActivity implements View.OnClickListener,
        View.OnTouchListener {
    private static final String TAG = "MonitoringActivity";
    private String mRobotId;
    private String mCurrentCallId;
    private SurfaceView mSurfaceView;
    private Button mPlayBT;
  //  private ImageView mSpeakIV;
    private Button mBackBT;

    private ImageView mUpIV;
    private ImageView mLeftIV;
    private ImageView mDownIV;
    private ImageView mRightIV;
    private TextView mMoveTv;

    private static Timer time = null;
    /**
     * 设置通话状态监听
     */
    private boolean isconnected = false;
    private ProgressDialog mProgressDialog;

    private long mLastClickTime = 0;

    private RecognizerDialog mDialog;
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Log.i("Error", "错误码为:" + code);
            }
        }
    };

    private BroadcastReceiver mRNameBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String rname = intent.getStringExtra("rname");
            getSharedPreferences("robotname", MODE_PRIVATE).edit()
                    .putString("name", rname).commit();
        }
    };
    private ECCaptureView mECCaptureView;
    private MyOnVoIPListener mOnVoIPListener;
    private boolean mIsVideo;
    private Button mSpeakToggle;
    private boolean controlMute = true; //监控静音标识 true表示静音状态

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        init();
    }

    private void init() {
        setWindowState();
        initLayout();
        initBroadcastReceiver();
        initVideo();
    }

    private void initVideo() {
        mRobotId = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                "robotid", null);
        mOnVoIPListener = new MyOnVoIPListener();
        ECDevice.getECVoIPCallManager().setOnVoIPCallListener(mOnVoIPListener);
        mIsVideo = getIntent().getBooleanExtra(Constants.IS_VIDEO, false);
        if (mIsVideo) {
            mCurrentCallId = getIntent().getStringExtra(Constants.CALL_ID);
            mSurfaceView.setVisibility(View.VISIBLE);
            ECDevice.getECVoIPSetupManager().setVideoView(mSurfaceView, mECCaptureView);
            showPlayButton(false);
            toggle();
            showState(mECCaptureView, mIsVideo);
        }
    }

    private void initBroadcastReceiver() {
        BroadcastReceiverRegister.reg(this, new String[]{Constants.BATTERY}, mRNameBR);
        BroadcastReceiverRegister.reg(this,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION},
                neterror);
    }

    private void setWindowState(){
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void initLayout() {
        mPlayBT = (Button) findViewById(R.id.play);
        mPlayBT.setOnClickListener(this);
//        mSpeakIV = (ImageView) findViewById(R.id.speak);
//        mSpeakIV.setOnClickListener(this);
        mBackBT = (Button) findViewById(R.id.back);
        mBackBT.setOnClickListener(this);

        mUpIV = (ImageView) findViewById(R.id.up);
        mUpIV.setOnTouchListener(this);
        mLeftIV = (ImageView) findViewById(R.id.left);
        mLeftIV.setOnTouchListener(this);
        mDownIV = (ImageView) findViewById(R.id.down);
        mDownIV.setOnTouchListener(this);
        mRightIV = (ImageView) findViewById(R.id.right);
        mRightIV.setOnTouchListener(this);
        mMoveTv = (TextView) findViewById(R.id.move);

        mSpeakToggle = (Button) findViewById(R.id.mictoggole);
        mSpeakToggle.setOnClickListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.opposite_surface);
        mSurfaceView.setOnClickListener(this);
        mECCaptureView = (ECCaptureView) findViewById(R.id.ec_capture_view);
        mECCaptureView.setOnCameraInitListener(new OnCameraInitListener() {
            @Override
            public void onCameraInit(boolean result) {
                if (!result) ToastUtil.showtomain(MonitoringActivity.this, "摄像头被占用");
            }
        });
        setSpeakToggleVisiableState(false);
        mProgressDialog = new ProgressDialog(this);
    }



    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //    mSpeakIV.setEnabled(false);
            sendcmd("start", v);
            if (time != null) {
                time.cancel();
            }
            time = new Timer();
            time.scheduleAtFixedRate(new TimerTask() {

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (time != null)
                time.cancel();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setSpeakToggleVisiableState(boolean show) {
        if (show) {
            mSpeakToggle.setVisibility(View.VISIBLE);
        } else {
            mSpeakToggle.setVisibility(View.GONE);
        }
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
            if (time != null) {
                time.cancel();
            }
            time = null;
        }

    }

    Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
           if (msg.what == 1) {
                toggle();
            }

        };
    };


    private void execute(String execode) {
        Constants.execode = execode;
        Intent intent = new Intent();
        intent.setAction(Constants.Move_aciton);
        sendBroadcast(intent);
    }


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
                        StartUtil.startintent(MonitoringActivity.this,
                                ConnectActivity.class, "finish");
                    }

                }

            }
        }
    };



    private boolean checknetwork() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info.isAvailable() && info.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - mLastClickTime < 1000) {
                    mLastClickTime = currentTimeMillis;
                    ToastUtil.showtomain(MonitoringActivity.this, getString(R.string.dont_so_fast2));
                    return;
                }
                mLastClickTime = currentTimeMillis;
                if(!isconnected) {
                    playVideo();
                } else {
                    finishVideo();
                }
                break;
//            case R.id.speak:
//                mSpeakIV.setEnabled(false);
//                SpeechUtility.createUtility(this, "appid="
//                        + getString(R.string.app_id));
//                if (mDialog == null) {
//                    mDialog = new RecognizerDialog(MonitoringActivity.this, mInitListener);
//
//                    Utils.SystemLanguage language = Utils.getLanguage(MonitoringActivity.this);
//                    if (Utils.SystemLanguage.CHINA.equals(language)) {
//                        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//                    } else if (Utils.SystemLanguage.ENGLISH.equals(language)) {
//                        mDialog.setParameter(SpeechConstant.LANGUAGE, "en_us");
//                    }
//                    //	mDialog.setParameter(SpeechConstant.ACCENT, "vinn");
//                    mDialog.setParameter("asr_sch", "1");
//                    mDialog.setParameter("nlp_version", "2.0");
//                    mDialog.setParameter("dot", "0");
//                }
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//
//                    @Override
//                    public void run() {
//                        if (mDialog != null && mDialog.isShowing()) {
//                            mHandler.sendEmptyMessage(5);
//                            mDialog.dismiss();
//                        }
//                    }
//                }, 15000);
//                mDialog.setListener(new RecognizerDialogListener() {
//
//                    @Override
//                    public void onResult(RecognizerResult result, boolean arg1) {
//                        Log.i("Result", "result:" + result.getResultString());
//                        try {
//                            JSONObject jo = new JSONObject(result.getResultString());
//                            String text = jo.getString("text");
//                            Constants.text = text;
//                            Intent intent = new Intent();
//                            intent.setAction(Constants.Speech_action);
//                            sendBroadcast(intent);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        mHandler.sendEmptyMessage(5);
//                    }
//
//                    @Override
//                    public void onError(SpeechError error) {
//                        Log.i("errorXF", "errorXF:" + error.getErrorDescription());
//                        mHandler.sendEmptyMessage(5);
//                    }
//                });
//                mDialog.show();
//                mDialog.setCancelable(false);
//                mSpeakIV.setImageResource(R.drawable.dianjishi);
//                break;
            case R.id.back:
                this.onBackPressed();
                break;
            case R.id.opposite_surface:
                if (isconnected) {
                    if (mUpIV.getVisibility() == View.GONE) {
                        toggle();
                    } else {
                        if (mPlayBT.getVisibility() != View.GONE)
                            mPlayBT.setVisibility(View.GONE);
                        else
                            mPlayBT.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.mictoggole:
                setSpeakToggleState(!controlMute);
                break;
        }

    }


    private void playVideo(){

        boolean wificheck = getSharedPreferences("setting",
                MODE_PRIVATE).getBoolean("wificheck", true);
        if (wificheck && !checknetwork()) {
            ToastUtil.showtomain(MonitoringActivity.this, getString(R.string.not_wifi));
            return;
        }
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(getString(R.string.calling));
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mSurfaceView.setVisibility(View.VISIBLE);
        ECDevice.getECVoIPSetupManager().setVideoView(mSurfaceView, mECCaptureView);
        mCurrentCallId = establishVideo(mRobotId);
        if (TextUtils.isEmpty(mCurrentCallId)){
            ToastUtil.showtomain(this, getString(R.string.disconnected));
            finish();
        }
        Log.i(TAG, "callId:" + mCurrentCallId);
    //    showPlayButton(true);
        showState(mECCaptureView, mIsVideo);
        setSpeakerState(true);
        setSpeakToggleState(true);
    }

    private void finishVideo(){
        if (!TextUtils.isEmpty(mCurrentCallId)) {
            Log.i(TAG, "callId:" + mCurrentCallId);
            hangup(mCurrentCallId);
            if (mProgressDialog != null) {
                mProgressDialog.setMessage(getString(R.string.hanguping));
                mProgressDialog.show();
                mProgressDialog.setCanceledOnTouchOutside(false);
            }
        } else {
            finish();
        }
    }

    private void toggle() {
        if (mUpIV.getVisibility() == View.VISIBLE) {
            mUpIV.setVisibility(View.GONE);
            mDownIV.setVisibility(View.GONE);
            mLeftIV.setVisibility(View.GONE);
            mRightIV.setVisibility(View.GONE);
        //    mSpeakIV.setVisibility(View.GONE);
            mPlayBT.setVisibility(View.GONE);
            mMoveTv.setVisibility(View.GONE);
        } else {
            mUpIV.setVisibility(View.VISIBLE);
            mDownIV.setVisibility(View.VISIBLE);
            mLeftIV.setVisibility(View.VISIBLE);
            mRightIV.setVisibility(View.VISIBLE);
        //    mSpeakIV.setVisibility(View.VISIBLE);
            mPlayBT.setVisibility(View.VISIBLE);
            mMoveTv.setVisibility(View.VISIBLE);
        }
    }

    private void sendMuteMsg(boolean mute) {
        try {
            String msgType = "setMute";
            String body = mute + "";
            String from = Utils.getAccount(this);
            String to = getSharedPreferences("Receipt", MODE_PRIVATE).getString(
                    "robotid", null);
            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setFrom(from);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(to);
            msg.setSessionId(to);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData("msgType://" + msgType);

            ECTextMessageBody msgBody = new ECTextMessageBody(body);
            msg.setBody(msgBody);
            ECChatManager manager = ECDevice.getECChatManager();
            manager.sendMessage(msg, new ECChatManager.OnSendMessageListener() {
                @Override
                public void onSendMessageComplete(ECError error, ECMessage message) {
                    // 处理消息发送结果
                    if (message == null) {
                        return;
                    }
                }

                @Override
                public void onProgress(String msgId, int totalByte, int progressByte) {
                }
            });
        } catch (Exception e) {
            // 处理发送异常
            Log.e(TAG, "send message fail , e=" + e.getMessage());
        }

    }

    private void setSpeakToggleState(boolean state){
        if (state) {
            setMuteState(true);
            sendMuteMsg(true);
            mSpeakToggle.setBackgroundResource(R.drawable.icon_mute_on);
            controlMute = true;
        } else {
            setMuteState(false);
            sendMuteMsg(false);
            mSpeakToggle.setBackgroundResource(R.drawable.icon_mute_normal);
            controlMute = false;
        }
    }

    /**
     * 是否显示
     * @param v
     * @param show
     */
    private void showState(ECCaptureView v, boolean show) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        if (!show) {
            params.setMargins(0, -v.getHeight(), -v.getWidth(), 0);
        }
        v.setLayoutParams(params);
    }

    /**
     *
     * @param play
     */
    private void showPlayButton(boolean play){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPlayBT.getLayoutParams();
        params.height = Utils.dp2px(this, 80);
        params.width = Utils.dp2px(this, 80);
        if (play) {
            mPlayBT.setBackgroundResource(R.drawable.zanting);
        } else {
            mPlayBT.setBackgroundResource(R.drawable.bofang);
        }
        isconnected = play;
    }

    private class MyOnVoIPListener implements ECVoIPCallManager.OnVoIPListener{

        @Override
        public void onDtmfReceived(String s, char c) {

        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voIPCall) {
            if(voIPCall==null) 
            {
                Log.i(TAG, "null");
                return;
            }

            switch(voIPCall.callState){

                case ECCALL_ALERTING:
                    Log.i(TAG, "ECCALL_ALERTING");
                    //对方振铃
                    break;

                case ECCALL_PROCEEDING:
                    //呼叫中
                    Log.i(TAG, "ECCALL_PROCEEDING");
                    mHandler.sendEmptyMessage(1);
                    break;

                case ECCALL_ANSWERED:
                    //John接受了呼叫应答
                    Log.i(TAG, "ECCALL_ANSWERED");
                    showPlayButton(true);
                    setSpeakToggleVisiableState(true);
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    break;

                case ECCALL_FAILED:
                    Log.i(TAG, "ECCALL_FAILED");
                    //呼叫失败
                    resumeToInit();
                    ToastUtil.showtomain(MonitoringActivity.this, "呼叫失败" + voIPCall.reason);
                    break;

                case ECCALL_RELEASED:
                    Log.i(TAG, "ECCALL_RELEASED");
                    //无论是Tony还是John主动结束通话，双方都会进入到此回调
                    resumeToInit();
                    break;
                case ECCALL_PAUSED:
                    Log.i(TAG, "ECCALL_PAUSED");
                    break;
                case ECCALL_PAUSED_BY_REMOTE:
                    Log.i(TAG, "ECCALL_PAUSED_BY_REMOTE");
                    break;
                default:

                    break;

            }

        }

        @Override
        public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {
            Log.i(TAG, "w:" + videoRatio.getWidth() + ",h:" + videoRatio.getHeight());
        }
    }

    private void resumeToInit() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        setSpeakToggleVisiableState(false);
        showPlayButton(false);
        mCurrentCallId = "";
        mSurfaceView.setVisibility(View.INVISIBLE);
        if (mUpIV.getVisibility() == View.GONE) {
            toggle();
        }
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (time != null) {
            time.cancel();
        }

        Utils.unRegisterReceiver(neterror, this);
        Utils.unRegisterReceiver(mRNameBR, this);
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
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

}
