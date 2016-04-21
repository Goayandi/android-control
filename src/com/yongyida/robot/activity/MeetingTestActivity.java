package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.MeetingInviteRequest;
import com.yongyida.robot.video.sdk.CmdCallBacker;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.User;
import com.yongyida.robot.video.sdk.YYDLogicServer;
import com.yongyida.robot.video.sdk.YYDSDKHelper;
import com.yongyida.robot.video.sdk.YYDVideoServer;
import com.yongyida.robot.widget.InviteReplyDialog;

/**
 * Created by Administrator on 2016/4/13 0013.
 */
public class MeetingTestActivity extends BaseVideoActivity implements OnClickListener{
    public static final String TAG = "MeetingTestActivity";
    private EditText mEditUserId;
    private EditText mEditInviteeUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_test);
        findViewById(R.id.bt).setOnClickListener(this);
        mEditUserId = (EditText) findViewById(R.id.edt_userid);
        mEditUserId.setText("11073");
        mEditInviteeUserId = (EditText) findViewById(R.id.edt_inviteeuserid);
        mEditInviteeUserId.setText("11090");
        YYDSDKHelper.getInstance().registerEventListener(mEventListener);

        /* /media/reply/resopnse */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.Replay_Response}, mConnectionResponseBR);

        /* /media/invite */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.VIDEO_REQUEST_FROM_OTHERS}, mVideoRequestBR);

        /* 登入房间返回 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.LOGIN_VIDEO_ROOM_RESPONSE}, mLoginVideoRoomResponseBR);
    }

    BroadcastReceiver mLoginVideoRoomResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User user = new User("User", 100227);
                    YYDSDKHelper.getInstance().setUser(user);
                    YYDVideoServer.getInstance().getMeetingInfo().setVideoServer(
                            intent.getIntExtra(Constants.RoomID, -1),
                            "120.24.242.163",
                            8003);
                    YYDVideoServer.getInstance().connect("120.24.242.163", 8003);
                    YYDVideoServer.getInstance().enterRoom(new CmdCallBacker() {
                        @Override
                        public void onSuccess(Object o) {
                            Log.i(TAG, "success");
                            Intent i = new Intent(MeetingTestActivity.this, ActivityMeeting.class);
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

    BroadcastReceiver mConnectionResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent itt = new Intent(Constants.LOGIN_VIDEO_ROOM);
            itt.putExtra(Constants.RoomID, intent.getIntExtra(Constants.RoomID, -1));
            sendBroadcast(itt);
        }
    };

    private BroadcastReceiver mVideoRequestBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showInvite(null);
        }
    };

    private BroadcastReceiver mConnectResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
            int replay = intent.getIntExtra(Constants.REPLY, -1);
            switch (replay) {
                case 0:
                    ToastUtil.showtomain(MeetingTestActivity.this, "挂断");
                    break;
                case 1:
                    ToastUtil.showtomain(MeetingTestActivity.this, "接受音&视频");
                    break;
                case 2:
                    ToastUtil.showtomain(MeetingTestActivity.this, "接受音频");
                    break;
                case 3:
                    ToastUtil.showtomain(MeetingTestActivity.this, "接受视频");
                    break;
                default:
                    ToastUtil.showtomain(MeetingTestActivity.this, "连接异常");
                    break;
            }
        }
    };

    @Override
    protected void initView() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt:
                meetingInvite();
                break;
        }

    }

    public void meetingInvite() {
        log.d(TAG, "meetingInvite()");

        String role = YYDSDKHelper.getInstance().getRole();
        int id = Integer.valueOf(mEditInviteeUserId.getText().toString());

        Intent intent = new Intent(MeetingTestActivity.this, InviteActivity.class);
        intent.putExtra("role", role);
        intent.putExtra("id", id);
        intent.putExtra("username", role + id);
        startActivity(intent);
    }

    public EventListener mEventListener = new EventListener() {
        public void onEvent(Event event, final Object data) {
            log.d(TAG, "onEvent(), envet: " + event);

            switch (event) {
                case MeetingInviteResponse:
                    break;
                case MeetingInviteRequest: {
                    // 用户B：收到服务器的视频会议邀请转发后，显示邀请页面（“接受”还是“拒绝”）。
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showInvite((MeetingInviteRequest) data);
                        }
                    });
                    break;
                }
                case MeetingReplyRequest:
                    break;
                case MeetingReplyResponse:
                    break;
                case EnterRoomRequest:
                    break;
                case EnterRoomResponse: {
                    // 收到进入房间响应后，打开视频会议 页面。
                    Intent intent = new Intent(MeetingTestActivity.this, InviteActivity.class);
                    intent.putExtra("EnableSend", true);
                    intent.putExtra("EnableRecv", true);
                    startActivity(intent);
                    break;
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

    private void showInvite(final MeetingInviteRequest req) {
        final InviteReplyDialog.Builder builder = new InviteReplyDialog.Builder(this);
    //    builder.setUserName(req.getUserName());
        builder.setMessage("邀请您视频通话...");
        builder.setPositiveButton(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置接受操作
                inviteReply();
            //    inviteReply(true, req);
                builder.stopCallSound();
            }
        });
        builder.setNegativeButton(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置拒绝操作
             //   inviteReply(false, req);
                builder.stopCallSound();
            }
        });
        builder.create().show();
    }

    private void inviteReply(){
        Intent intent = new Intent();
        intent.setAction(Constants.BR_REPLY);
        sendBroadcast(intent);
    }

    private void inviteReply(boolean reply, final MeetingInviteRequest req) {
        YYDLogicServer.getInstance().meetingReply(reply, // true:接受, false: 拒绝
                req.getRole(), // 发出邀请的用户Id
                req.getId(),
                new CmdCallBacker() {
                    public void onSuccess(Object arg) {
                        log.d(TAG, "meetingAnswer success");
                    }

                    public void onFailed(int error) {
                        log.d(TAG, "meetingAnswer failed, error: " + error);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        log.d(TAG, "onDestroy()");
        YYDSDKHelper.getInstance().unRegisterEventListener(mEventListener);
        YYDVideoServer.getInstance().disConnect();
        super.onDestroy();
    }
}
