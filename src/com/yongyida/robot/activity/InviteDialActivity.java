package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.video.av.VideoSizeType;
import com.yongyida.robot.video.comm.Size;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.widget.CircleImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2016/5/11 0011.
 */
public class InviteDialActivity extends BaseVideoActivity implements View.OnClickListener{
    public static final String TAG = "ActivityInvite";
    public static final int INVITE_DURATION_TIME = 1000*10;  // 邀请持续时间（单位：毫秒）

    private CircleImageView mImageView;
    private TextView mTvMessage;
    private Button mHangup;
    private Handler mHandler = new Handler();
    private String mRole;
    private long mId;
    private String mUserName;
    private String mPicture;
    private String mNumberType;
    private long mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Intent intent = getIntent();
        mRole = intent.getStringExtra("role");
        mId = intent.getLongExtra("id", 0);
        mUserName = intent.getStringExtra("username");
        mPicture = intent.getStringExtra("picture");
        mNumberType = intent.getStringExtra("numbertype");
        mNumber = intent.getLongExtra("number", 0);

//         /* /media/invite/response */
//        BroadcastReceiverRegister.reg(this, new String[]{Constants.CONNECTION_REQUEST}, mConnectionResponseBR);
//
//        /* /media/reply */
//        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_REPLY}, mMediaReplyBR);

        /* 登入房间返回 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.LOGIN_VIDEO_ROOM_RESPONSE}, mLoginVideoRoomResponseBR);

        meetingInvite();
    }

//    BroadcastReceiver mConnectionResponseBR = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            mRoomID = intent.getIntExtra(Constants.RoomID, -1);
//            mIp = intent.getStringExtra(Constants.MediaTcpIp);
//            mPort = intent.getIntExtra(Constants.MediaTcpPort, -1);
//            User user = new User("User", 100227);
//            //  User user = new User("User", 100069);
//            YYDSDKHelper.getInstance().setUser(user);
//            YYDVideoServer.getInstance().getMeetingInfo().setOwner("User", 100069, "qqq");
//            YYDVideoServer.getInstance().getMeetingInfo().addUser(new User("User", 100069));
//            YYDVideoServer.getInstance().getMeetingInfo().setVideoServer_Tcp(mRoomID,
//                    mIp, mPort);
//        }
//    };
//
//    BroadcastReceiver mMediaReplyBR = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    YYDVideoServer.getInstance().connect(
//                            mIp,
//                            mPort);
//                    Intent itt = new Intent(Constants.LOGIN_VIDEO_ROOM);
//                    itt.putExtra(Constants.RoomID, mRoomID);
//                    itt.putExtra(Constants.MediaTcpIp, mIp);
//                    itt.putExtra(Constants.MediaTcpPort, mPort);
//                    sendBroadcast(itt);
//                }
//            }).start();
//
//
//        }
//    };

    BroadcastReceiver mLoginVideoRoomResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String send_host = intent.getStringExtra("send_host");
            int send_port = intent.getIntExtra("send_port", -1);
            YYDVideoServer.getInstance().getMeetingInfo().setVideoServer_Udp(send_host, send_port);
            YYDVideoServer.getInstance().getMeetingInfo().setAtRooming(true);
            Intent i = new Intent(InviteDialActivity.this, ActivityMeeting.class);
            i.putExtra("EnableSend", true);
            i.putExtra("EnableRecv", true);
            startActivity(i);
            finish();
        }
    };

    @Override
    protected void initView() {
        mImageView = (CircleImageView) findViewById(R.id.iv_inviter);
        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mHangup = (Button) findViewById(R.id.btn_hangup);

        mImageView.setImageBitmap(loadPicture(mPicture));
        mTvMessage.setText(("正在呼叫：" + mUserName));
        mHangup.setOnClickListener(this);
    }

    private Bitmap loadPicture(String path) {
        Bitmap bitmap = null;

        if (path != null && Utils.fileExists(path)) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
                bitmap = BitmapFactory.decodeStream(fis);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_default);
        }
        return bitmap;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_hangup:
                handCancel();
                break;
            default:
                break;
        }
    }

    public void meetingInvite() {
        log.d(TAG, "meetingInvite()");

        String numberType = null;
        long number = 0;
        if (mNumberType != null && mNumberType.length() > 0) {
            numberType = mNumberType;
            number = mNumber;
        }
        else if (mRole != null && mRole.length() > 0) {
            numberType = mRole;
            number = mId;
        }
        log.d(TAG, "numberType: " + numberType + ", number: " + number);

        // 根据用户配置项返回视频大小
        Size videoSize = VideoSizeType.getVideoSize(Config.getVideoSizeType());

        // 设置视频参数
        YYDVideoServer.getInstance().setVideoParam(videoSize, Config.getFrameRateType(), Config.getBitRateType());

        // 设置音频参数
        YYDVideoServer.getInstance().setAudioParam(Config.getSampleRate(), Config.getChannel(), Config.getAudioFormat());

        Intent intent = new Intent();
        intent.setAction(Constants.VIDEO_REQUEST);
        sendBroadcast(intent);

        YYDLogicServer.getInstance().meetingInvite("视频会议", //会议名称
                numberType,
                number,
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

        //10秒无人接听自动挂断
        timerCancel(INVITE_DURATION_TIME);
    }

    private Runnable timerCancelRunnable = new Runnable() {
        public void run() {
            log.d(TAG, "Timeout close invite for none answer.");
            inviteCancel();
        }
    };

    /**
     * 手动挂断
     * @param
     * @return
     *
     */
    private void handCancel() {
        log.d(TAG, "handCancel()");
        mHandler.removeCallbacks(timerCancelRunnable);
        inviteCancel();
    }

    /**
     * 定时挂断
     * @param
     * @return
     *
     */
    private void timerCancel(int delay) {
        mHandler.postDelayed(timerCancelRunnable, delay);
    }

    public void inviteCancel() {
        log.d(TAG, "inviteCancel()");

        String numberType = null;
        long number = 0;
        if (mNumberType != null && mNumberType.length() > 0) {
            numberType = mNumberType;
            number = mNumber;
        }
        else if (mRole != null && mRole.length() > 0) {
            numberType = mRole;
            number = mId;
        }

        YYDLogicServer.getInstance().meetingInviteCancel(numberType, number, new CmdCallBacker() {
            public void onSuccess(Object arg) {
                log.d(TAG, "meetingInviteCancel success");
                finish();
            }

            public void onFailed(int error) {
                log.d(TAG, "meetingInviteCancel failed, error: " + error);
                // 挂 断失败1秒后重试
                timerCancel(1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

}
