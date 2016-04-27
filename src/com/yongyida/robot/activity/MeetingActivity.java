package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.layout.utils.PercentRelativeLayout;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.video.av.AVMeeting;
import com.yongyida.robot.video.av.CameraView;
import com.yongyida.robot.video.av.TransferDataType;
import com.yongyida.robot.video.av.UserView;
import com.yongyida.robot.video.av.VideoParam;
import com.yongyida.robot.video.av.VideoSizeType;
import com.yongyida.robot.video.comm.Size;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.ExitRoomRequest;
import com.yongyida.robot.video.command.JoinRoomRequest;
import com.yongyida.robot.video.sdk.ChannelConfig;
import com.yongyida.robot.video.sdk.Event;
import com.yongyida.robot.video.sdk.EventListener;
import com.yongyida.robot.video.sdk.User;
import com.yongyida.robot.video.sdk.YYDSDKHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/30 0030.
 */
public class MeetingActivity extends BaseVideoActivity implements View.OnClickListener{
    public static final String TAG = "MeetingActivity";

    private FrameLayout mFrameLayout;
    private Button mSwitchCameraButton;
    private Button mMuteButton;
    private Button mSwitchVoiceButton;
    private PercentRelativeLayout mControlPRL;
    private Button mInviteButton;
    private Button mTrancribeButton;
    private Button mScreenshotButton;
    private Button mHangUpButton;
    private PercentRelativeLayout mInvitePRL;
    private RecyclerView mRecycleView;
    private Button mCancelButton;
    private Button mInviteConfirmButton;
    private MyRecycleViewAdapter mAdapter;
    private AudioManager audioManager;
    private List<View> mViews;
    private AVMeeting mAVMeeting;
    private ChannelConfig mConfig;
    private Size mVideoSize;
    private int mThumbWidth;
    private int mThumbHeight;
    private Long mInviteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        initView();
        initDevice();
        initBroadcastReceiver();
    }

    private void initBroadcastReceiver() {
        /* 网络断开 */
        BroadcastReceiverRegister.reg(this,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION},
                mNeterrorBR);

        /* 视频请求的回复 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.CONNECTION_REQUEST}, mConnectRequestBR);

        /* 视频应答,接通或挂断视频请求 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.CONNECTION_RESPONSE}, mConnectResponseBR);

        /* 接到视频请求 */
        BroadcastReceiverRegister.reg(this,
                new String[]{Constants.VIDEO_REQUEST_FROM_OTHERS}, mVideoRequestBR);
        YYDSDKHelper.getInstance().registerEventListener(entListener);
    }

    private void initVideoConnection() {
        Intent intent = new Intent();
        intent.setAction(Constants.VIDEO_REQUEST);
        sendBroadcast(intent);
    }

    private BroadcastReceiver mVideoRequestBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MeetingActivity", "mVideoRequestBR");
            mInviteId = intent.getLongExtra(Constants.ID, -1);
        }
    };

    private BroadcastReceiver mConnectResponseBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
            int replay = intent.getIntExtra(Constants.REPLY, -1);
            switch (replay) {
                case 0:
                    ToastUtil.showtomain(MeetingActivity.this, "挂断");
                    break;
                case 1:
                    ToastUtil.showtomain(MeetingActivity.this, "接受音&视频");
                    break;
                case 2:
                    ToastUtil.showtomain(MeetingActivity.this, "接受音频");
                    break;
                case 3:
                    ToastUtil.showtomain(MeetingActivity.this, "接受视频");
                    break;
                default:
                    ToastUtil.showtomain(MeetingActivity.this, "连接异常");
                    break;
            }
        }
    };

    private BroadcastReceiver mConnectRequestBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra(Constants.RET, -1);
            switch (ret) {
                case 0:
                    videoPlay();
                    ToastUtil.showtomain(MeetingActivity.this, "成功，等待应答");
                    break;
                case 1:
                    ToastUtil.showtomain(MeetingActivity.this, "对方不在线");
                    break;
                case 2:
                    ToastUtil.showtomain(MeetingActivity.this, "房间不存在");
                    break;
                case 3:
                    ToastUtil.showtomain(MeetingActivity.this, "有未处理的视频请求");
                    break;
                default:
                    ToastUtil.showtomain(MeetingActivity.this, "连接异常");
                    break;
            }
        }
    };

    private BroadcastReceiver mNeterrorBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
        }
    };
    private void initDevice() {
        audioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.view_container);

        mSwitchCameraButton = (Button) findViewById(R.id.bt_switch_camera);
        mSwitchCameraButton.setOnClickListener(this);
        mMuteButton = (Button) findViewById(R.id.bt_mute);
        mMuteButton.setOnClickListener(this);
        mSwitchVoiceButton = (Button) findViewById(R.id.bt_switch_voice);
        mSwitchVoiceButton.setOnClickListener(this);

        mControlPRL = (PercentRelativeLayout) findViewById(R.id.rl_control);
        mInviteButton = (Button) findViewById(R.id.bt_invite);
        mInviteButton.setOnClickListener(this);
        mTrancribeButton = (Button) findViewById(R.id.bt_transcribe);
        mTrancribeButton.setOnClickListener(this);
        mScreenshotButton = (Button) findViewById(R.id.bt_screenshot);
        mScreenshotButton.setOnClickListener(this);
        mHangUpButton = (Button) findViewById(R.id.bt_hang_up);
        mHangUpButton.setOnClickListener(this);

        mInvitePRL = (PercentRelativeLayout) findViewById(R.id.rl_invite);
        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mCancelButton = (Button) findViewById(R.id.bt_cancel);
        mCancelButton.setOnClickListener(this);
        mInviteConfirmButton = (Button) findViewById(R.id.bt_invite_confirm);
        mInviteConfirmButton.setOnClickListener(this);

        mAdapter = new MyRecycleViewAdapter();
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 2));

    }

    private void videoPlay() {
        mAVMeeting = createAVMeeting();
        mThumbWidth = getThumbWidth();
        mThumbHeight = getThumbHeight();
        mViews = new ArrayList<View>();
        addCameraView();
        addUserViews();
    }


    private AVMeeting createAVMeeting() {

        mConfig = new ChannelConfig();
        mConfig.transferTyp = Config.getTransferType();
        mConfig.encoderType = Config.getEncoderType();
        mConfig.transferDataType = Config.getTransferDataType();
        mConfig.enableAudio = (mConfig.transferDataType == TransferDataType.AUDIO
                || mConfig.transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableVideo = (mConfig.transferDataType == TransferDataType.VIDEO
                || mConfig.transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableSend = getIntent().getBooleanExtra("EnableSend", true);
        mConfig.enableRecv = getIntent().getBooleanExtra("EnableRecv", true);
        mVideoSize = VideoSizeType.getVideoSize(Config.getVideoSizeType());
        VideoParam vp = VideoParam.getVideoParam(mVideoSize.width, mVideoSize.height, Config.getFrameRateType(),
                Config.getBitRateType());
        mVideoSize.width = vp.VideoWidth;
        mVideoSize.height = vp.VideoHeight;
        mConfig.videoWidth = mVideoSize.width;
        mConfig.videoHeight = mVideoSize.height;
        mConfig.frameRate = vp.FrameRate;
        mConfig.bitRate = vp.Bitrate;

        return new AVMeeting(mConfig);
    }

    /**
     * 加入摄像头视图
     *
     */
    private void addCameraView() {

        CameraView cv = new CameraView(this, mAVMeeting);
        cv.setLeft(getViewInitLeft());
        cv.setTop(getViewInitTop());
        cv.setLayoutParams(new LayoutParams(getViewInitWidth(), getViewInitHeight()));
        cv.setOnClickListener(mThumbClickListener);
        cv.setBorderVisible(getInitBorderVisible());
        cv.setTitleVisible(getInitTitleVisible());
        mViews.add(cv);
        mFrameLayout.addView(cv);
    }

    private void addUserViews() {
        List<User> users = mAVMeeting.getUsers();
        for (User user : users) {
            addUserView(user);
        }
    }

    private void addUserView(User user) {

        UserView uv = new UserView(this, user, mAVMeeting);
        uv.setLeft(getViewInitLeft());
        uv.setTop(getViewInitTop());
        uv.setLayoutParams(new LayoutParams(getViewInitWidth(), getViewInitHeight()));
        uv.setOnClickListener(mThumbClickListener);
        uv.setBorderVisible(getInitBorderVisible());
        uv.setTitleVisible(getInitTitleVisible());
        mViews.add(uv);
        mFrameLayout.addView(uv);
        refreshView();
    }

    private void refreshView() {
        int count = mFrameLayout.getChildCount();
        int thumbHeight = getThumbHeight();
        for (int i = 0; i < count; ++i) {
            View view = mFrameLayout.getChildAt(i);
            if (view.getHeight() <= thumbHeight) {
                mFrameLayout.bringChildToFront(view);
            }
        }
    }

    private int getScreenHeight() {
        return (Utils.getScreenConfigurationOrientatioin(this) == Configuration.ORIENTATION_LANDSCAPE)
                ? Utils.getScreenHeight(this) : Utils.getScreenWidth(this);
    }

    private int getThumbWidth() {
        return (int)((((float)mVideoSize.width) / mVideoSize.height) * getThumbHeight());
    }

    private int getThumbHeight() {
        return getScreenHeight() / 4;
    }

    private int getViewInitLeft() {
        if (mViews.size() > 1)
            return (mViews.size() - 1) * mThumbWidth;
        else
            return 0;
    }

    private int getViewInitTop() {
        return 0;
    }

    private int getViewInitWidth() {
        if (mViews.size() == 1)
            return LayoutParams.MATCH_PARENT;
        else
            return mThumbWidth;
    }

    private int getViewInitHeight() {
        if (mViews.size() == 1)
            return LayoutParams.MATCH_PARENT;
        else
            return mThumbHeight;
    }

    private boolean getInitBorderVisible() {
        return (mViews.size() != 1);
    }

    private boolean getInitTitleVisible() {
        return (mViews.size() != 1);
    }

    private void switchView(View smallView, View bigView) {

        // 大视图变为小视图
        FrameLayout.LayoutParams svp = new FrameLayout.LayoutParams(
                smallView.getWidth(),
                smallView.getHeight());
        bigView.setLayoutParams(svp);
      /*  if (bigView instanceof IThumbView) {
            IThumbView thumb = (IThumbView)bigView;
            thumb.setBorderVisible(true);
            thumb.setTitleVisible(true);
        }

        // 小视图变为大视图
        FrameLayout.LayoutParams bvp = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        smallView.setLayoutParams(bvp);
        if (smallView instanceof IThumbView) {
            IThumbView thumb = (IThumbView)smallView;
            thumb.setBorderVisible(false);
            thumb.setTitleVisible(false);
        }*/

        // 把小视图（原来的大视图）置前
        mFrameLayout.bringChildToFront(bigView);
    }

    private OnClickListener mThumbClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getHeight() > getThumbHeight()) {
                return;
            }

            View bigView = getBigView();
            if (bigView != null) {
                switchView(v, bigView);
            }
            else {
            }
        }
    };

    private View getBigView() {
        for (View v : mViews) {
            if (v.getWidth() > mThumbWidth)
                return v;
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_switch_camera:  //切换摄像头
                initVideoConnection();
                break;
            case R.id.bt_mute:    //静音
                muteControl();
                break;
            case R.id.bt_switch_voice:  //切换语音
                break;
            case R.id.bt_invite:  //邀请
                invite();
                break;
            case  R.id.bt_transcribe:   //录制
                break;
            case R.id.bt_screenshot:  //截图
                break;
            case R.id.bt_hang_up:   //挂断
                hangUp();
                break;
            case R.id.bt_cancel:   //取消
                break;
            case R.id.bt_invite_confirm:  //邀请确认
                inviteConfirm();
                break;
        }
    }

    /**
     * 挂断
     */
    private void hangUp() {

    }

    /**
     * 邀请确认
     */
    private void inviteConfirm() {
        //发出会议邀请
    }

    /**
     * 静音控制
     */
    private void muteControl() {
        if (audioManager.isMicrophoneMute()) {
            audioManager.setMicrophoneMute(false);
            mMuteButton.setTextColor(getResources().getColor(R.color.black_deep));
        } else {
            audioManager.setMicrophoneMute(true);
            mMuteButton.setTextColor(getResources().getColor(R.color.red_pure));
        }
    }

    /**
     * 邀请
     */
    private void invite() {
        mControlPRL.setVisibility(View.GONE);
        mInvitePRL.setVisibility(View.VISIBLE);
        //发出网络请求获取列表
        int id = getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0);
        String session = getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null);
        findRobotFriend(id, session);
    }

    /**
     * 添加adapter的数据
     * @param data
     */
    private void setAdapterData(JSONArray data){
        mAdapter.setAdapterData(data);
        mAdapter.notifyDataSetChanged();
    }


    /**
     * 查询好友
     * @param id 手机用户id
     * @param session 登录时获得的session
     */
    public void findRobotFriend(int id, String session){
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", id + "");
        params.put("session", session);
        try {
            NetUtil.getinstance().http(Constants.FIND_ROBOT_FRIEND, params, new NetUtil.callback() {
                @Override
                public void success(JSONObject json) {
                    try {
                        int ret = json.getInt("ret");
                        switch (ret) {
                            case -1:
                                Log.i("AddFriendsActivity", "缺少参数或参数未校验");
                                break;
                            case 0:
                                JSONArray data = json.getJSONArray("Robots");
                                setAdapterData(data);
                                Log.i("AddFriendsActivity", "成功");
                                break;
                            case 1:
                                Log.i("AddFriendsActivity", "用户信息为空");
                                break;
                            case 2:
                                Log.i("AddFriendsActivity", "手机用户没有朋友");
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
            }, MeetingActivity.this);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
    }


    class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder>{
        private JSONArray data;
        private List<Boolean> checkList;  //checkBox的状态
        public MyRecycleViewAdapter(){
            checkList = new ArrayList<Boolean>();
        }

        public void setAdapterData(JSONArray data){
            this.data = data;
        }

        @Override
        public MyRecycleViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(MeetingActivity.this).inflate(R.layout.item_rv_video, viewGroup , false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final MyRecycleViewAdapter.MyViewHolder viewHolder, final int i) {
            try {
                viewHolder.tv.setText(data.getJSONObject(i).getString("rname"));
                viewHolder.cb.setChecked(checkList.get(i));
                viewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        checkList.set(i, isChecked);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tv;
            ImageView iv;
            LinearLayout ll;
            CheckBox cb;
            public MyViewHolder(View itemView) {
                super(itemView);
                tv = ((TextView) itemView.findViewById(R.id.tv));
                iv = ((ImageView) itemView.findViewById(R.id.iv));
                ll = ((LinearLayout) itemView.findViewById(R.id.ll));
                cb = ((CheckBox) itemView.findViewById(R.id.cb));
            }
        }
    }

    private EventListener entListener = new EventListener(){

        @Override
        public void onEvent(Event event, Object data) {
            switch (event) {
                case MeetingInviteResponse:
                    Log.i("MeetingActivity", "MeetingInviteResponse");
                    break;
                case MeetingInviteRequest: {
                    // 用户B：收到服务器的视频会议邀请转发后，显示邀请页面（“接受”还是“拒绝”）。
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("MeetingActivity", "MeetingInviteRequest");
                        }
                    });
                    //发送invite
                    Intent intent = new Intent(Constants.BR_REPLY);
                    intent.putExtra(Constants.INVITE_ID, mInviteId);
                    sendBroadcast(intent);
                    break;
                }
                case MeetingReplyRequest:
                    Log.i("MeetingActivity", "MeetingReplyRequest");
                    break;
                case MeetingReplyResponse:
                    Log.i("MeetingActivity", "MeetingReplyResponse");
                    break;
                case EnterRoomRequest:
                    Log.i("MeetingActivity", "EnterRoomRequest");
                    break;
                case EnterRoomResponse:
                    // 收到进入房间响应后，打开视频会议 页面。
                    Log.i("MeetingActivity", "EnterRoomResponse");
                    break;
                case CommandTimeout:
                    Log.i("MeetingActivity", "CommandTimeout");
                    break;
                case CommandNotExecute:
                    Log.i("MeetingActivity", "CommandNotExecute");
                    break;
                case JoinRoomRequest: {
                    //收到用户进入房间
                    log.d(TAG, "Received a JoinRoomRequest.");
                    JoinRoomRequest req = (JoinRoomRequest) data;
                    if (req.getRoomUser() != null) {
                        final User user = mAVMeeting.getUser(req.getRoomUser().Role, req.getRoomUser().Id);
                        if (user != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addUserView(user);
                                }
                            });
                        }
                        else {
                            log.e(TAG, "Not found user, role: " + req.getRoomUser().Role + ", id: " + req.getRoomUser().Id);
                        }
                    }
                    else {
                        log.e(TAG, "JoinRoomRequest error, RoomUser null!");
                    }
                    break;
                }
                case ExitRoomRequest: {
                    //收到用户退出房间
                    log.d(TAG, "Received a ExitRoomRequest.");
                    final ExitRoomRequest req = (ExitRoomRequest) data;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //移除用户视图
                            removeUserView(req.getRole(), req.getId());
                            if (mAVMeeting.getUsers().size() == 0) {
                                log.d(TAG, "All user exit, will exit.");
                                hangUp();
                            }
                        }
                    });
                    break;
                }
                case ForwardRequest: {
                    //数据转发
                    log.d(TAG, "Received a ForwardRequest.");
                    break;
                }
                default:
                    Log.i("MeetingActivity", "default");
                    break;
            }
        }
    };

    private UserView getUserView(String role, long id) {
        log.d(TAG, "getUserView(), role: " + role + ", id: " + id);

        for (View v : mViews) {
            if (v instanceof UserView) {
                UserView uv = (UserView)v;
                log.d(TAG, "User role: " + uv.getUser().getRole() + ", id: " + uv.getUser().getId());
                if (role.equals(uv.getUser().getRole())
                        && id ==  uv.getUser().getId()) {
                    return uv;
                }
            }
        }
        return null;
    }

    private void removeUserView(String role, long id) {
        log.d(TAG, "removeUserView(), role: " + role + ", id: " + id);

        UserView uv = getUserView(role, id);
        if (uv != null) {
            uv.close();
            mViews.remove(uv);
            mFrameLayout.removeView(uv);
        }
        else {
            log.e(TAG, "Not found user view, role: " + role + ", id: " + id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        YYDSDKHelper.getInstance().unRegisterEventListener(entListener);
    }
}
