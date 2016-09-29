package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Administrator on 2016/7/9 0009.
 */
public class InteractActivity extends Activity implements View.OnClickListener {
    private final static int RESUME = 1;
    private static final String TAG = "InteractActivity";
    protected AudioManager audioManager;
    private View recordingContainer;
    private ImageView micImage;
    private TextView recordingHint;
    private View buttonPressToSpeak;
    private Drawable[] micImages;
    private String sendText;
    private SpeechRecognizer mIat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);
        audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        recordingContainer = findViewById(R.id.recording_container);
        micImage = (ImageView) findViewById(R.id.mic_image);
        recordingHint = (TextView) findViewById(R.id.recording_hint);
        buttonPressToSpeak =  findViewById(R.id.btn_press_to_speak);
        // 动画资源文件,用于录制语音时
        micImages = new Drawable[] { getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14) };
        buttonPressToSpeak.setOnTouchListener(new PressToSpeakListener());
        SpeechUtility.createUtility(InteractActivity.this, "appid="
                + getString(R.string.app_id));
        mIat = SpeechRecognizer.createRecognizer(InteractActivity.this, mInitListener);
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
    }


    private RecognizerListener mRecoListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            Log.e(TAG, "result:" + recognizerResult.getResultString());
            String text = parseIatResult(recognizerResult.getResultString());
            sendText += text;
            if (isLast) {
                Constants.text = sendText;
                sendText = "";
                Intent intent = new Intent();
                intent.setAction(Constants.Talk);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.e(TAG, "error:" + speechError.getErrorCode());
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
//				如果需要多候选结果，解析数组其他字段
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    /**
     * 按住说话listener
     *
     */
    class PressToSpeakListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
                    recordingContainer.setVisibility(View.VISIBLE);
                    recordingHint.setText(getString(R.string.move_up_to_cancel));
                    recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    audioManager.setSpeakerphoneOn(false);


                    mIat.startListening(mRecoListener);
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint.setText(getString(R.string.release_to_cancel));
                        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (event.getY() < 0) {
                        mIat.cancel();
                    } else {
                        mIat.stopListening();
                    }
                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    return false;
            }
        }
    }


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_speak:
//                SpeechUtility.createUtility(this, "appid="
//                        + getString(R.string.app_id));
//                mDialog.setListener(new RecognizerDialogListener() {
//
//                    @Override
//                    public void onResult(RecognizerResult result, boolean arg1) {
//                        Log.i("Result", "result:" + result.getResultString());
//                        try {
//                            JSONObject jo = new JSONObject(result.getResultString());
//                            String text = jo.getString("text");
//                            //TODO
//                            Log.e(TAG, "text:" + text);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        mHandler.sendEmptyMessage(RESUME);
//                    }
//
//                    @Override
//                    public void onError(SpeechError error) {
//                        Log.i("errorXF", "errorXF:" + error.getErrorDescription());
//                        mHandler.sendEmptyMessage(RESUME);
//                    }
//                });
//                break;
        }
    }
}
