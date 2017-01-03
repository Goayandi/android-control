package com.yongyida.robot.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;

import com.yongyida.robot.activity.ConnectActivity;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
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
            Utils.startSocketService(getActivity());
            return true;
        } else if (id != 0 && Utils.SystemLanguage.ENGLISH.equals(Utils.getLanguage(getActivity()))){
            StartUtil.startintent(getActivity(), ConnectActivity.class, "finish");
            Utils.startSocketService(getActivity());
            return true;
        }
        return false;
    }

}
