package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.video.comm.log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/9/2 0002.
 */
public class AgoraInviteActivity extends Activity {
    private static final String TAG = "AgoraInviteActivity";
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected Timer mTimer;
    protected int mSoundId;
    protected int mStreamId;
    private Button mAcceptBT;
    private Button mRefuseBT;
    private final static String SCREEN_ON = "screen_on";
    private PowerManager.WakeLock mWakeLock;
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };
    private String mChannelID;
    private long mCallID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_invite);
        initData();
        initLayout();
        initTimerAndSound();
        initBR();
    }

    private void initBR() {
        BroadcastReceiverRegister.reg(this, new String[]{Constants.AGORA_INVITE_CANCEL}, mInviteCancelBR);
    }

    private BroadcastReceiver mInviteCancelBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private void initData() {
        mChannelID = getIntent().getStringExtra(Constants.AGORA_CHANNEL_ID);
        mCallID = getIntent().getLongExtra(Constants.AGORA_ID, -1);
    }

    private void initTimerAndSound() {
        playCallSound();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        }, 23000);
    }

    private void initLayout() {
        ((TextView) findViewById(R.id.tv_username)).setText(mCallID + "");
        mAcceptBT = (Button) findViewById(R.id.btn_accept);
        mRefuseBT = (Button) findViewById(R.id.btn_refuse);
        mAcceptBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
        mRefuseBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refuse();
            }
        });

        PowerManager pm =(PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                SCREEN_ON);
        mWakeLock.acquire();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }


    /**
     * 播放拨号响铃
     */
    public void playCallSound() {
        log.d(TAG, "playCallSound()");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        mAudioManager.setSpeakerphoneOn(true);
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

    private void inviteReply(boolean accept) {
        if ("error".equals(Utils.getUserID(this))) {
            ToastUtil.showtomain(this, "账号出错");
            finish();
            return;
        }

        Intent intent = new Intent(Constants.AGORA_VIDEO_MEETING_INVITE_REPLY);
        intent.putExtra(Constants.AGORA_ID, Long.parseLong(Utils.getUserID(this)));
        intent.putExtra(Constants.AGORA_ROLE, "User");
        intent.putExtra(Constants.AGORA_INVITE_TYPE, "Robot");
        intent.putExtra(Constants.AGORA_INVITE_ID, mCallID + "");
        if (accept) {
            intent.putExtra(Constants.AGORA_REPLY, 1);
        } else {
            intent.putExtra(Constants.AGORA_REPLY, 0);
        }
        sendBroadcast(intent);
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
        Intent intent = new Intent(this, VideoMeetingActivity.class);
        intent.putExtra(Constants.AGORA_CHANNEL_ID, mChannelID);
        intent.putExtra(Constants.AGORA_ID, mCallID);
        startActivity(intent);
        finish();
    }

    private void accept(){
        mAcceptBT.setEnabled(false);
        mRefuseBT.setEnabled(false);
        stopCallSound();
        inviteReply(true);
        enterMeetingRoom();
    }

    private void refuse(){
        mAcceptBT.setEnabled(false);
        mRefuseBT.setEnabled(false);
        stopCallSound();
        inviteReply(false);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.unRegisterReceiver(mInviteCancelBR, this);
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        stopCallSound();
    }
}
