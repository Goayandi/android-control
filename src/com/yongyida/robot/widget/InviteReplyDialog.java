package com.yongyida.robot.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.video.comm.log;

public class InviteReplyDialog extends Dialog {
    public static final String TAG = "InviteReplyDialog";

    public InviteReplyDialog(Context context) {
        super(context);
    }

    public InviteReplyDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String pictureUrl;
        private String userName;
        private String message;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;

        protected AudioManager mAudioManager;
        protected SoundPool mSoundPool;
        protected int mSoundId;
        protected int mStreamId;

        public Builder(Context context) {
            this.context = context;
            playCallSound();
        }

        public Builder setPictureUrl(String pictureUrl) {
            this.pictureUrl = pictureUrl;
            return this;
        }

        public Builder setUserName(String name) {
            this.userName = name;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setPositiveButton(DialogInterface.OnClickListener listener) {
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(DialogInterface.OnClickListener listener) {
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * 播放拨号响铃
         *
         */
        @SuppressWarnings("deprecation")
        public void playCallSound() {
            log.d(TAG, "playCallSound()");

            mAudioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setMode(AudioManager.MODE_RINGTONE);
            mAudioManager.setSpeakerphoneOn(false);
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
            mSoundId = mSoundPool.load(context, R.raw.sedna, 1);
            mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    try {
                        mStreamId = mSoundPool.play(mSoundId, // 声音资源
                                1.0f, // 左声道
                                1.0f, // 右声道
                                1, // 优先级，0最低
                                -1, // 循环次数，0是不循环，-1是永远循环
                                1); // 回放速度，0.5-2.0之间。1为正常速度
                    }
                    catch (Exception e) {
                        log.e(TAG, "SoundPool play exception: " + e);
                    }
                }
            });
        }

        /**
         * 播放拨号响铃
         *
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

        @SuppressLint("InflateParams")
        public InviteReplyDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final InviteReplyDialog dialog = new InviteReplyDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_invite_reply, null);
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            // 设置用户头像
            CircleImageView imgView = (CircleImageView) layout.findViewById(R.id.iv_inviter);
            if (pictureUrl != null) {
                Bitmap bt = BitmapFactory.decodeFile(pictureUrl);
                imgView.setImageBitmap(bt);
            }
            else {
                imgView.setImageResource(R.drawable.ic_launcher);
            }

            // 设置用户名称
            if (userName != null)
                ((TextView) layout.findViewById(R.id.tv_username)).setText(userName);

            // 设置消息提示
            if (message != null)
                ((TextView) layout.findViewById(R.id.tv_message)).setText(message);

            // 设置接听按扭
            if (positiveButtonClickListener != null) {
                ((Button) layout.findViewById(R.id.btn_positiveButton)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }

            // 设置拒绝按扭
            if (negativeButtonClickListener != null) {
                ((Button) layout.findViewById(R.id.btn_negativeButton)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
            dialog.setCancelable(false);
            dialog.setContentView(layout);
            return dialog;
        }
    }
}

