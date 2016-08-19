package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.ronglianyun.SDKCoreHelper;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.video.comm.log;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/6/12 0012.
 */
public class InviteActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "InviteActivity";
    private static final int ERROR_CODE = 1;
    /**
     * 呼叫唯一标识号
     */
    protected String mCallId;
    /**
     * 通话号码
     */
    protected String mCallNumber;
    /**
     * 会议id
     **/
    protected String mConferenceId;
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected int mSoundId;
    protected int mStreamId;
    protected Timer mTimer;
    protected String mCallName;
    protected String mAccount;
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR_CODE:
                    ToastUtil.showtomain(InviteActivity.this, "code:" + msg.arg1);
                    break;
            }
        }
    };
    private ECVoIPCallManager.OnVoIPListener mOnVoIPListener;
    private Button mAcceptBT;
    private Button mRefuseBT;
    private OnChatReceiveListener mOnChatReceiveListener;
    private final static String SCREEN_ON = "screen_on";
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        mCallId = getIntent().getStringExtra(ECDevice.CALLID);
        mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);

//        mCallName = getIntent().getStringExtra(Constants.CALL_NO);
//        mConferenceId = getIntent().getStringExtra(Constants.CONFERENCE_ID);

        String[] infos = getIntent().getExtras().getStringArray(ECDevice.REMOTE);
        mAccount = Utils.getAccount(this);
        mConferenceId = "";
        for (String s : infos) {
            if (s.startsWith("confid=")) {
                mConferenceId = s.substring(7);
            }
            if (s.startsWith("dpname=")) {
                mCallName = s.substring(7);
            }
        }

        init();
        mOnVoIPListener = new MyOnVoIPListener();
        mOnChatReceiveListener = new MyOnChatReceiveListener();
        ECDevice.getECVoIPCallManager().setOnVoIPCallListener(mOnVoIPListener);
        SDKCoreHelper.getInstance().registOnChatReceiveListener(mOnChatReceiveListener);
        playCallSound();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hangup();
                    }
                });
            }
        }, 25000);
        PowerManager pm =(PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                SCREEN_ON);
        mWakeLock.acquire();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void init() {
        ((TextView) findViewById(R.id.tv_username)).setText(mCallName);
        mAcceptBT = (Button) findViewById(R.id.btn_accept);
        mRefuseBT = (Button) findViewById(R.id.btn_refuse);
        mAcceptBT.setOnClickListener(this);
        mRefuseBT.setOnClickListener(this);
    }

    private class MyOnVoIPListener implements ECVoIPCallManager.OnVoIPListener {

        @Override
        public void onDtmfReceived(String s, char c) {

        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voIPCall) {
            // 接收VoIP呼叫事件回调
            if (voIPCall == null) {
                Log.e(TAG, "handle call event error , voipCall null");
                return;
            }
            switch (voIPCall.callState) {
                case ECCALL_PROCEEDING:
                    break;
                case ECCALL_ALERTING:
                    break;
                case ECCALL_ANSWERED:
                    Log.e(TAG, "ECCALL_ANSWERED");
                    break;
                case ECCALL_FAILED:
                    break;
                case ECCALL_RELEASED:
                    finish();
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

        }
    }

    /**
     * 播放拨号响铃
     */
    public void playCallSound() {
        log.d(TAG, "playCallSound()");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        mAudioManager.setSpeakerphoneOn(false);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        mSoundId = mSoundPool.load(this, R.raw.sedna, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                try {
                    mStreamId = mSoundPool.play(mSoundId, // 声音资源
                            0.5f, // 左声道
                            0.5f, // 右声道
                            1, // 优先级，0最低
                            -1, // 循环次数，0是不循环，-1是永远循环
                            1); // 回放速度，0.5-2.0之间。1为正常速度
                } catch (Exception e) {
                    log.e(TAG, "SoundPool play exception: " + e);
                }
            }
        });
    }

    /**
     * 停止播放拨号响铃
     */
    public void stopCallSound() {
        if (mSoundPool != null) {
            mSoundPool.stop(mStreamId);
            mSoundPool.release();
            mSoundPool = null;
        }

        if (mAudioManager != null) {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    private void enterMeetingRoom() {
        if (TextUtils.isEmpty(mConferenceId)) {
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.putExtra(Constants.CALL_ID, mCallId);
            intent.putExtra(Constants.IS_VIDEO, true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, TestMeetingActivity.class);
            intent.putExtra(Constants.HOST, false);
            intent.putExtra(Constants.CONFERENCE_ID, mConferenceId);
            intent.putExtra(Constants.CALL_ID, mCallId);
            intent.putExtra(Constants.CALL_NAME, mCallName);
            startActivity(intent);
        }

        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                accept();
                break;
            case R.id.btn_refuse:
                hangup();
                break;
            default:
                break;
        }
    }

    private void accept(){
        mAcceptBT.setEnabled(false);
        mRefuseBT.setEnabled(false);
        SDKCoreHelper.getVoIPCallManager().acceptCall(mCallId);
        //    sendAcceptMessage(mCallName);
        stopCallSound();
        enterMeetingRoom();
    }

    private void hangup(){
        mAcceptBT.setEnabled(false);
        mRefuseBT.setEnabled(false);
        SDKCoreHelper.getVoIPCallManager().rejectCall(mCallId, SdkErrorCode.REMOTE_CALL_BUSY);
        stopCallSound();
        //    sendRejectMessage(mCallName);
        finish();
    }

    /**
     * 接受邀请
     *
     * @param to
     */
    private void sendAcceptMessage(String to) {
        try {
            String msgType = Constants.MEETING_INVITE_REPLY;
            String body = Constants.INVITE_ACCEPT;

            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setForm(mAccount);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(to);
            msg.setSessionId(to);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData(Constants.MESSAGE_PREFIX + msgType);

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
                    //                Message msg = mHandler.obtainMessage(ERROR_CODE);
                    //                msg.arg1 = error.errorCode;
                    //                mHandler.sendMessage(msg);
                    // 将发送的消息更新到本地数据库并刷新UI
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

    /**
     * 拒绝邀请
     *
     * @param to
     */
    private void sendRejectMessage(String to) {
        try {
            String msgType = Constants.MEETING_INVITE_REPLY;
            String body = Constants.INVITE_REJECT;

            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setFrom(mAccount);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(to);
            msg.setSessionId(to);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData(Constants.MESSAGE_PREFIX + msgType);

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
                    //                Message msg = mHandler.obtainMessage(ERROR_CODE);
                    //                msg.arg1 = error.errorCode;
                    //                mHandler.sendMessage(msg);
                    // 将发送的消息更新到本地数据库并刷新UI
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

    private class MyOnChatReceiveListener implements OnChatReceiveListener {

        @Override
        public void OnReceivedMessage(ECMessage ecMessage) {
            if (ecMessage.getType() == ECMessage.Type.TXT) {
                // 在这里处理文本消息
//                ECTextMessageBody textMessageBody = (ECTextMessageBody) ecMessage.getBody();
//                if (textMessageBody != null) {
//                    if (ecMessage.getUserData().equals(Constants.MESSAGE_PREFIX + Constants.MEETING_CANCEL_INVITE)) {
//                        if (textMessageBody.getMessage().equals(mConferenceId)) {
//                            finish();
//                        }
//                    }
//
//                }
            }
        }

        @Override
        public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

        }

        @Override
        public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage ecGroupNoticeMessage) {

        }

        @Override
        public void onOfflineMessageCount(int i) {

        }

        @Override
        public int onGetOfflineMessage() {
            return 0;
        }

        @Override
        public void onReceiveOfflineMessage(List<ECMessage> list) {

        }

        @Override
        public void onReceiveOfflineMessageCompletion() {

        }

        @Override
        public void onServicePersonVersion(int i) {

        }

        @Override
        public void onReceiveDeskMessage(ECMessage ecMessage) {

        }

        @Override
        public void onSoftVersion(String s, int i) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        stopCallSound();
        SDKCoreHelper.getInstance().unRegistOnChatReceiveListener(mOnChatReceiveListener);
    }
}
