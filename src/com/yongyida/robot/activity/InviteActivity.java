package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.MeetingReplyRequest;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.User;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.widget.CircleImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2016/4/13 0013.
 */
public class InviteActivity extends BaseVideoActivity implements OnClickListener {
    public static final String TAG = "InviteActivity";

    private CircleImageView mImageView;
    private TextView mTvMessage;
    private Button mHangup;

    private String mRole;
    private long mId;
    private String mUserName;
    private String mPicture;
    private String mNumberType;
    private long mNumber;
    private int mRoomID;

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

        mImageView = (CircleImageView) findViewById(R.id.iv_inviter);
        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mHangup = (Button) findViewById(R.id.btn_hangup);

        mImageView.setImageBitmap(loadPicture(mPicture));
        mTvMessage.setText(("正在呼叫：" + mUserName));
        mHangup.setOnClickListener(this);
        log.d(TAG, "mUserName:" + mUserName);

        YYDSDKHelper.getInstance().registerEventListener(mEventListener);

        /* /media/invite/response */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.CONNECTION_REQUEST}, mConnectionResponseBR);

        /* /media/reply */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_REPLY}, mMediaReplyBR);

        /* 登入房间返回 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.LOGIN_VIDEO_ROOM_RESPONSE}, mLoginVideoRoomResponseBR);
        meetingInvite();
    }

    BroadcastReceiver mLoginVideoRoomResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User user = new User("User", 100069);
                    YYDSDKHelper.getInstance().setUser(user);
                    YYDVideoServer.getInstance().getMeetingInfo().setVideoServer(
                            mRoomID,
                            "120.24.242.163",
                            8003);
                    YYDVideoServer.getInstance().connect("120.24.242.163", 8003);
                    YYDVideoServer.getInstance().enterRoom(new CmdCallBacker() {
                        @Override
                        public void onSuccess(Object o) {
                            Log.i(TAG, "success");
                            Intent i = new Intent(InviteActivity.this, ActivityMeeting.class);
                            i.putExtra("EnableSend", true);
                            i.putExtra("EnableRecv", true);
                            startActivity(i);
                            finish();
                        }

                        @Override
                        public void onFailed(int i) {
                            Log.i(TAG, "fail");
                        }
                    });
                }
            }).start();
        }
    };

    BroadcastReceiver mMediaReplyBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent itt = new Intent(Constants.LOGIN_VIDEO_ROOM);
            itt.putExtra(Constants.RoomID, mRoomID);
            sendBroadcast(itt);

        }
    };

    BroadcastReceiver mConnectionResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRoomID = intent.getIntExtra(Constants.RoomID, -1);
        }
    };

    @Override
    protected void initView() {

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
                inviteCancel();
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

       //TODO 发送invite请求
        Intent intent = new Intent();
        intent.setAction(Constants.VIDEO_REQUEST);
        sendBroadcast(intent);
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

       //TODO  取消invite
    }

    public EventListener mEventListener = new EventListener() {
        public void onEvent(Event event, final Object data) {
            log.d(TAG, "onEvent(), envet: " + event);

            switch (event) {
                //1：收到邀请响应,成功跳过，失败提示。
                case MeetingInviteResponse:
                    break;
                //2：收到邀请取消响应
                case MeetingInviteCancelResponse:
                    break;
                //情况3：收到邀请答复，如果为“接受”不处理，如果为“拒绝”则提示后关闭。
                case MeetingReplyRequest: {
                    MeetingReplyRequest req = (MeetingReplyRequest)data;
                    if (req.getAnswer() == 0) {
                        Utils.noUIThreadToast(InviteActivity.this, "对方已拒绝");
                        finish();
                    }
                    break;
                }
                //情况4：进入房间响应
                case EnterRoomResponse: {
                    // 收到进入房间响应后，打开视频会议 页面。
                    Intent intent = new Intent(InviteActivity.this, ActivityMeeting.class);
                    intent.putExtra("EnableSend", true);
                    intent.putExtra("EnableRecv", true);
                    startActivity(intent);
                    finish();
                }
                case CommandTimeout:
                    log.e(TAG, "CommandTimeout, cmdId: " + data);
                    break;
                case CommandNotExecute:
                    log.e(TAG, "CommandNotExecute, cmdId: " + data);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        log.d(TAG, "onDestroy()");
        YYDSDKHelper.getInstance().unRegisterEventListener(mEventListener);
        super.onDestroy();
    }
}
