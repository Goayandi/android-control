package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/7/9 0009.
 */
public class InteractActivity extends Activity implements View.OnClickListener {
    private final static int RESUME = 1;
    private static final String TAG = "InteractActivity";
    private ImageView mSpeakIV;
    protected AudioManager audioManager;
    private RecognizerDialog mDialog;
    private boolean mMicroInitMute = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESUME:
                    mSpeakIV.setEnabled(true);
                    mSpeakIV.setImageResource(R.drawable.speech);
                    audioManager.setSpeakerphoneOn(true);
                    audioManager.setMicrophoneMute(mMicroInitMute);
                    mDialog.setCancelable(true);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);
        audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        mSpeakIV = (ImageView) findViewById(R.id.iv_speak);
        mSpeakIV.setOnClickListener(this);
    }


    InitListener init = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Log.i("Error", "错误码为:" + code);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_speak:
                if (audioManager.isMicrophoneMute()) {
                    audioManager.setMicrophoneMute(false);
                    mMicroInitMute = true;
                }
                mSpeakIV.setEnabled(false);
                audioManager.setSpeakerphoneOn(false);
                SpeechUtility.createUtility(this, "appid="
                        + getString(R.string.app_id));
                if (mDialog == null) {
                    mDialog = new RecognizerDialog(InteractActivity.this, init);

                    Utils.SystemLanguage language = Utils.getLanguage(InteractActivity.this);
                    if (Utils.SystemLanguage.CHINA.equals(language)) {
                        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                    } else if (Utils.SystemLanguage.ENGLISH.equals(language)) {
                        mDialog.setParameter(SpeechConstant.LANGUAGE, "en_us");
                    }
                    //	mDialog.setParameter(SpeechConstant.ACCENT, "vinn");
                    mDialog.setParameter("asr_sch", "1");
                    mDialog.setParameter("nlp_version", "2.0");
                    mDialog.setParameter("dot", "0");
                }
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mHandler.sendEmptyMessage(RESUME);
                            mDialog.dismiss();
                        }
                    }
                }, 15000);
                mDialog.setListener(new RecognizerDialogListener() {

                    @Override
                    public void onResult(RecognizerResult result, boolean arg1) {
                        Log.i("Result", "result:" + result.getResultString());
                        try {
                            JSONObject jo = new JSONObject(result.getResultString());
                            String text = jo.getString("text");
                            //TODO
                            Log.e(TAG, "text:" + text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(RESUME);
                    }

                    @Override
                    public void onError(SpeechError error) {
                        Log.i("errorXF", "errorXF:" + error.getErrorDescription());
                        mHandler.sendEmptyMessage(RESUME);
                    }
                });
                mDialog.show();
                mDialog.setCancelable(false);
                mSpeakIV.setImageResource(R.drawable.dianjishi);
                break;
        }
    }
}
