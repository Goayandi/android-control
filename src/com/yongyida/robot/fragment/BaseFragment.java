package com.yongyida.robot.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.StartUtil;
import com.yongyida.robot.utils.Utils;

public class BaseFragment extends Fragment {


    /**
     * 是否登录过
     * @return
     */
    public boolean isLogined(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userinfo",
                getActivity().MODE_PRIVATE);
        int id = sharedPreferences.getInt("id", 0);
        // 如果本地存在记录则自动跳转
        if (id != 0 && DemoHXSDKHelper.getInstance().isLogined()) {
            StartUtil.startintent(getActivity(), ConnectActivity.class, "finish");
            Constants.isUserClose = false;
            if (!Utils.isServiceRunning(getActivity(), SocketService.class.getSimpleName())) {
                getActivity().startService(new Intent(getActivity(), SocketService.class));
            }
            return true;
        }
        return false;
    }

}
