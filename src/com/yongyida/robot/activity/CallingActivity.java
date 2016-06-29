package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.yongyida.robot.R;
import com.yongyida.robot.ronglianyun.SDKCoreHelper;
import com.yongyida.robot.utils.Constants;
import com.yuntongxun.ecsdk.CameraCapability;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/6/6 0006.
 */
public class CallingActivity extends Activity implements OnMeetingListener {
    private static final String TAG = "CallingActivity";
    /**呼叫唯一标识号*/
    protected String mCallId;
    /**通话号码*/
    protected String mCallNumber;

    /**会议id**/
    protected String mConferenceId;

    private HashMap<String, Integer> mUserAccounts;
    private ECCaptureView mCaptureView;
    private SurfaceView mSurfaceView;

    private void publishVideo(){
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        meetingManager.publishSelfVideoFrameInVideoMeeting(mConferenceId,
                new ECMeetingManager.OnSelfVideoFrameChangedListener() {
                    @Override
                    public void onSelfVideoFrameChanged(boolean isPublish, ECError error) {
                        if(error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
                            // 发布视频会议图像成功
                            // isPublish 表示当前是否是发布视频会议请求
                            // isPublish = true表示请求发布视频会议图像接口回调
                            Log.e(TAG, "onSelfVideoFrameChanged");
                            return ;
                        }
                    }
                });
    }

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        mCaptureView = (ECCaptureView)findViewById(R.id.eccapture_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.sur);
        mCaptureView.setZOrderMediaOverlay(true);
        mSurfaceView.setZOrderOnTop(true);
        mCallId = getIntent().getStringExtra(ECDevice.CALLID);
        mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
        String[] infos = getIntent().getExtras().getStringArray(ECDevice.REMOTE);
        mConferenceId = "";
        for (String s: infos) {
            if (s.startsWith("confid=")) {
                mConferenceId = s.substring(7);
            }
            Log.e(TAG, s);
        }
        Log.e(TAG, mConferenceId);

        SDKCoreHelper.getVoIPCallManager().rejectCall(mCallId, SdkErrorCode.REMOTE_CALL_BUSY);
        ECDevice.getECMeetingManager().setOnMeetingListener(this);

