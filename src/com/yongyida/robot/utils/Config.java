package com.yongyida.robot.utils;

/**
 * Created by Administrator on 2016/4/12 0012.
 */
/**
 * Copyright (C) 2015 Zhensheng Yongyida Robot Co.,Ltd. All rights reserved.
 *
 * @author: hujianfeng@gmail.com
 * @version 0.1
 * @date 2015-09-01
 *
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.yongyida.robot.video.comm.log;

/**
 * 应用程序配置项
 * @author hujianfeng
 *
 */
public class Config {
    private static final String TAG = "Config";

    private static final String PREFERENCE_NAME = "yyd_robot_video_app_setting";
    private final static String SHARED_KEY_TRANSFERDATA_TYPE = "shared_key_setting_transferdata_type";
    private final static String SHARED_KEY_TRANSFER_TYPE = "shared_key_setting_transfer_type";
    private final static String SHARED_KEY_ENCODER_TYPE = "shared_key_setting_encoder_type";
    private final static String SHARED_KEY_VIDEOSIZE_TYPE = "shared_key_setting_videosize_type";
    private final static String SHARED_KEY_BITRATE_TYPE = "shared_key_setting_bitrate_type";
    private final static String SHARED_KEY_FRAMERATE_TYPE = "shared_key_setting_framerate_type";

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    /**
     *  私有构造，只能静态使用，不能构造实例。
     */
    private Config() {
    }

    /**
     * 初始化函数
     *
     */
    public static void init(Context context) {
        log.i(TAG, "Config init()");

        mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static int getTransferDataType() {
        return mSharedPreferences.getInt(SHARED_KEY_TRANSFERDATA_TYPE, 1);
    }

    public static void setTransferDataType(int value) {
        mEditor.putInt(SHARED_KEY_TRANSFERDATA_TYPE, value);
        mEditor.commit();
    }

    public static int getTransferType() {
        return mSharedPreferences.getInt(SHARED_KEY_TRANSFER_TYPE, 1);
    }

    public static void setTransferType(int value) {
        mEditor.putInt(SHARED_KEY_TRANSFER_TYPE, value);
        mEditor.commit();
    }

    public static int getEncoderType() {
        return mSharedPreferences.getInt(SHARED_KEY_ENCODER_TYPE, 1);
    }

    public static void setEncoderType(int value) {
        mEditor.putInt(SHARED_KEY_ENCODER_TYPE, value);
        mEditor.commit();
    }

    public static int getVideoSizeType() {
        return mSharedPreferences.getInt(SHARED_KEY_VIDEOSIZE_TYPE, 1);
    }

    public static void setVideoSizeType(int value) {
        mEditor.putInt(SHARED_KEY_VIDEOSIZE_TYPE, value);
        mEditor.commit();
    }

    public static int getFrameRateType() {
        return mSharedPreferences.getInt(SHARED_KEY_FRAMERATE_TYPE, 0);
    }

    public static void setFrameRateType(int value) {
        mEditor.putInt(SHARED_KEY_FRAMERATE_TYPE, value);
        mEditor.commit();
    }

    public static int getBitRateType() {
        return mSharedPreferences.getInt(SHARED_KEY_BITRATE_TYPE, 0);
    }

    public static void setBitRateType(int value) {
        mEditor.putInt(SHARED_KEY_BITRATE_TYPE, value);
        mEditor.commit();
    }

    public static void clear() {
        mEditor.clear();
    }

    @Override
    public String toString() {
        return "Config [ TransferDataType: " + getTransferDataType()
                + "TransferType: " + getTransferType()
                + "EncoderType: " + getEncoderType()
                + "VideoSizeType: " + getVideoSizeType()
                + "BitRateType: " + getBitRateType()
                + "FrameRateType: " + getFrameRateType() + "]";
    }
}

