package com.yongyida.robot.utils;

import com.yuntongxun.ecsdk.CameraCapability;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/7/6 0006.
 */
public class CameraUtils {

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
}
