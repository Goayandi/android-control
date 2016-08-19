package com.yongyida.robot.activity;

import android.hardware.Camera;
import android.os.Bundle;

import com.yongyida.robot.utils.CameraUtils;
import com.yuntongxun.ecsdk.CameraInfo;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;

/**
 * Created by Administrator on 2016/7/6 0006.
 */
public abstract class RlyVideoBaseActivity extends RLYBaseActivity {
    private final static int FPS = 15;
    private final static int COMPLIANT = 320 * 240;
    private static final String TAG = "RlyVideoBaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCamera();
    }

    protected void initCamera(){
        setSelectCamera(Camera.CameraInfo.CAMERA_FACING_FRONT, getCapbilityIndex(Camera.CameraInfo.CAMERA_FACING_FRONT));
    }

    /**
     * 获取合适的摄像头参数
     * @param camera   前置还是后置
     * @return
     */
    private int getCapbilityIndex(int camera) {
        CameraInfo[] cameraInfos = ECDevice.getECVoIPSetupManager().getCameraInfos();
        int cameraCapbilityIndex = 0;
        if (cameraInfos != null) {
            for (int i = 0; i < cameraInfos.length; i++) {
                if (cameraInfos[i].index == camera) {
                    cameraCapbilityIndex = CameraUtils.comportCapabilityIndex(cameraInfos[cameraInfos[i].index].caps, COMPLIANT);
                }
            }
        }
        return cameraCapbilityIndex;
    }

    /**
     * 建立视频连接
     * @param number
     * @return
     */
    protected String establishVideo(String number){
        String id = "";
        try {
            id = ECDevice.getECVoIPCallManager().makeCall(ECVoIPCallManager.CallType.VIDEO, number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * 挂断
     * @param callid
     */
    protected void hangup(String callid){
        ECDevice.getECVoIPCallManager().releaseCall(callid);
    }

    /**
     * 设置摄像头
     */
    protected void setSelectCamera(int cameraIndex, int capabilityIndex){
        ECDevice.getECVoIPSetupManager().selectCamera(cameraIndex, capabilityIndex, FPS, ECVoIPSetupManager.Rotate.ROTATE_0, true);
    }

    /**
     * 设置扬声器状态
     * @param on
     */
    protected void setSpeakerState(boolean on) {
        ECDevice. getECVoIPSetupManager().enableLoudSpeaker(on);
    }

    /**
     * 获取扬声器状态
     * @return
     */
    protected boolean getSpeakerState(){
        return ECDevice. getECVoIPSetupManager().getLoudSpeakerStatus();
    }

    /**
     * 设置静音状态
     * @param on
     */
    protected void setMuteState(boolean on){
        ECDevice. getECVoIPSetupManager().setMute(on);
    }

    /**
     * 获取静音状态
     * @return
     */
    protected boolean getMuteState(){
        return ECDevice.getECVoIPSetupManager().getMuteStatus();
    }

    /**
     * 设置码流
     * @param bitRates
     */
    protected void setVideoBitRates(int bitRates){
        ECDevice.getECVoIPSetupManager().setVideoBitRates(bitRates);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSpeakerState(false);
        setMuteState(false);
    }
}
