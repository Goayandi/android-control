package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.yongyida.robot.R;
import com.yongyida.robot.bean.MeetingMemberObj;
import com.yongyida.robot.ronglianyun.SDKCoreHelper;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;
import com.yongyida.robot.widget.MeetingUserLayout;
import com.yongyida.robot.widget.ScreenshotDialog;
import com.yuntongxun.ecsdk.CameraCapability;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECMeetingManager.ECCreateMeetingParams.ToneMode;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;
import com.yuntongxun.ecsdk.meeting.ECVideoMeetingMember;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingDeleteMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingExitMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingJoinMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRejectMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingRemoveMemberMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingSwitchMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingVideoFrameActionMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/6/2 0002.
 */
public class TestMeetingActivity extends Activity {

    private static final String TAG = "TestMeetingActivity";
    private static final int QUERY_MEMBER_COUNT = 1;
    private static final int CREATE_FAIL = 4;
    private static final int CANCEL_VIDEO_SUCCESS = 5;
    private static final int PUBLISH_SUCCESS = 6;
    private static final int CANCEL_VIDEO_FAIL = 7;
    private static final int PUBLISH_FAIL = 8;
    private static final int REQUEST_VIDEO_SUCCESS = 9;
    private static final int REQUEST_VIDEO_FAIL = 10;
    private static final int INVITE_SUCCESS = 11;
    private static final int INVITE_FAIL = 12;
    private static final int RELEASE_FAIL = 13;
    private static final int INVITE_REFUSE_MESSAGE = 14;
    private static final int RELEASE_SUCCESS = 15;
    private static final int CUT = 16;
    private static final int MEETING_DELETE = 17;
    private static final int INVITE_ACCEPT_MESSAGE = 18;
    private static final int MEETING_NOT_EXIST = 19;
    private static final int LOCAL_LINE_OCCUPY = 170486;
    private static final int REQUEST_SERVER_FAIL = 171139;
    private static final int MEETING_NOT_EXIST_CODE = 175707;
    public static final int RELEASE_MEETING_FAIL_CODE = 1;
    public static final int REQUEST_SERVER_FAIL_CODE = 2;
    private List<String> mUserAccounts;
    private String mVideoConferenceId;
    private ECCaptureView mECCaptureView;
   // private SurfaceView mSurfaceView;
    private MeetingUserLayout mMeetingUserLayout; //全屏视图里的布局
    private FrameLayout mFrameLayout;
    private LinearLayout mSurfaceContainer;
    private ECMeetingManager meetingManager;
    private RelativeLayout mFriendsRl;
    private LinearLayout mFunctionRightLL;
    private LinearLayout mFunctionTopLL;
    private EditText mInviteEditText;
    private Button mInviteBtn;
    private Button mMuteBtn;
    private String mAccount;
    private byte[] mLock = new byte[0];
    private List<MeetingMemberObj> mMeetingMemberObjList;
    private boolean host; //是否是房主
    private boolean isVideo = true; //是否发布视频
    private OnMeetingListener mOnMeetingListener;
    private OnChatReceiveListener mOnChatReceiveListener;
    private ECVoIPCallManager.OnVoIPListener mOnVoIPListener;
    private PopupWindow mPopupWindow;
    private String mPopupWindowTag;
    private List<String> mInviteList; //邀请人名单
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QUERY_MEMBER_COUNT:
                    if (msg.arg1 <= 1) {
                        mMeetingUserLayout = null;
                        mFrameLayout.removeAllViews();
                    }
                    break;
                case CREATE_FAIL:
                    if (msg.arg1 == REQUEST_SERVER_FAIL) {
                        setResult(REQUEST_SERVER_FAIL_CODE);
                    }
                    ToastUtil.showtomain(TestMeetingActivity.this,"创建视频房间失败，请稍后重试" + msg.arg1);
                    TestMeetingActivity.this.finish();
                    break;
                case CANCEL_VIDEO_SUCCESS:
                    ToastUtil.showtomain(TestMeetingActivity.this, "取消视频发布");
                    mSwitchPublishStateBtn.setText("切换视频");
                    mSwitchPublishStateBtn.setEnabled(true);
                    break;
                case CANCEL_VIDEO_FAIL:
                    ToastUtil.showtomain(TestMeetingActivity.this, "取消视频发布失败");
                    mSwitchPublishStateBtn.setEnabled(true);
                    break;
                case PUBLISH_SUCCESS:
                    ToastUtil.showtomain(TestMeetingActivity.this, "发布视频成功");
                    mSwitchPublishStateBtn.setText("切换语音");
                    mSwitchPublishStateBtn.setEnabled(true);
                    break;
                case PUBLISH_FAIL:
                    ToastUtil.showtomain(TestMeetingActivity.this, "发布视频失败");
                    mSwitchPublishStateBtn.setEnabled(true);
                    break;
                case REQUEST_VIDEO_SUCCESS:
                    ToastUtil.showtomain(TestMeetingActivity.this, "请求视频图像");
                    break;
                case REQUEST_VIDEO_FAIL:
                    ToastUtil.showtomain(TestMeetingActivity.this, "请求视频图像失败,请刷新");
                    break;
                case INVITE_SUCCESS:
                    ToastUtil.showtomain(TestMeetingActivity.this, "邀请发送成功");
                    break;
                case INVITE_FAIL:
                    ToastUtil.showtomain(TestMeetingActivity.this, "邀请发送失败");
                    break;
                case RELEASE_FAIL:
                    ToastUtil.showtomain(TestMeetingActivity.this, "解散房间失败");
                    finish();
                    break;
                case RELEASE_SUCCESS:
                    ToastUtil.showtomain(TestMeetingActivity.this, "解散房间成功");
                    finish();
                    break;
                case INVITE_REFUSE_MESSAGE:
                    ToastUtil.showtomain(TestMeetingActivity.this, msg.obj + "拒绝邀请");
                    break;
                case CUT:
                    ToastUtil.showtomain(TestMeetingActivity.this, "视频会议中断");
                    break;
                case MEETING_DELETE:
                    ToastUtil.showtomain(TestMeetingActivity.this, "视频会议结束");
                    break;
                case INVITE_ACCEPT_MESSAGE:
                    ToastUtil.showtomain(TestMeetingActivity.this, msg.obj + "接受邀请");
                    break;
                case MEETING_NOT_EXIST:
                    ToastUtil.showtomain(TestMeetingActivity.this, "会议不存在");
                    break;
                default:
                    break;
            }
        }
    };
    private Button mSwitchPublishStateBtn;
 //   private String mCallId;
    private String mCallName;
    private String mUnreleaseMeetingNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_meeting);
        host = getIntent().getBooleanExtra(Constants.HOST, true);
        init();
        openCamera();
        SDKCoreHelper.getInstance().setmBusyFlag(true);
        if (host) {
            mUnreleaseMeetingNo = getIntent().getStringExtra(Constants.TO_MEETING_NO);
            Log.e(TAG, "mUnreleaseMeetingNo:" + mUnreleaseMeetingNo);
            createMeetingRoom();
        } else {
            mFunctionRightLL.setVisibility(View.GONE);
            mVideoConferenceId = getIntent().getStringExtra(Constants.CONFERENCE_ID);
        //    mCallId = getIntent().getStringExtra(Constants.CALL_ID);
            mCallName = getIntent().getStringExtra(Constants.CALL_NAME);
            joinMeeting();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mECCaptureView.onResume();
    }

    private void publishVideo() {
        meetingManager.publishSelfVideoFrameInVideoMeeting(mVideoConferenceId,
                new ECMeetingManager.OnSelfVideoFrameChangedListener() {
                    @Override
                    public void onSelfVideoFrameChanged(boolean isPublish, ECError error) {
                        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            // 发布视频会议图像成功
                            // isPublish 表示当前是否是发布视频会议请求
                            // isPublish = true表示请求发布视频会议图像接口回调
                            isVideo = isPublish;
                            mHandler.sendEmptyMessage(PUBLISH_SUCCESS);
                            return;
                        }
                        mHandler.sendEmptyMessage(PUBLISH_FAIL);
                    }
                });
    }

    private void joinMeeting() {
        meetingManager.joinMeetingByType(mVideoConferenceId, "",
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnCreateOrJoinMeetingListener() {
                    @Override
                    public void onCreateOrJoinMeeting(ECError reason, String meetingNo) {
                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            // 加入会议成功
                            openCamera();
                            publishVideo();
                            query();
                        } else if (reason.errorCode == MEETING_NOT_EXIST_CODE) {
                            mHandler.sendEmptyMessage(MEETING_NOT_EXIST);
                            finish();
                        }

                    }
                });

    }

    private void init() {
        initlayout();
        initMeetingInfo();

    }

    private void createMeetingRoom() {
        // 初始化创建会议所需要的参数
        ECMeetingManager.ECCreateMeetingParams.Builder builder
                = new ECMeetingManager.ECCreateMeetingParams.Builder();
        // 设置语音会议房间名称
        builder.setMeetingName("meetingName")
                // 设置视频会议创建者退出是否自动解散会议
                .setIsAutoClose(false)
                        // 设置视频会议创建成功是否自动加入
                .setIsAutoJoin(true)
                        // 设置视频会议背景音模式
                .setVoiceMod(ToneMode.ALL)
                        // 设置视频会议所有成员退出后是否自动删除会议
                .setIsAutoDelete(true);
        ECMeetingManager.ECCreateMeetingParams params = builder.create();
        // 获取一个会议管理接口对象
        meetingManager.createMultiMeetingByType(params,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnCreateOrJoinMeetingListener() {
                    @Override
                    public void onCreateOrJoinMeeting(ECError reason, String meetingNo) {
                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            mVideoConferenceId = meetingNo;
                            Log.d(TAG, "mVideoConferenceId:" + mVideoConferenceId);
                            return;
                        } else {
                            if (reason.errorCode == LOCAL_LINE_OCCUPY) {
                                if (!TextUtils.isEmpty(mUnreleaseMeetingNo)){
                                    releaseMeeting(mUnreleaseMeetingNo);
                                    return;
                                }
                            }
                            Message message = mHandler.obtainMessage(CREATE_FAIL);
                            message.arg1 = reason.errorCode;
                            mHandler.sendMessage(message);
                            Log.d(TAG, reason.errorMsg + ":" + reason.errorCode);
                        }
                    }
                });

    }

    public void initlayout() {
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mSurfaceContainer = (LinearLayout) findViewById(R.id.ll_container);
        mECCaptureView = (ECCaptureView) findViewById(R.id.ec_capture_view);
        mECCaptureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mECCaptureView.onResume();
                query();
            }
        });
        mFriendsRl = (RelativeLayout) findViewById(R.id.rl_invite);
        mFunctionRightLL = (LinearLayout) findViewById(R.id.ll_right_function);
        mFunctionTopLL = (LinearLayout) findViewById(R.id.ll_top_function);
        mInviteBtn = (Button) findViewById(R.id.bt_invite);
        mMuteBtn = (Button) findViewById(R.id.bt_mute);
        mSwitchPublishStateBtn = (Button) findViewById(R.id.bt_switch_voice);
        if (ECDevice.getECVoIPSetupManager() == null) {
            mHandler.sendEmptyMessage(CREATE_FAIL);
            return;
        }
        mMuteBtn.setTextColor(ECDevice.getECVoIPSetupManager().getMuteStatus() ? getResources().getColor(R.color.red) : getResources().getColor(R.color.black_deep));
        mInviteEditText = (EditText) findViewById(R.id.et_invite);

    }


    /**
     * 获取小的surfaceView的布局
     * @param account
     * @return
     */
    private MeetingUserLayout getSmallSurfaceViewLayout(String account) {
        MeetingUserLayout meetingUserLayout = new MeetingUserLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getScreenWidth() / 4, LinearLayout.LayoutParams.MATCH_PARENT);
        params.rightMargin = 5;
        params.leftMargin = 5;
        meetingUserLayout.setLayoutParams(params);
        mSurfaceContainer.addView(meetingUserLayout);
        meetingUserLayout.setTag(account);
        meetingUserLayout.setSurfaceViewClickListener(new SurfaceviewClickListener());
        meetingUserLayout.setTextViewText("加入中");
        return meetingUserLayout;
    }


    /**
     * 移除小的surfaceView的布局
     * @param meetingUserLayout
     * @param exit
     */
    private void removeSurfaceViewLayout(MeetingUserLayout meetingUserLayout, boolean exit){
        mSurfaceContainer.removeView(meetingUserLayout);
        if (exit) {
            if (meetingUserLayout != null) {
                removeVideoUIMemberCache((String) meetingUserLayout.getTag());
            }
        }
    }

    private void initMeetingInfo() {
        mUserAccounts = new ArrayList<String>();
        mMeetingMemberObjList = new ArrayList<MeetingMemberObj>();
        mInviteList = new ArrayList<String>();
        if (TextUtils.isEmpty(Utils.getAccount(this))) {
            ToastUtil.showtomain(this, "无法获取账户信息");
            finish();
            return;
        }
        mAccount = Utils.getAccount(this);
        putVideoUIMemberCache(mAccount);
        meetingManager = ECDevice.getECMeetingManager();
        mOnMeetingListener = new MyOnMeetingListener();
        mOnChatReceiveListener = new MyOnChatReceiveListener();
        mOnVoIPListener = new MyOnVoIPListener();
        SDKCoreHelper.getInstance().registOnChatReceiveListener(mOnChatReceiveListener);
        SDKCoreHelper.getInstance().registOnMeetingListener(mOnMeetingListener);
        SDKCoreHelper.getInstance().registOnVoIPCallListener(mOnVoIPListener);

        SDKCoreHelper.getVoIPCallManager().setOnVoIPCallListener(new MyOnVoIPListener());

    }

    private class MyOnVoIPListener implements  ECVoIPCallManager.OnVoIPListener {

        @Override
        public void onDtmfReceived(String s, char c) {

        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voIPCall) {
            // 接收VoIP呼叫事件回调
            if(voIPCall == null) {
                Log.d(TAG, "handle call event error , voipCall null");
                return ;
            }
            switch (voIPCall.callState) {
                case ECCALL_PROCEEDING:
                    break;
                case ECCALL_ALERTING:
                    break;
                case ECCALL_ANSWERED:
                    break;
                case ECCALL_FAILED:
                    break;
                case ECCALL_RELEASED:
                    Log.d(TAG, "ECCALL_RELEASED");
                //    finish();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {

        }
    }

    /**
     * 拒绝邀请
     *
     * @param to
     */
    private void sendRejectMessage(String to) {
        try {
            String msgType = Constants.MEETING_INVITE_REPLY;
            String body = Constants.INVITE_REJECT;

            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setFrom(mAccount);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(to);
            msg.setSessionId(to);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData(Constants.MESSAGE_PREFIX + msgType);

            ECTextMessageBody msgBody = new ECTextMessageBody(body);
            msg.setBody(msgBody);
            ECChatManager manager = ECDevice.getECChatManager();
            manager.sendMessage(msg, new ECChatManager.OnSendMessageListener() {
                @Override
                public void onSendMessageComplete(ECError error, ECMessage message) {

                    // 处理消息发送结果
                    if (message == null) {
                        return;
                    }
                    //                Message msg = mHandler.obtainMessage(ERROR_CODE);
                    //                msg.arg1 = error.errorCode;
                    //                mHandler.sendMessage(msg);
                    // 将发送的消息更新到本地数据库并刷新UI
                }

                @Override
                public void onProgress(String msgId, int totalByte, int progressByte) {
                }
            });
        } catch (Exception e) {
            // 处理发送异常
            Log.e(TAG, "send message fail , e=" + e.getMessage());
        }
    }

    private class MyOnChatReceiveListener implements OnChatReceiveListener {
        @Override
        public void OnReceivedMessage(ECMessage msg) {
            if(msg.getType() == ECMessage.Type.TXT) {
                // 在这里处理文本消息
                ECTextMessageBody textMessageBody = (ECTextMessageBody) msg.getBody();
                if (textMessageBody != null) {

                    if (msg.getUserData().equals(Constants.MESSAGE_PREFIX + Constants.MEETING_INVITE_REPLY)) {
                        if (mInviteList != null && mInviteList.contains(msg.getForm())) {
                            mInviteList.remove(msg.getForm());
                        }
                        if (Constants.INVITE_REJECT.equals(textMessageBody.getMessage())) {
                            Message message = mHandler.obtainMessage(INVITE_REFUSE_MESSAGE);
                            message.obj = msg.getForm();
                            mHandler.sendMessage(message);
                        } else if (Constants.INVITE_ACCEPT.equals(textMessageBody.getMessage())) {
                            Message message = mHandler.obtainMessage(INVITE_ACCEPT_MESSAGE);
                            message.obj = msg.getForm();
                            mHandler.sendMessage(message);
                        }
                    } else if (msg.getUserData().equals(Constants.MESSAGE_PREFIX + Constants.INVITE_MESSAGE)) {
                        sendRejectMessage(msg.getForm());
                    }

                }
            }

        }

        @Override
        public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

        }

        @Override
        public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
            // 收到群组通知消息（有人加入、退出...）
            // 可以根据ECGroupNoticeMessage.ECGroupMessageType类型区分不同消息类型
        }

        @Override
        public void onOfflineMessageCount(int count) {
            // 登陆成功之后SDK回调该接口通知账号离线消息数
        }

        @Override
        public int onGetOfflineMessage() {
            return 0;
        }

        @Override
        public void onReceiveOfflineMessage(List msgs) {
            // SDK根据应用设置的离线消息拉去规则通知应用离线消息
        }

        @Override
        public void onReceiveOfflineMessageCompletion() {
            // SDK通知应用离线消息拉取完成
        }

        @Override
        public void onServicePersonVersion(int version) {
            // SDK通知应用当前账号的个人信息版本号
        }

        @Override
        public void onReceiveDeskMessage(ECMessage ecMessage) {

        }

        @Override
        public void onSoftVersion(String s, int i) {

        }
    }

    /**
     * 添加已进入房间的MeetingMemberObj对象
     * @param obj
     */
    private void addMeetingMemberObj(MeetingMemberObj obj) {
        if (mMeetingMemberObjList != null) {
            if (!mMeetingMemberObjList.contains(obj)) {
                mMeetingMemberObjList.add(obj);
            }
        }
    }

    /**
     * 根据account获取MeetingMemberObj对象
     * @param account
     * @return
     */
    private MeetingMemberObj getMeetingMemberObjByAccount(String account) {
        if (mMeetingMemberObjList != null && mMeetingMemberObjList.size() != 0) {
            for (MeetingMemberObj obj : mMeetingMemberObjList) {
                if (obj.getAccount().equals(account)) {
                    return obj;
                }
            }
        }
        return null;
    }

    private void deleteMeetingMemberObjByAccount(String account) {
        if (mMeetingMemberObjList != null && mMeetingMemberObjList.size() != 0) {
            for (MeetingMemberObj obj : mMeetingMemberObjList) {
                if (obj.getAccount().equals(account)) {
                    mMeetingMemberObjList.remove(obj);
                }
            }
        }
    }

    /**
     * 获取屏幕宽度
     * @return
     */
    private int getScreenWidth(){
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度
     * @return
     */
    private int getScreenHeight(){
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 打开相机 并选择合适的传输分辨率
     */
    private void openCamera(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        int cameraIndex = 0;
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraIndex = camIdx;
            }
        }


        CameraInfo[] cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();
        int mCameraCapbilityIndex = 0;
        if (cameraInfos != null) {
            for (int i = 0; i < cameraInfos.length; i++) {
                if (cameraInfos[i].index == cameraIndex) {
                    mCameraCapbilityIndex = comportCapabilityIndex(cameraInfos[cameraInfos[i].index].caps, 352 * 288);
                }
            }
        }
        ECDevice.getECVoIPSetupManager().setVideoView(mECCaptureView);
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
        mECCaptureView.setZOrderOnTop(true);
        ECDevice.getECVoIPSetupManager().selectCamera(cameraIndex, mCameraCapbilityIndex, 15, ECVoIPSetupManager.Rotate.ROTATE_0, true);
    }

    /**
     * 寻找合适的清晰度
     * @param caps
     * @param compliant
     * @return
     */
    public static int comportCapabilityIndex(CameraCapability[] caps , int compliant) {
        if(caps == null ) {
            return 0;
        }
        int pixel[] = new int[caps.length];
        int _pixel[] = new int[caps.length];
        for(CameraCapability cap : caps) {
            if(cap.index >= pixel.length) {
                continue;
            }
            pixel[cap.index] = cap.width * cap.height;
        }

        System.arraycopy(pixel, 0, _pixel, 0, caps.length);

        Arrays.sort(_pixel);
        for(int i = 0 ; i < caps.length ; i++) {
            if(pixel[i] >= compliant) {
                return i;
            }
        }
        return 0;
    }


    private class MyOnMeetingListener implements OnMeetingListener {

        @Override
        public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg ecInterPhoneMeetingMsg) {

        }

        @Override
        public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg ecVoiceMeetingMsg) {

        }

        @Override
        public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {
            switch (msg.getMsgType()) {
                case JOIN:
                    // 视频会议消息类型-有人加入
                    ECVideoMeetingJoinMsg joinMsg = (ECVideoMeetingJoinMsg) msg;
                    String string = "";
                    for (String s : joinMsg.getWhos()) {
                        string = string + s + ",";
                    }
                    Log.d(TAG, "join:" + string);
                    query();
                    break;
                case EXIT:
                    // 视频会议消息类型-有人退出
                    final ECVideoMeetingExitMsg exitMsg = (ECVideoMeetingExitMsg) msg;
                    for (int i = 0 ; i < exitMsg.getWhos().length; i ++) {
                        Log.d(TAG, "exit: " + exitMsg.getWhos()[i]);
                        if (host) {
                            if (exitMsg.getWhos()[i].equals(mAccount)) {
                                exitRoom();
                                return;
                            }
                        } else {
                            //房主或是自己离开房间
                            if (exitMsg.getWhos()[i].equals(mCallName) || exitMsg.getWhos()[i].equals(mAccount)) {
                                exitRoom();
                                return;
                            }
                        }
                        if (mMeetingUserLayout != null && exitMsg.getWhos()[i].equals(mMeetingUserLayout.getTag().toString())) {
                            mMeetingUserLayout = null;
                            mFrameLayout.removeAllViews();
                            removeVideoUIMemberCache(exitMsg.getWhos()[i]);
                        } else {
                            removeSurfaceViewLayout((MeetingUserLayout) mSurfaceContainer.findViewWithTag(exitMsg.getWhos()[i]), true);
                        }
                        if (mPopupWindow != null && exitMsg.getWhos()[i].equals(mPopupWindowTag)) {
                            mPopupWindow.dismiss();
                        }
                    }
                    queryCount();
                    break;
                case DELETE:
                    // 视频会议消息类型-会议结束
                    ECVideoMeetingDeleteMsg delMsg = (ECVideoMeetingDeleteMsg) msg;
                    Log.d(TAG, "DELETE");
            //        mHandler.sendEmptyMessage(MEETING_DELETE);
            //        exitRoom();
                    finish();
                    break;
                case REMOVE_MEMBER:
                    // 视频会议消息类型-成员被移除
                    ECVideoMeetingRemoveMemberMsg rMsg =
                            (ECVideoMeetingRemoveMemberMsg) msg;
                    Log.d(TAG, "remove: " + rMsg.getWho());
                    if (mMeetingUserLayout != null && rMsg.getWho().equals(mMeetingUserLayout.getTag().toString())) {
                        mMeetingUserLayout = null;
                        mFrameLayout.removeAllViews();
                        removeVideoUIMemberCache(rMsg.getWho());
                    } else {
                        removeSurfaceViewLayout((MeetingUserLayout) mSurfaceContainer.findViewWithTag(rMsg.getWho()), true);
                    }
                    if (mPopupWindow != null && rMsg.getWho().equals(mPopupWindowTag)) {
                        mPopupWindow.dismiss();
                    }
                    queryCount();
                    break;
                case SWITCH:
                    // 视频会议消息类型-主屏切换
                    ECVideoMeetingSwitchMsg sMsg = (ECVideoMeetingSwitchMsg) msg;
                    Log.d(TAG, "SWITCH");
                    break;
                case VIDEO_FRAME_ACTION:
                    // 视频会议消息类型-成员图象发布或者取消发布
                    ECVideoMeetingVideoFrameActionMsg actionMsg =
                            (ECVideoMeetingVideoFrameActionMsg) msg;
                    View view = mSurfaceContainer.findViewWithTag(actionMsg.getWho());
                    if (view == null) {
                        if (mMeetingUserLayout != null && actionMsg.getWho().equals(mMeetingUserLayout.getTag())) {
                            view = mMeetingUserLayout;
                        } else {
                            view = null;
                        }
                    }
                    if (view == null) {
                        return;
                    }
                    MeetingUserLayout meetingUserLayout = null;
                    if (view instanceof  MeetingUserLayout) {
                        meetingUserLayout = (MeetingUserLayout)view;
                    }
                    if (meetingUserLayout != null) {
                        if (actionMsg.isPublish()) {  // 发布
                            Log.d(TAG, actionMsg.getWho() + "发布");
                            if (meetingUserLayout.getTextViewText().equals(getString(R.string.cancel_publish))) {
                                meetingUserLayout.invisiableTextView();
                            }
                        } else {  //取消发布
                            meetingUserLayout.setTextViewText(getString(R.string.cancel_publish));
                            Log.d(TAG, actionMsg.getWho() + "取消发布");
                        }
                    }
                    break;
                case REJECT:
                    // 视频会议消息类型-成员拒绝邀请加入会议请求
                    ECVideoMeetingRejectMsg rejectMsg =
                            (ECVideoMeetingRejectMsg) msg;
                    Message message = mHandler.obtainMessage(INVITE_REFUSE_MESSAGE);
                    message.obj = rejectMsg.getWho();
                    mHandler.sendMessage(message);
                    Log.d(TAG, "reject:" + rejectMsg.describeContents());
                    break;
                case CUT:
                //    mHandler.sendEmptyMessage(CUT);
                    break;
                default:
                    Log.e(TAG, "can't handle notice msg "
                            + msg.getMsgType());
                    break;
            }
        }

        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {
            int height = videoRatio.getHeight();
            int width = videoRatio.getWidth();
            Log.d(TAG, "height:" + height);
            Log.d(TAG, "width:" + width);
            View view = mSurfaceContainer.findViewWithTag(videoRatio.getAccount());
            if (view instanceof  MeetingUserLayout) {
                ((MeetingUserLayout)view).invisiableTextView();
            }
        }
    }



    /**
     * 查询视频会议中的成员 添加没有加进来的
     */
    public void query(){
        // 发起查询视频会议成员请求
        meetingManager.queryMeetingMembersByType(mVideoConferenceId,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnQueryMeetingMembersListener() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason, List members) {
                        if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                            // 查询视频会议成员成功
                            for (ECVideoMeetingMember m : (List<ECVideoMeetingMember>) members) {
                                if (!isVideoUIMemberExist(m.getNumber()) && !m.getNumber().equals(mAccount)) {
                                    addJoinedUsers(m);
                                }
                            }

                        } else {
                            Log.d(TAG, "query fail:" + reason.errorCode);
                        }
                    }
                });
    }
    private void queryCount() {
        // 发起查询视频会议成员请求
        meetingManager.queryMeetingMembersByType(mVideoConferenceId,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnQueryMeetingMembersListener() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason, List members) {
                        if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                            // 查询视频会议成员成功
                            Message message = mHandler.obtainMessage();
                            message.what = QUERY_MEMBER_COUNT;
                            message.arg1 = members.size();
                            mHandler.sendMessage(message);
                        }
                    }
                });
    }

    /**
     * 静音按钮
     * @param v
     */
    public void mute(View v) {
        ECDevice.getECVoIPSetupManager().setMute(!ECDevice.getECVoIPSetupManager().getMuteStatus());
        mMuteBtn.setTextColor(ECDevice.getECVoIPSetupManager().getMuteStatus() ? getResources().getColor(R.color.red) : getResources().getColor(R.color.black_deep));
    }

    /**
     * 切换摄像头按钮
     * @param v
     */
    public void switchCamera(View v) {
        mECCaptureView.switchCamera();
    }

    /**
     * 切换语音按钮
     * @param v
     */
    public void switchVoice(View v) {
        mSwitchPublishStateBtn.setEnabled(true);
        if (isVideo) {
            cancelVideo();
        } else {
            publishVideo();
        }
    }

    /**
     *  取消视频发布
     */
    public void cancelVideo() {
        meetingManager.cancelPublishSelfVideoFrameInVideoMeeting(mVideoConferenceId,
                new ECMeetingManager.OnSelfVideoFrameChangedListener() {
                    @Override
                    public void onSelfVideoFrameChanged(boolean isPublish, ECError error) {
                        if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            // 取消视频会议图像成功
                            // isPublish 表示当前是否是发布视频会议请求
                            // isPublish = false表示取消发布视频会议图像接口回调
                            isVideo = isPublish;
                            mHandler.sendEmptyMessage(CANCEL_VIDEO_SUCCESS);
                            return;
                        }
                        mHandler.sendEmptyMessage(CANCEL_VIDEO_FAIL);
                        Log.e(TAG, "cancel publish sel video error[" + error.errorCode
                                + " ]");
                    }
                });
    }

    /**
     * 是否开启麦克风
     * @param open
     */
    public void microphone(boolean open) {
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(open);
    }

    /**
     * 添加成员
     * @param member
     */
    private void addJoinedUsers(ECVideoMeetingMember member) {
        // 调用请求视频会议成员图像接口
        // meetingNo :所在的会议号
        // meetingPwd :所在的会议密码
        // account :需要请求视频的成员账号，比如需要请求John的视频图像则 account 为John账号
        // displayView :视频图像显示View
        // ip和port :成员视频图像所在的IP地址和端口可以参考ECVideoMeetingMember.java和ECVideoMeetingJoinMsg.java 参数
        Log.d(TAG, "account:" + member.getNumber());
        MeetingUserLayout meetingUserLayout = getSmallSurfaceViewLayout(member.getNumber());
        putVideoUIMemberCache(member.getNumber());
        MeetingMemberObj obj = new MeetingMemberObj(member.getNumber(), member.getIp(), member.getPort());
        addMeetingMemberObj(obj);
        requestVideo(obj, meetingUserLayout);
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
    }

    /**
     * 请求视频图像
     * @param obj
     * @param meetingUserLayout
     */
    private void requestVideo(MeetingMemberObj obj, MeetingUserLayout meetingUserLayout) {
        if (obj != null) {
            meetingManager.requestMemberVideoInVideoMeeting(mVideoConferenceId, "",
                    obj.getAccount(), meetingUserLayout.getSurfaceView(), obj.getIp(), obj.getPort(),
                    new ECMeetingManager.OnMemberVideoFrameChangedListener() {
                        @Override
                        public void onMemberVideoFrameChanged(boolean isRequest, ECError reason, String meetingNo, String account) {
                            if(reason.errorCode == SdkErrorCode.REQUEST_SUCCESS || reason.errorCode == 0) {
                                mHandler.sendEmptyMessage(REQUEST_VIDEO_SUCCESS);
                            } else {
                                mHandler.sendEmptyMessage(REQUEST_VIDEO_FAIL);
                            }
                        }
                    });
        }
    }

    public void putVideoUIMemberCache(String who) {
        synchronized (mLock) {
            if (mUserAccounts != null) {
                mUserAccounts.add(who);
            }
        }
    }

    public void removeVideoUIMemberCache(String who) {
        synchronized (mLock) {
            if (mUserAccounts != null) {
                mUserAccounts.remove(who);
            }
        }
    }

    private boolean isVideoUIMemberExist(String who) {
        synchronized (mLock) {
            if (TextUtils.isEmpty(who)) {
                return false;
            }

            if (mUserAccounts != null) {
                if (mUserAccounts.contains(who)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 录制
     * @param v
     */
    public void transcribe(View v) {
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(!ECDevice.getECVoIPSetupManager().getLoudSpeakerStatus());
    }

    /**
     * 邀请
     * @param v
     */
    public void invite(View v) {
        if (!host) {
            ToastUtil.showtomain(this, "只有房主能邀请");
        } else {
            mFriendsRl.setVisibility(View.VISIBLE);
            mFunctionRightLL.setVisibility(View.GONE);
        }
    }


    /**
     * 退出会议
     */
    private void exitMeeting() {
        meetingManager.exitMeeting(ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO);
    }

    /**
     * 邀请用户
     * @param user
     */
    private void inviteMember(String user) {
        String members[] = {user};
        // isLandingCall:表示是否以落地电话形式或者VoIP来电方式
        // 获取一个会议管理接口对象
        // 发起邀请加入会议请求
        meetingManager.inviteMembersJoinToMeeting(mVideoConferenceId, members, false,
                new ECMeetingManager.OnInviteMembersJoinToMeetingListener() {
                    @Override
                    public void onInviteMembersJoinToMeeting(ECError reason, String meetingNo) {
//                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
//                            mHandler.sendEmptyMessage(INVITE_SUCCESS);
//                            return;
//                        }
//                        mHandler.sendEmptyMessage(INVITE_FAIL);
                    }
                });
    }

    /**
     *
     * @param user
     */
    private void inviteMemberByMessage(String user){
        try {
            String msgType = Constants.INVITE_MESSAGE;
            String body = mVideoConferenceId;    // 会议号

            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setForm(mAccount);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(user);
            msg.setSessionId(user);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData(Constants.MESSAGE_PREFIX + msgType);

            ECTextMessageBody msgBody = new ECTextMessageBody(body);
            msg.setBody(msgBody);
            ECChatManager manager = ECDevice.getECChatManager();
            manager.sendMessage(msg, new ECChatManager.OnSendMessageListener() {
                @Override
                public void onSendMessageComplete(ECError error, ECMessage message) {

                    // 处理消息发送结果
                    if (message == null) {
                        return;
                    }
                    //                Message msg = mHandler.obtainMessage(ERROR_CODE);
                    //                msg.arg1 = error.errorCode;
                    //                mHandler.sendMessage(msg);
                    // 将发送的消息更新到本地数据库并刷新UI
                }

                @Override
                public void onProgress(String msgId, int totalByte, int progressByte) {
                }
            });
        } catch (Exception e) {
            // 处理发送异常
            Log.e(TAG, "send message fail , e=" + e.getMessage());
        }
    }

    /**
     * 截屏
     * @param v
     */
    public void screenshot(View v) {
    //    showScreenshotDialog(getScreenshotBitmap());
    }

    private Bitmap getScreenshotBitmap(){
        return null;
    }

    private void showScreenshotDialog(final Bitmap bitmap){
        ScreenshotDialog dialog = new ScreenshotDialog(this);
        dialog.setImage(bitmap);
        dialog.showDialog(getScreenWidth() / 6 * 5, getScreenHeight() / 6 * 5, new ScreenshotDialog.OnSavingListener() {
            @Override
            public void save() {
                String albumPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + "screenshot";
                Long photoName = System.currentTimeMillis();
                com.yongyida.robot.utils.Utils.saveFile(bitmap, albumPath + "/" + photoName);
            }
        });
    }

    /**
     * 挂断
     * @param v
     */
    public void hangup(View v) {
        exitRoom();
    }

    /**
     * 退出房间
     */
    public void exitRoom() {
        if (host) {
            exitMeeting();
            releaseMeeting(mVideoConferenceId);
        } else {
        //    releaseCall(mCallId);
            exitMeeting();
            finish();
        }
    }

    /**
     * 接听方挂断
     * @param callId
     */
    public void releaseCall(String callId){
        SDKCoreHelper.getVoIPCallManager().releaseCall(callId);
    }

    /**
     * 取消邀请
     * @param v
     */
    public void inviteCancel(View v) {
        mFriendsRl.setVisibility(View.GONE);
        mFunctionRightLL.setVisibility(View.VISIBLE);
    }

    /**
     * 确认邀请
     * @param v
     */
    public void inviteConfirm(View v) {
        mFriendsRl.setVisibility(View.GONE);
        mFunctionRightLL.setVisibility(View.VISIBLE);
        String account = mInviteEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(account)) {
        //    inviteMember(account);
            inviteMemberByMessage(account);
            if (mInviteList != null && !mInviteList.contains(account)) {
                mInviteList.add(account);
            }
        }
    }

    @Override
    public void onBackPressed() {
        exitRoom();
    }


    /**
     * 解散会议
     * @param meetingNo
     */
    private void releaseMeeting(String meetingNo){
        // 发起解散会议请求
        meetingManager.deleteMultiMeetingByType(ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                meetingNo, new ECMeetingManager.OnDeleteMeetingListener() {
                    @Override
                    public void onMeetingDismiss(ECError reason, String meetingNo) {
                        if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            if (!TextUtils.isEmpty(mUnreleaseMeetingNo)) {
                                mUnreleaseMeetingNo = "";
                                createMeetingRoom();
                                return;
                            }
                            // 解散会议成功
                            mHandler.sendEmptyMessage(RELEASE_SUCCESS);
                        } else {
                            Intent intent = new Intent();
                            if (TextUtils.isEmpty(mVideoConferenceId)) {
                                intent.putExtra(Constants.UNRELEASE_MEETING_NO, mUnreleaseMeetingNo);
                            } else {
                                intent.putExtra(Constants.UNRELEASE_MEETING_NO, mVideoConferenceId);
                            }
                            setResult(RELEASE_MEETING_FAIL_CODE, intent);
                            mHandler.sendEmptyMessage(RELEASE_FAIL);
                        }
                    }
                });
    }

    /**
     * 切换到全屏显示
     * @param meetingUserLayout
     */
    private void switchToBigScreen(MeetingUserLayout meetingUserLayout) {
        // 获取一个会议管理接口对象
        String viewTag = (String) meetingUserLayout.getTag();
        int ret;
        if (mMeetingUserLayout != null && mMeetingUserLayout.getTag() != null) {
            String bigSurfaceTag = (String) mMeetingUserLayout.getTag();
            String bigSurfaceText = mMeetingUserLayout.getTextViewText();
            ret = meetingManager.resetVideoMeetingWindow(viewTag, getBigSurfaceView(viewTag));
            if (meetingUserLayout.getTextViewText().equals(getString(R.string.cancel_publish))) {
                mMeetingUserLayout.setTextViewText(getString(R.string.cancel_publish));
            } else {
                mMeetingUserLayout.invisiableTextView();
            }
            ret = ret + meetingManager.resetVideoMeetingWindow(bigSurfaceTag, meetingUserLayout.getSurfaceView());
            if (getString(R.string.cancel_publish).equals(bigSurfaceText)) {
                meetingUserLayout.setTextViewText(getString(R.string.cancel_publish));
            } else {
                meetingUserLayout.invisiableTextView();
            }
            meetingUserLayout.setTag(bigSurfaceTag);
        } else {
            ret = meetingManager.resetVideoMeetingWindow(viewTag, getBigSurfaceView(viewTag));
            if (meetingUserLayout.getTextViewText().equals(getString(R.string.cancel_publish))) {
                mMeetingUserLayout.setTextViewText(getString(R.string.cancel_publish));
            }
            removeSurfaceViewLayout(meetingUserLayout, false);
        }
        // 调用切换/重置当前视频成员图像显示窗口接口
        // account :John的账号
        // displayView :新的视频图像显示窗口
        if(ret == 0) {
            // 切换请求执行成功
            return ;
        }
        // 失败[ -1(不支持视频) 170012(账号号为NULL) -3(displayView为NULL) -4(找不到该账号相关的资料)]
        Log.e(TAG, "reset member video window error[" + ret + " ]");
    }

    private class SurfaceviewClickListener implements MeetingUserLayout.OnSurfaceViewClickListener {

        @Override
        public void onSurfaceViewClick(View v) {
            showPopupWindow(v);
        }
    }

    /**
     * 显示popupWindow
     * @param surfaceView
     */
    private void showPopupWindow(View surfaceView){
        final MeetingUserLayout meetingUserLayout = (MeetingUserLayout) surfaceView.getParent();
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(R.layout.layout_popupwindow, null);
        mPopupWindow = new PopupWindow(contentView, mMuteBtn.getWidth(), mMuteBtn.getHeight() * 2 + 10);
        mPopupWindowTag = (String) meetingUserLayout.getTag();
        contentView.findViewById(R.id.bt_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = (String)meetingUserLayout.getTag();
                MeetingMemberObj obj = getMeetingMemberObjByAccount(account);
                requestVideo(obj, meetingUserLayout);
                mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.bt_magnify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToBigScreen(meetingUserLayout);
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        int[] location = new int[2];
        surfaceView.getLocationOnScreen(location);

        mPopupWindow.showAtLocation(surfaceView, Gravity.NO_GRAVITY, location[0], location[1]);

    }

    /**
     *
     * @param surfaceView
     */
    private void showFullScreenViewPopupWindow(View surfaceView) {
        final MeetingUserLayout meetingUserLayout = (MeetingUserLayout) surfaceView.getParent();
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = layoutInflater.inflate(R.layout.layout_popupwindow, null);
        mPopupWindow = new PopupWindow(contentView, mMuteBtn.getWidth(), mMuteBtn.getHeight() + 5);
        mPopupWindowTag = (String) meetingUserLayout.getTag();
        contentView.findViewById(R.id.bt_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = (String)meetingUserLayout.getTag();
                MeetingMemberObj obj = getMeetingMemberObjByAccount(account);
                requestVideo(obj, meetingUserLayout);
                mPopupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.bt_magnify).setVisibility(View.GONE);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());


        mPopupWindow.showAtLocation(surfaceView, Gravity.NO_GRAVITY, surfaceView.getWidth() / 2 - mMuteBtn.getWidth() / 2, surfaceView.getHeight() / 2);
    }

    /**
     * 获取全屏的surfaceView
     * @param tag  用户id
     * @return
     */
    private View getBigSurfaceView(String tag) {
        int childCount = mFrameLayout.getChildCount();
        if (childCount != 0) {
            mFrameLayout.removeAllViews();
        }
        mMeetingUserLayout = new MeetingUserLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMeetingUserLayout.setLayoutParams(params);
        mMeetingUserLayout.setTag(tag);
        mMeetingUserLayout.setSurfaceViewClickListener(new MeetingUserLayout.OnSurfaceViewClickListener() {
            @Override
            public void onSurfaceViewClick(View v) {
                showFullScreenViewPopupWindow(v);
            }
        });
        mFrameLayout.addView(mMeetingUserLayout);
        return mMeetingUserLayout.getSurfaceView();
    }


    /**
     * 取消邀请
     * @param to
     */
    private void sendInviteCancelMessage(String to){
        try {
            //取消邀请
            String msgType = Constants.MEETING_CANCEL_INVITE;
            String body = mVideoConferenceId;    // 会议号

            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
            msg.setForm(mAccount);
            msg.setMsgTime(System.currentTimeMillis());
            msg.setTo(to);
            msg.setSessionId(to);
            msg.setDirection(ECMessage.Direction.SEND);
            msg.setUserData(Constants.MESSAGE_PREFIX + msgType);

            ECTextMessageBody msgBody = new ECTextMessageBody(body);
            msg.setBody(msgBody);
            ECChatManager manager = ECDevice.getECChatManager();
            manager.sendMessage(msg, new ECChatManager.OnSendMessageListener() {
                @Override
                public void onSendMessageComplete(ECError error, ECMessage message) {

                    // 处理消息发送结果
                    if (message == null) {
                        return;
                    }
                    //                Message msg = mHandler.obtainMessage(ERROR_CODE);
                    //                msg.arg1 = error.errorCode;
                    //                mHandler.sendMessage(msg);
                    // 将发送的消息更新到本地数据库并刷新UI
                }

                @Override
                public void onProgress(String msgId, int totalByte, int progressByte) {
                }
            });
        } catch (Exception e) {
            // 处理发送异常
            Log.e(TAG, "send message fail , e=" + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (meetingManager != null) {
            exitMeeting();
        }
        if (mInviteList != null && mInviteList.size() != 0) {
            for (String account : mInviteList) {
                sendInviteCancelMessage(account);
                Log.d(TAG, "account:" + account);
            }
        }
        if (mUserAccounts != null) {
            mUserAccounts.clear();
            mUserAccounts = null;
        }
        if (mInviteList != null) {
            mInviteList.clear();
            mInviteList = null;
        }
        mLock = null;
        if (ECDevice.getECVoIPSetupManager() != null) {
            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(false);
        }
        SDKCoreHelper.getInstance().setmBusyFlag(false);
        SDKCoreHelper.getInstance().unRegistOnChatReceiveListener(mOnChatReceiveListener);
        SDKCoreHelper.getInstance().unRegistOnMeetingListener(mOnMeetingListener);
        SDKCoreHelper.getInstance().unRegistOnVoIPCallListener(mOnVoIPListener);
    }

}
