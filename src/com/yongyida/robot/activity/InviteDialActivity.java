package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.video.av.VideoSizeType;
import com.yongyida.robot.video.comm.Size;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.User;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.widget.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

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
    private String mUserName;   //接听方的昵称
    private String mNumberType;  //接听方的类型
    private long mNumber;     // 接听方的号
    private String mRole;   //拨打方的角色
    private int mId;        //拨打方的id
    private String mPicture;
    private int mRoomID;
    private String mIp;
    private int mPort;
    private int mReply;
    private final static int HANG_UP = 0;
    private final static int BOTH_VIDEO_AND_AUDIO = 1;
    private final static int AUDIO_ONLY = 2;
    private final static int VIDEO_ONLY = 3;
    private Handler mReplyHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANG_UP:
                    ToastUtil.showtomain(InviteDialActivity.this, getString(R.string.hang_up_opposite));
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Intent intent = getIntent();
        mRole = intent.getStringExtra("role");
        mId = intent.getIntExtra("id", 0);
        mUserName = intent.getStringExtra("username");
        mPicture = intent.getStringExtra("picture");
        mNumberType = intent.getStringExtra("numbertype");
        mNumber = intent.getLongExtra("number", 0);

        User user = new User("User", getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0));
        YYDSDKHelper.getInstance().setUser(user);

         /* /media/invite/response */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.CONNECTION_REQUEST}, mConnectionResponseBR);

        /* /media/reply */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_REPLY}, mMediaReplyBR);

        /* /media/cancel/response 取消邀请 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_INVITE_CANCEL}, mMediaInviteCancelBR);

        /* 登入房间返回 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.LOGIN_VIDEO_ROOM_RESPONSE}, mLoginVideoRoomResponseBR);

        meetingInvite();
    }

    private void addFriend() {
        addRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                mNumber, getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
    }

    /**
     * 添加好友
     *
     * @param id
     * @param frid
     * @param session
     */
    public void addRobotFriend(final int id, final Long frid, final String session) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id + "");
                params.put("frid", frid + "");
                params.put("session", session);
                try {
                    NetUtil.getinstance().http(Constants.ADD_ROBOT_FRIEND, params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            Log.i(TAG, json.toString());
                            try {
                                int ret = json.getInt("ret");
                                switch (ret) {
                                    case -1:
                                        Log.i(TAG, "传入用户信息不存在");
                                        break;
                                    case 0:
                                        Log.i(TAG, "成功");
                                        break;
                                    case 1:
                                        Log.i(TAG, "用户信息为空");
                                        break;
                                    case 2:
                                        Log.i(TAG, "机器人信息为空");
                                        break;
                                    case 3:
                                        Log.i(TAG, "机器人id或序列号不存在");
                                        break;
                                    case 4:
                                        Log.i(TAG, "超过最大好友数(默认1000个)");
                                        break;
                                    case 5:
                                        Log.i(TAG, "手机用户已经添加该机器人为好友");
                                        break;
                                    default:
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void error(String errorresult) {

                        }
                    }, InviteDialActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    BroadcastReceiver mConnectionResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.RET, -1);
            switch (ret) {
                case 0:
                    mRoomID = intent.getIntExtra(Constants.RoomID, -1);
                    mIp = intent.getStringExtra(Constants.MediaTcpIp);
                    mPort = intent.getIntExtra(Constants.MediaTcpPort, -1);
                    if (!YYDVideoServer.getInstance().isVideoing()) {
                        Log.e(TAG, "isVideoing");
                        // 设置会议发起人为用户自己
                        YYDVideoServer.getInstance().getMeetingInfo().setOriginator(
                                YYDSDKHelper.getInstance().getUser(),
                                YYDSDKHelper.getInstance().getUser());
//                        YYDVideoServer.getInstance().getMeetingInfo().addRoomUser(new RoomUser(
//                                YYDSDKHelper.getInstance().getUser().getRole(),
//                                YYDSDKHelper.getInstance().getUser().getId(),
//                                YYDSDKHelper.getInstance().getUser().getUserName()
//                        ));
                        // 保存房间roomId和视频服务器VideoServerIp, VideoServerPort
                        YYDVideoServer.getInstance().getMeetingInfo().setVideoServer_Tcp(mRoomID,
                                mIp, mPort);
                    }
                    break;
                case 1:
                    ToastUtil.showtomain(InviteDialActivity.this, getString(R.string.not_online_opposite));
                    Log.i(TAG, getString(R.string.not_online_opposite));
                    finish();
                    break;
                case 2:
                    ToastUtil.showtomain(InviteDialActivity.this, getString(R.string.room_not_exist));
                    Log.i(TAG, getString(R.string.room_not_exist));
                    finish();
                    break;
                case 3:
                    ToastUtil.showtomain(InviteDialActivity.this, getString(R.string.some_request_not_handle));
                    Log.i(TAG, getString(R.string.some_request_not_handle));
                    finish();
                    break;
            }


        }
    };

    BroadcastReceiver mMediaReplyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int reply = intent.getIntExtra("reply", -1);
                    if (reply != -1) {
                        mReply = reply;
                    }
                    switch (reply) {
                        case HANG_UP:
                            mReplyHandler.sendEmptyMessage(HANG_UP);
                            break;
                        case BOTH_VIDEO_AND_AUDIO:
                        case AUDIO_ONLY:
                        case VIDEO_ONLY:
                            addFriend();
                            YYDVideoServer.getInstance().connect(
                                    mIp,
                                    mPort);
                            Intent itt = new Intent(Constants.LOGIN_VIDEO_ROOM);
                            itt.putExtra(Constants.ID, intent.getIntExtra("invite_id", -1));
                            itt.putExtra(Constants.RoomID, mRoomID);
                            itt.putExtra(Constants.MediaTcpIp, mIp);
                            itt.putExtra(Constants.MediaTcpPort, mPort);
                            itt.putExtra(Constants.TypeRole, mRole);
                            sendBroadcast(itt);
                            break;
                    }
                }
            }).start();

        }
    };

    BroadcastReceiver mMediaInviteCancelBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.RET, -1);
            if (ret == 0) {
                finish();
            }
        }
    };

    BroadcastReceiver mLoginVideoRoomResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.RET, -1);
            if (ret == 0) {
                Intent i = new Intent(InviteDialActivity.this, MeetingActivity.class);
                i.putExtra("EnableSend", true);
                i.putExtra("EnableRecv", true);
                i.putExtra("id", mId);
                i.putExtra("role", mRole);
                i.putExtra("room_id", mRoomID);
                startActivity(i);
                finish();
            } else {
                ToastUtil.showtomain(InviteDialActivity.this, "进入房间出错");
                finish();
            }
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

        // 根据用户配置项返回视频大小
        Size videoSize = VideoSizeType.getVideoSize(Config.getVideoSizeType());

        // 设置视频参数
        YYDVideoServer.getInstance().setVideoParam(videoSize, Config.getFrameRateType(), Config.getBitRateType());

        // 设置音频参数
        YYDVideoServer.getInstance().setAudioParam(Config.getSampleRate(), Config.getChannel(), Config.getAudioFormat());

        Intent intent = new Intent();
        intent.setAction(Constants.VIDEO_REQUEST);
        intent.putExtra("id", mId);
        intent.putExtra("role", mRole);
        if (mPicture != null) {
            intent.putExtra("picture", mPicture);
        }
        intent.putExtra("numberType", mNumberType);
        intent.putExtra("number", mNumber);
        intent.putExtra("nickname", mUserName);
        sendBroadcast(intent);

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

    /**
     * 取消邀请
     */
    public void inviteCancel() {
        Intent intent = new Intent(Constants.BR_CANCEL_DIAL);
        intent.putExtra("id", mId);
        intent.putExtra("role", mRole);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        log.d(TAG, "onDestroy()");
        try {
            if (mConnectionResponseBR != null) {
                unregisterReceiver(mConnectionResponseBR);
            }
            if (mMediaReplyBR != null) {
                unregisterReceiver(mMediaReplyBR);
            }
            if (mLoginVideoRoomResponseBR != null) {
                unregisterReceiver(mLoginVideoRoomResponseBR);
            }
            if (mMediaInviteCancelBR != null) {
                unregisterReceiver(mMediaInviteCancelBR);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

}
