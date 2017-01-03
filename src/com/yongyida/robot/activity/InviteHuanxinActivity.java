package com.yongyida.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EMNoActiveCallException;
import com.yongyida.robot.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/12 0012.
 */
public class InviteHuanxinActivity extends OriginalActivity implements View.OnClickListener{
    private Button mAcceptBT;
    private Button mRefuseBT;
    protected Timer mTimer;
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected int mSoundId;
    protected int mStreamId;
    private String mCallName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huanxin_invite);
        mCallName = getIntent().getStringExtra("username");
        init();
        playCallSound();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopCallSound();
                finish();
            }
        }, 50000);
    }

    private void init() {
        ((TextView) findViewById(R.id.tv_username)).setText(mCallName);
        mAcceptBT = (Button) findViewById(R.id.btn_accept);
        mRefuseBT = (Button) findViewById(R.id.btn_refuse);
        mAcceptBT.setOnClickListener(this);
        mRefuseBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                stopCallSound();
                mAcceptBT.setEnabled(false);
                startActivity(new Intent(this, ControlActivity.class)
                        .putExtra("username", mCallName)
                        .putExtra("isComingCall", true)
                        .putExtra("mode", "chat"));
                finish();
                break;
            case R.id.btn_refuse:
                mRefuseBT.setEnabled(false);
                stopCallSound();
                try {
                    EMChatManager.getInstance().rejectCall();
                } catch (EMNoActiveCallException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            default:
                break;
        }
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

    /**
     * 播放拨号响铃
     */
    public void playCallSound() {

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
                }
            }
        });
    }
}
