package com.yongyida.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.easemob.EMCallBack;
import com.yongyida.robot.R;
import com.yongyida.robot.huanxin.DemoHXSDKHelper;
import com.yongyida.robot.ronglianyun.SDKCoreHelper;
import com.yongyida.robot.service.SocketService;
import com.yongyida.robot.utils.BroadcastReceiverRegister;
import com.yongyida.robot.utils.Constants;
import com.yongyida.robot.utils.ThreadPool;
import com.yongyida.robot.utils.ToastUtil;
import com.yongyida.robot.utils.Utils;

/**
 * Created by Administrator on 2016/7/30 0030.
 */
public abstract class RLYBaseActivity extends Activity{
    private BroadcastReceiver mRlyKickOffBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fulllyExit();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BroadcastReceiverRegister.reg(this, new String[]{Constants.RLY_KICK_OFF}, mRlyKickOffBR);
    }

    private void fulllyExit(){
        ToastUtil.showtomain(RLYBaseActivity.this, getString(R.string.kick_off_relogin));
        SDKCoreHelper.logout(false);
        ThreadPool.execute(new Runnable() {

            @Override
            public void run() {
                DemoHXSDKHelper.getInstance().logout(false,
                        new EMCallBack() {

                            @Override
                            public void onSuccess() {
                                SharedPreferences sharedPreferences = getSharedPreferences(
                                        "userinfo", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences
                                        .edit();
                                editor.clear();
                                editor.commit();
                                getSharedPreferences("huanxin", MODE_PRIVATE).edit().clear().commit();
                                if (Utils.isServiceRunning(RLYBaseActivity.this, SocketService.class.getCanonicalName())) {
                                    Utils.stopSocketService(RLYBaseActivity.this);
                                }
                                startActivity(new Intent(
                                        RLYBaseActivity.this,
                                        NewLoginActivity.class)
                                        .setFlags(
                                                Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .addFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK));

                            }

                            @Override
                            public void onProgress(int arg0, String arg1) {

                            }

                            @Override
                            public void onError(int arg0, String arg1) {

                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.unRegisterReceiver(mRlyKickOffBR, this);
    }
}