        mUserAccounts = new HashMap<String, Integer>();
        mUserAccounts.put("18664920330", 1);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        openCamera();
    }

    public void accept(View v) {
//        if (!"".equals(mConferenceId)) {
//            ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
//            // 发起加入会议请求[如果会议需要验证密码则需要输入会议密码]
//            meetingManager.joinMeetingByType(mConferenceId, "",
//                    ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
//                    new ECMeetingManager.OnCreateOrJoinMeetingListener() {
//                        @Override
//                        public void onCreateOrJoinMeeting(ECError reason, String meetingNo) {
//                            if (reason.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
//                                // 加入会议成功
//                                Log.e(TAG, "onCreateOrJoinMeeting");
//                                openCamera();
//                                publishVideo();
//                                query();
//                            }
//
//                        }
//                    });
//
//        }
        Intent intent = new Intent(this, TestMeetingActivity.class);
        intent.putExtra(Constants.HOST, false);
        intent.putExtra(Constants.CONFERENCE_ID, mConferenceId);
        startActivity(intent);
    }


    public void openCamera(){
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
        ECDevice.getECVoIPSetupManager().setVideoView(mCaptureView);
        ECDevice.getECVoIPSetupManager().selectCamera(cameraIndex, mCameraCapbilityIndex, 15, ECVoIPSetupManager.Rotate.ROTATE_0, true);
    }

    public void request(View v) {
        publishVideo();
    }

    public void refuse(View v) {
        // 获取一个会议管理接口对象
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        // 发起退出视频会议请求
        meetingManager.exitMeeting(ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO);
        finish();
    }

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

                Log.e(TAG, "join:" + string);
           //     query(joinMsg);
                break;
            case EXIT:
                // 视频会议消息类型-有人退出
                ECVideoMeetingExitMsg exitMsg = (ECVideoMeetingExitMsg) msg;
                break;
            case DELETE:
                // 视频会议消息类型-会议结束
                ECVideoMeetingDeleteMsg delMsg = (ECVideoMeetingDeleteMsg) msg;
                break;
            case REMOVE_MEMBER:
                // 视频会议消息类型-成员被移除
                ECVideoMeetingRemoveMemberMsg rMsg =
                        (ECVideoMeetingRemoveMemberMsg) msg;
                break;
            case SWITCH:
                // 视频会议消息类型-主屏切换
                ECVideoMeetingSwitchMsg sMsg = (ECVideoMeetingSwitchMsg) msg;
                break;
            case VIDEO_FRAME_ACTION:
                // 视频会议消息类型-成员图象发布或者取消发布
                ECVideoMeetingVideoFrameActionMsg actionMsg =
                        (ECVideoMeetingVideoFrameActionMsg) msg;
                break;
            case REJECT:
                // 视频会议消息类型-成员拒绝邀请加入会议请求
                ECVideoMeetingRejectMsg rejectMsg =
                        (ECVideoMeetingRejectMsg) msg;
                break;
            default:
                Log.e("ECSDK_Demo", "can't handle notice msg "
                        + msg.getMsgType());
                break;
        }
    }

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {

    }

    public void query(){
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        // 发起查询视频会议成员请求
        meetingManager.queryMeetingMembersByType(mConferenceId,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnQueryMeetingMembersListener() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason, List members) {
                        if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                            // 查询视频会议成员成功
                            for (ECVideoMeetingMember m: (List<ECVideoMeetingMember>)members) {
                                Log.e(TAG,"number:" + m.getNumber());
                                Log.e(TAG, "MeetingNo:" + m.getMeetingNo());
                                if(!isVideoUIMemberExist(m.getNumber())){
                                    addJoinedUsers(m);
                                }
                            }
                        }
                    }
                });
    }

    private void addJoinedUsers(ECVideoMeetingMember member) {
        Log.e(TAG, "mVideoConferenceId:" + mConferenceId);
        Log.e(TAG, "account:" + member.getNumber());
        Log.e(TAG, "ip:" + member.getIp());
        Log.e(TAG, "port:" + member.getPort());
        Log.e(TAG, "surface:" + mSurfaceView);
        int i = ECDevice.getECMeetingManager().requestMemberVideoInVideoMeeting(mConferenceId, "",
                member.getNumber(), mSurfaceView, member.getIp(), member.getPort(),
                new ECMeetingManager.OnMemberVideoFrameChangedListener() {
                    @Override
                    public void onMemberVideoFrameChanged(boolean isRequest, ECError reason, String meetingNo, String account) {
                        Log.e(TAG, "onMemberVideoFrameChanged:" + account);
                        ECDevice.getECMeetingManager().resetVideoMeetingWindow(account, mSurfaceView);
                    }
                });
        Log.e(TAG, "i:" + i);
        ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
    }

    public void query(final ECVideoMeetingJoinMsg joinMsg){
        // 获取一个会议管理接口对象
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        // 发起查询视频会议成员请求
        meetingManager.queryMeetingMembersByType(mConferenceId,
                ECMeetingManager.ECMeetingType.MEETING_MULTI_VIDEO,
                new ECMeetingManager.OnQueryMeetingMembersListener() {
                    @Override
                    public void onQueryMeetingMembers(ECError reason, List members) {
                        if (SdkErrorCode.REQUEST_SUCCESS == reason.errorCode) {
                            // 查询视频会议成员成功
                            for (ECVideoMeetingMember m: (List<ECVideoMeetingMember>)members) {
                                Log.e(TAG,"number:" + m.getNumber());
                                Log.e(TAG, "MeetingNo:" + m.getMeetingNo());
                            }
                            addJoinedUsers(joinMsg ,members);
                            return;
                        }
                    }
                });
    }

    private void addJoinedUsers(ECVideoMeetingJoinMsg joinMsg, List<ECVideoMeetingMember> members) {
        // 获取一个会议管理接口对象
        ECMeetingManager meetingManager = ECDevice.getECMeetingManager();
        // 调用请求视频会议成员图像接口
        // meetingNo :所在的会议号
        // meetingPwd :所在的会议密码
        // account :需要请求视频的成员账号，比如需要请求John的视频图像则 account 为John账号
        // displayView :视频图像显示View
        // ip和port :成员视频图像所在的IP地址和端口可以参考ECVideoMeetingMember.java和ECVideoMeetingJoinMsg.java 参数
        for (final ECVideoMeetingMember ecVideoMeetingMember: members) {
            if (!isVideoUIMemberExist(ecVideoMeetingMember.getNumber())){
                Log.e(TAG, "mVideoConferenceId:" + mConferenceId);
                Log.e(TAG, "account:" + ecVideoMeetingMember.getNumber());
                Log.e(TAG, "ip:" + joinMsg.getIp());
                Log.e(TAG, "port:" + joinMsg.getPort());
                Log.e(TAG, "surface:" + mSurfaceView);
                int i = meetingManager.requestMemberVideoInVideoMeeting(mConferenceId, "",
                        ecVideoMeetingMember.getNumber(), mSurfaceView, joinMsg.getIp(), joinMsg.getPort(),
                        new ECMeetingManager.OnMemberVideoFrameChangedListener() {
                            @Override
                            public void onMemberVideoFrameChanged(boolean isRequest, ECError reason, String meetingNo, String account) {
                                Log.e(TAG, "onMemberVideoFrameChanged:" + account);
                                ECDevice.getECMeetingManager().resetVideoMeetingWindow(account, mSurfaceView);
                            }
                        });
                Log.e(TAG, "i:" + i);
                ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
            }
        }

    }

    private boolean isVideoUIMemberExist(String who) {
        synchronized (mUserAccounts) {
            if (TextUtils.isEmpty(who)) {
                return false;
            }

            if (mUserAccounts.containsKey(who)) {
                return true;
            }

            return false;
        }
    }
}
