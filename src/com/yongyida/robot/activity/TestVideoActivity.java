package com.yongyida.robot.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.yongyida.robot.R;
import com.yuntongxun.ecsdk.CameraCapability;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/5/31 0031.
 */
public class TestVideoActivity extends BaseActivity {

    private static final String TAG = "TestVideoActivity";
    private SurfaceView view;
    private ECCaptureView mCameraView;
    private int mCameraIndex = 0;
    private int mVideoWidth = 480;
    private int mVideoHeight = 640;
    private int mFps = 15;
    private String mCurrentCallId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);
        view = (SurfaceView)findViewById(R.id.opposite_surface);
        mCameraView = (ECCaptureView) findViewById(R.id.localvideo_view);
        mCameraView.setZOrderMediaOverlay(true);

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraIndex = camIdx;
            }
        }

        CameraInfo[] cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();
        int mCameraCapbilityIndex = 0;
        if (cameraInfos != null) {
            for (int i = 0; i < cameraInfos.length; i++) {
                if (cameraInfos[i].index == mCameraIndex) {
                    mCameraCapbilityIndex = comportCapabilityIndex(cameraInfos[cameraInfos[i].index].caps, mVideoWidth * mVideoHeight);
                }
            }
        }
        Log.e(TAG, "mCameraCapbilityIndex:" + mCameraCapbilityIndex);
        ECDevice.getECVoIPSetupManager().setVideoView(view, mCameraView);
        ECDevice.getECVoIPSetupManager().selectCamera(mCameraIndex, mCameraCapbilityIndex, mFps, ECVoIPSetupManager.Rotate.ROTATE_AUTO, true);
    //    ECDevice.getECVoIPSetupManager().setMute(false);
        //view 显示远端视频的surfaceview
        //localView本地显示视频的view
        mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(ECVoIPCallManager.CallType.VIDEO, "18664920330");

        ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if(callInterface != null) {
            callInterface.setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio) {

                }

                @Override
                public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

                }

                @Override
                public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

                }

                @Override
                public void onDtmfReceived(String s, char c) {

                }

                @Override
                public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
                    // 处理呼叫事件回调
                    if(voipCall == null) {
                        Log.e("SDKCoreHelper", "handle call event error , voipCall null");
                        return ;
                    }
                    // 根据不同的事件通知类型来处理不同的业务
                    ECVoIPCallManager.ECCallState callState = voipCall.callState;
                    switch (callState) {
                        case ECCALL_PROCEEDING:
                            // 正在连接服务器处理呼叫请求
                            break;
                        case ECCALL_ALERTING:
                            // 呼叫到达对方客户端，对方正在振铃
                            break;
                        case ECCALL_ANSWERED:
                            if(ECDevice.getECVoIPSetupManager() != null) {
                                ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
                            }
                            // 对方接听本次呼叫
                            break;
                        case ECCALL_FAILED:
                            // 本次呼叫失败，根据失败原因播放提示音
                            break;
                        case ECCALL_RELEASED:
                            // 通话释放[完成一次呼叫]
                            finish();
                            break;
                        default:
                            Log.e("SDKCoreHelper", "handle call event error , callState " + callState);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void initlayout(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
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

    public void hangup(View v) {
        ECDevice.getECVoIPCallManager().releaseCall(mCurrentCallId);
        finish();
    }

}
