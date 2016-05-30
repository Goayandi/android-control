package com.yongyida.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.layout.utils.PercentRelativeLayout;
import com.yongyida.robot.R;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Config;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.HandlerUtil;
import com.yongyida.robot.utils.NetUtil;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.video.av.AVMeeting;
import com.yongyida.robot.video.av.CameraView;
import com.yongyida.robot.video.av.ISwitchView;
import com.yongyida.robot.video.av.TransferDataType;
import com.yongyida.robot.video.av.UserView;
import com.yongyida.robot.video.av.ViewStyle;
import com.yongyida.robot.video.comm.Utils;
import com.yongyida.robot.video.comm.log;
import com.yongyida.robot.video.command.RoomUser;
import com.yongyida.robot.video.sdk.ChannelConfig;

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
    private AudioManager mAudioManager;
    private int mThumbWidth;
    private int mThumbHeight;
    private List<View> mViews;
    private AVMeeting mAVMeeting;
    private ChannelConfig mConfig;
    private int mId;
    private String mRole;
    private int mRoomId;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    try {
                        JSONArray data = new JSONArray(msg.getData().getString("result"));
                        setAdapterData(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private View mRootView;
    private CameraView cv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_meeting);
        Log.e(TAG, "onCreate");
        mId = getIntent().getIntExtra("id", -1);
        mRole = getIntent().getStringExtra("role");
        mRoomId = getIntent().getIntExtra("room_id", -1);

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        initView();
        mAVMeeting = createAVMeeting();
        mThumbWidth = getThumbWidth();
        mThumbHeight = getThumbHeight();

        mViews = new ArrayList<View>();
        addCameraView();
        addUserViews();
        initOthers();
        /* 有人进入房间 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.MEDIA_JOIN_ROOM}, mMediaJoinRoomBR);

        /* 退出房间返回 */
        BroadcastReceiverRegister.reg(this, new String[]{Constants.LOGIN_VIDEO_ROOM_LOGOUT_RESPONSE}, mMediaRoomLogoutBR);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    private void initOthers() {
        if (!isMute()){
            changeButtonState(R.id.bt_mute, false);
        } else {
            changeButtonState(R.id.bt_mute, true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private BroadcastReceiver mMediaRoomLogoutBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            close();
            finish();
        }
    };

    private BroadcastReceiver mMediaJoinRoomBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO 有人加入房间
        }
    };

    private AVMeeting createAVMeeting() {
        log.d(TAG, "createAVMeeting()");

        mConfig = new com.yongyida.robot.video.sdk.ChannelConfig();
        mConfig.transferTyp = Config.getTransferType();
        mConfig.encoderType = Config.getEncoderType();
        int transferDataType = Config.getTransferDataType();
        mConfig.enableAudio = (transferDataType == TransferDataType.AUDIO
                || transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableVideo = (transferDataType == TransferDataType.VIDEO
                || transferDataType == TransferDataType.AUDIOVIDEO);
        mConfig.enableSend = getIntent().getBooleanExtra("EnableSend", true);
        mConfig.enableRecv = getIntent().getBooleanExtra("EnableRecv", true);
        log.d(TAG, mConfig.toString());

        return new AVMeeting(mConfig);
    }

    private void switchView(View smallView, View bigView) {
        log.d(TAG, "switchView(), smallView: " + smallView + ", bigView: " + bigView);

        // 大视图变为小视图
        FrameLayout.LayoutParams sp = (FrameLayout.LayoutParams)smallView.getLayoutParams();
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(smallView.getWidth(), smallView.getHeight());
        bp.setMargins(sp.leftMargin, sp.topMargin, sp.rightMargin, sp.bottomMargin);
        bigView.setLayoutParams(bp);
        if (bigView instanceof ISwitchView) {
            ISwitchView bv = (ISwitchView) bigView;
            bv.setViewStyle(ViewStyle.ThumbView);
        }

        // 小视图变为大视图
        FrameLayout.LayoutParams sp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        smallView.setLayoutParams(sp2);
        if (smallView instanceof ISwitchView) {
            ISwitchView sv = (ISwitchView) smallView;
            sv.setViewStyle(ViewStyle.FullView);
        }

        // 刷新，把小视图（原来的大视图）置前
        refreshView();
    }

    private View getBigView() {
        for (View v : mViews) {
            if (v instanceof ISwitchView
                    && ((ISwitchView) v).getViewStyle() == ViewStyle.FullView)
                return v;
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

    private UserView getUserView(String role, long id) {
        log.d(TAG, "getUserView(), role: " + role + ", id: " + id);

        for (View v : mViews) {
            if (v instanceof UserView) {
                UserView uv = (UserView) v;
                log.d(TAG, "User role: " + uv.getUser().getRole() + ", id: " + uv.getUser().getId());
                if (role.equals(uv.getUser().getRole()) && id == uv.getUser().getId()) {
                    return uv;
                }
            }
        }
        return null;
    }

    private int getScreenHeight() {
        return (Utils.getScreenConfigurationOrientatioin(this) == Configuration.ORIENTATION_LANDSCAPE)
                ? Utils.getScreenHeight(this) : Utils.getScreenWidth(this);
    }

    private int getThumbWidth() {
        return (int) ((((float) mAVMeeting.getMeetingInfo().VideoWidth) / mAVMeeting.getMeetingInfo().VideoHeight) * getThumbHeight());
    }

    private int getThumbHeight() {
        return getScreenHeight() / 4;
    }

    private ViewStyle getInitViewStyle() {
        return (mViews.size() == 1) ? ViewStyle.FullView : ViewStyle.ThumbView;
    }

    private int getViewInitLeft() {
        if (mViews.size() > 1)
            return (mViews.size() - 1) * (mThumbWidth + 5);
        else
            return 0;
    }

    private int getViewInitTop() {
        return 0;
    }

    private int getViewInitWidth() {
        if (mViews.size() == 1)
            return ViewGroup.LayoutParams.MATCH_PARENT;
        else
            return mThumbWidth;
    }

    private int getViewInitHeight() {
        if (mViews.size() == 1)
            return ViewGroup.LayoutParams.MATCH_PARENT;
        else
            return mThumbHeight;
    }

    private void close() {
        for (View v : mViews) {
            if (v instanceof CameraView) {
                ((CameraView) v).close();
            }
            if (v instanceof UserView) {
                ((UserView) v).close();
            }
        }

        if (mAVMeeting != null) {
            mAVMeeting.close();
            mAVMeeting = null;
        }

    }

    /**
     * 加入摄像头视图
     *
     */
    private void addCameraView() {
        log.d(TAG, "addCameraView");

        cv = new CameraView(this, mAVMeeting);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getViewInitWidth(), getViewInitHeight());
        layoutParams.setMargins(getViewInitLeft(), getScreenHeight() - getViewInitHeight(), 0, 0);

        cv.setLayoutParams(layoutParams);
        cv.setViewStyle(getInitViewStyle());
        cv.setOnClickListener(mSwitchClickListener);
        cv.setTitle("Camera");
        mViews.add(cv);
        mFrameLayout.addView(cv);
    }

    private void addUserViews() {
        log.d(TAG, "addUserViews");

        List<RoomUser> users = mAVMeeting.getRoomUsers();
        for (RoomUser user : users) {
            log.d(TAG, "user: " + user.toString());
            addUserView(user);
        }

        // 刷新视图, 把大视图置底，小视图置前。
        refreshView();
    }

    private void addOwnUserView(){

    }

    private void addUserView(RoomUser user) {
        log.d(TAG, "addUserView");

        UserView uv = new UserView(this, user, mAVMeeting);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getViewInitWidth(), getViewInitHeight());
        layoutParams.setMargins(getViewInitLeft(), getViewInitTop(), 0, 0);
        uv.setLayoutParams(layoutParams);
        uv.setViewStyle(getInitViewStyle());
        uv.setOnClickListener(mSwitchClickListener);
        uv.setTitle(user.getRole() + user.getId());
        mViews.add(uv);
        mFrameLayout.addView(uv);
    }

    private void refreshView() {
        log.d(TAG, "refreshView()");

        View bigView = getBigView();
        if (bigView != null) {
            moveToBack(bigView);
        }
        else {
            log.d(TAG, "Not found bigview");
        }
    }

    private void moveToBack(View currentView) {
        log.d(TAG, "moveToBack(), view: " + currentView);

        for (View view : mViews) {
            if (view instanceof ISwitchView
                    && ((ISwitchView) view).getViewStyle() == ViewStyle.ThumbView) {
                mFrameLayout.bringChildToFront(view);
            }
        }
    }

    private View.OnClickListener mSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof ISwitchView
                    && ((ISwitchView)v).getViewStyle() == ViewStyle.ThumbView) {
                View bigView = getBigView();
                if (bigView != null) {
                    switchView(v, bigView);
                }
                else {
                    log.e(TAG, "Not found big view!");
                }
            }
        }
    };

    @Override
    protected void initView() {
        mRootView = findViewById(R.id.root_view);

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
        mCancelButton = (Button) findViewById(R.id.bt_cancel);
        mCancelButton.setOnClickListener(this);
        mInviteConfirmButton = (Button) findViewById(R.id.bt_invite_confirm);
        mInviteConfirmButton.setOnClickListener(this);

        mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 2));

    }

    public boolean isMute() {
        return mAudioManager.isMicrophoneMute();
    }

    public void setMute(boolean mute) {
        mAudioManager.setMicrophoneMute(mute);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_switch_camera:  //切换摄像头
                ToastUtil.showtomain(this,"点");
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
                takeScreenShot();
                break;
            case R.id.bt_hang_up:   //挂断
                hangUp();
                break;
            case R.id.bt_cancel:   //取消
                inviteCancel();
                break;
            case R.id.bt_invite_confirm:  //邀请确认
                inviteConfirm();
                break;
        }
    }

    private void takeScreenShot() {
        String albumPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + "screenshot";
        Long photoName = System.currentTimeMillis();
        com.yongyida.robot.utils.Utils.saveFile(cv.mTextureView.getBitmap(), albumPath + "/" + photoName);
    }

    /**
     * 邀请取消
     */
    private void inviteCancel() {
        mInvitePRL.setVisibility(View.GONE);
        mControlPRL.setVisibility(View.VISIBLE);
    }

    /**
     * 设置按钮的状态
     * @param id
     * @param ifChoosed
     */
    public void changeButtonState(int id, boolean ifChoosed){
        if (ifChoosed) {
            ((Button) findViewById(id)).setTextColor(getResources().getColor(R.color.red));
            findViewById(id).setTag("on");
        } else {
            ((Button) findViewById(id)).setTextColor(getResources().getColor(R.color.black_deep));
            findViewById(id).setTag("off");
        }
    }

    /**
     * 挂断
     */
    private void hangUp() {
        Intent intent = new Intent(Constants.LOGOUT_VIDEO_ROOM);
        intent.putExtra("id", mId + "");
        intent.putExtra("role", mRole);
        intent.putExtra("room_id", mRoomId);
        sendBroadcast(intent);
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
        if (isMute()){
            changeButtonState(R.id.bt_mute, false);
        } else {
            changeButtonState(R.id.bt_mute, true);
        }
        setMute(!isMute());
    }

    /**
     * 邀请
     */
    private void invite() {
        mInvitePRL.setVisibility(View.VISIBLE);
        mControlPRL.setVisibility(View.GONE);
        findRobotFriend(getSharedPreferences("userinfo", MODE_PRIVATE).getInt("id", 0),
                getSharedPreferences("userinfo", MODE_PRIVATE).getString("session", null));
    }

    /**
     * 添加adapter的数据
     * @param data
     */
    private void setAdapterData(JSONArray data){
        if (mAdapter == null) {
            mAdapter = new MyRecycleViewAdapter(data, this);
            mRecycleView.setAdapter(mAdapter);
        } else {
            mAdapter.setAdapterData(data);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 查询好友
     *
     * @param id      手机用户id
     * @param session 登录时获得的session
     */
    public void findRobotFriend(final int id, final String session) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id + "");
                params.put("session", session);
                try {
                    NetUtil.getinstance().http(Constants.FIND_ROBOT_FRIEND, params, new NetUtil.callback() {
                        @Override
                        public void success(JSONObject json) {
                            Log.i(TAG, json.toString());
                            try {
                                int ret = json.getInt("ret");
                                switch (ret) {
                                    case -2:
                                        Log.i(TAG, "session过期");
                                        break;
                                    case -1:
                                        Log.i(TAG, "缺少参数或参数未校验");
                                        break;
                                    case 0:
                                        String dataString = json.getString("Robots");
                                        HandlerUtil.sendmsg(mHandler, dataString, 1);
                                        Log.i(TAG, "成功");
                                        break;
                                    case 1:
                                        Log.i(TAG, "用户信息为空");
                                        break;
                                    case 2:
                                        Log.i(TAG, "手机用户没有朋友");
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
                            Log.i(TAG, errorresult);
                        }
                    }, MeetingActivity.this);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.MyViewHolder>{
        private JSONArray data;
        private Context mContext;
        private List<Boolean> checkList;  //checkBox的状态
        public MyRecycleViewAdapter(JSONArray data, Context context){
            checkList = new ArrayList<Boolean>();
            mContext = context;
            this.data = data;
        }

        public void setAdapterData(JSONArray data){
            this.data = data;
        }

        @Override
        public MyRecycleViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rv_friend, viewGroup , false));
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final MyRecycleViewAdapter.MyViewHolder viewHolder, final int i) {
            try {
                viewHolder.tv.setText(data.getJSONObject(i).getString("rname"));
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

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        try {
            if (mMediaJoinRoomBR != null) {
                unregisterReceiver(mMediaJoinRoomBR);
            }
            if (mMediaRoomLogoutBR != null) {
                unregisterReceiver(mMediaRoomLogoutBR);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (mViews != null) {
            close();
        }
        super.onDestroy();
    }
}
